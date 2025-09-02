package Server.Controllers;

import JSocket2.Protocol.Rpc.RpcControllerBase;
import JSocket2.Protocol.Rpc.RpcResponse;
import Server.DaoManager;
import Shared.Api.Models.ChatController.*;
import Shared.Models.Account.Account;
import Shared.Models.Chat.Channel;
import Shared.Models.Chat.Chat;
import Shared.Models.Chat.GroupChat;
import Shared.Models.Membership.Membership;
import Shared.Models.Membership.MembershipType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChatRpcController extends RpcControllerBase {
    private final DaoManager daoManager;

    public ChatRpcController(DaoManager daoManager) {
        this.daoManager = daoManager;
    }

    public RpcResponse<List<Chat>> getChatsByUser(getChatsByUserInputModel model) {
        List<Membership> memberships = daoManager.getMembershipDAO()
                .findAllByField("account.id", model.getUserId());

        List<Chat> chats = memberships.stream()
                .map(Membership::getChat)
                .collect(Collectors.toList());

        return Ok(chats);
    }


    public RpcResponse<Object> createChannel(CreateChannelInputModel model) {
        // Find creator account
        Account creator = daoManager.getAccountDAO().findById(model.getCreatorId());
        if (creator == null) {
            return BadRequest("Creator account not found.");
        }

        // Create a new channel entity
        Channel newChannel = new Channel(model.getTitle(), model.getProfilePictureId(), creator, model.getDescription(), model.isPublic());
        daoManager.getChannelDAO().insert(newChannel);

        // Add creator as the first member and admin
        Membership creatorMembership = new Membership();
        creatorMembership.setAccount(creator);
        creatorMembership.setChat(newChannel);
        creatorMembership.setType(MembershipType.OWNER);
        creatorMembership.setJoinDate(LocalDateTime.now());
        daoManager.getMembershipDAO().insert(creatorMembership);

        return Ok(newChannel);
    }

    public RpcResponse<Object> createGroup(CreateGroupInputModel model){
        Account creator = daoManager.getAccountDAO().findById(model.getCreatorId());
        if (creator == null) {
            return BadRequest("Creator account not found.");
        }

        GroupChat newGroup = new GroupChat(
                model.getTitle(),
                model.getProfilePictureId(),
                creator,
                model.getDescription()
        );
        daoManager.getGroupChatDAO().insert(newGroup);

        Membership creatorMembership = new Membership();
        creatorMembership.setAccount(creator);
        creatorMembership.setChat(newGroup);
        creatorMembership.setType(MembershipType.OWNER);
        creatorMembership.setJoinDate(LocalDateTime.now());
        daoManager.getMembershipDAO().insert(creatorMembership);

        if (model.getMemberIds() != null) {
            for (UUID memberId : model.getMemberIds()) {
                if (memberId.equals(model.getCreatorId())) {
                    continue;
                }
                Account memberAccount = daoManager.getAccountDAO().findById(memberId);
                if (memberAccount != null) {
                    Membership memberMembership = new Membership();
                    memberMembership.setAccount(memberAccount);
                    memberMembership.setChat(newGroup);
                    memberMembership.setType(MembershipType.MEMBER); // Default role
                    memberMembership.setJoinDate(LocalDateTime.now());
                    daoManager.getMembershipDAO().insert(memberMembership);
                }
            }
        }

        return Ok(newGroup);
    }
}