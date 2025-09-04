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

    // Changed return type from List<Chat> to List<GetChatInfoOutputModel>
    public RpcResponse<List<GetChatInfoOutputModel>> getChatsByUser() {
        List<Membership> memberships = daoManager.getMembershipDAO()
                .findAllByField("account.id", UUID.fromString(getCurrentUser().getUserId()));

        List<GetChatInfoOutputModel> chatInfoList = memberships.stream()
                .map(membership -> {
                    Chat chat = membership.getChat();
                    GetChatInfoOutputModel output = new GetChatInfoOutputModel();
                    output.setId(chat.getId());
                    output.setType(chat.getType().toString());
                    output.setTitle(chat.getTitle());
                    output.setProfilePictureId(chat.getProfilePictureId());
                    return output;
                })
                .collect(Collectors.toList());

        return Ok(chatInfoList);
    }

    // Changed return type from Object to CreateChannelOutputModel
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

        // Map to output model
        CreateChannelOutputModel output = new CreateChannelOutputModel();
        output.setId(newChannel.getId());
        output.setType(newChannel.getType());
        output.setTitle(newChannel.getTitle());
        output.setProfilePictureId(newChannel.getProfilePictureId());
        output.setDescription(newChannel.getDescription());
        output.setPublic(newChannel.isPublic());
        output.setCreatorId(newChannel.getCreatedBy().getId());

        return Ok(output);
    }

    // Changed return type from Object to CreateGroupOutputModel
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

        // Map to output model
        CreateGroupOutputModel output = new CreateGroupOutputModel();
        output.setId(newGroup.getId());
        output.setType(newGroup.getType());
        output.setTitle(newGroup.getTitle());
        output.setProfilePictureId(newGroup.getProfilePictureId());
        output.setDescription(newGroup.getDescription());
        output.setCreatorId(newGroup.getCreatedBy().getId());
        output.setInitialMemberIds(model.getMemberIds()); // Reflect initial members from input

        return Ok(output);
    }

    public RpcResponse<Object> getChatInfo(GetChatInfoInputModel model){
        Chat chat = daoManager.getChatDAO().findById(model.getChatId());
        if (chat == null) {
            return BadRequest("Chat not found.");
        }

        GetChatInfoOutputModel output = new GetChatInfoOutputModel();
        output.setId(chat.getId());
        output.setType(chat.getType().toString());
        output.setTitle(chat.getTitle());
        output.setProfilePictureId(chat.getProfilePictureId());

        return Ok(output);
    }

    public RpcResponse<Object> updateChatInfo(UpdateChatInfoInputModel model){
        Chat chat = daoManager.getChatDAO().findById(model.getChatId());
        if (chat == null) {
            return BadRequest("Chat not found.");
        }

        if (model.getTitle() != null) {
            chat.setTitle(model.getTitle());
        }

        if (model.getProfilePictureId() != null) {
            chat.setProfilePictureId(model.getProfilePictureId());
        }

        daoManager.getChatDAO().update(chat);

        UpdateChatInfoOutputModel output = new UpdateChatInfoOutputModel();
        output.setId(chat.getId());
        output.setType(chat.getType());
        output.setTitle(chat.getTitle());
        output.setProfilePictureId(chat.getProfilePictureId());

        return Ok(output);
    }

    // Changed return type from Object to GetChatInfoOutputModel
    public RpcResponse<Object> getChatById(GetChatByIdInputModel model) {
        Chat chat = daoManager.getChatDAO().findById(model.getChatId());
        if (chat == null) {
            return BadRequest("Chat not found.");
        }
        GetChatInfoOutputModel output = new GetChatInfoOutputModel();
        output.setId(chat.getId());
        output.setType(chat.getType().toString());
        output.setTitle(chat.getTitle());
        output.setProfilePictureId(chat.getProfilePictureId());
        return Ok(output);
    }
}