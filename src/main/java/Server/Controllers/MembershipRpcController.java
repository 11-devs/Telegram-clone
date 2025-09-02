package Server.Controllers;

import JSocket2.Protocol.Rpc.RpcControllerBase;
import JSocket2.Protocol.Rpc.RpcResponse;
import Server.DaoManager;
import Shared.Api.Models.MembershipController.*;
import Shared.Models.Account.Account;
import Shared.Models.Chat.Chat;
import Shared.Models.Membership.Membership;
import Shared.Models.Membership.MembershipType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MembershipRpcController extends RpcControllerBase {
    private final DaoManager daoManager;

    public MembershipRpcController(DaoManager daoManager) {
        this.daoManager = daoManager;
    }

    private Membership findMembership(UUID chatId, UUID accountId) {
        List<Membership> membershipsInChat = daoManager.getMembershipDAO().findAllByField("chat.id", chatId);
        return membershipsInChat.stream()
                .filter(m -> m.getAccount().getId().equals(accountId))
                .findFirst()
                .orElse(null);
    }

    public RpcResponse<Object> addMember(AddMemberInputModel model) {
        Chat chat = daoManager.getChatDAO().findById(model.getChatId());
        if (chat == null) {
            return BadRequest("Chat not found.");
        }

        Account memberToAdd = daoManager.getAccountDAO().findById(model.getMemberId());
        if (memberToAdd == null) {
            return BadRequest("User to add not found.");
        }

        Account inviter = daoManager.getAccountDAO().findById(model.getInviterId());
        if (inviter == null) {
            return BadRequest("Inviter not found.");
        }

        if (findMembership(model.getChatId(), model.getMemberId()) != null) {
            return BadRequest("User is already a member of this chat.");
        }

        Membership newMembership = new Membership();
        newMembership.setAccount(memberToAdd);
        newMembership.setChat(chat);
        newMembership.setInvitedBy(inviter);
        newMembership.setJoinDate(LocalDateTime.now());
        newMembership.setType(MembershipType.MEMBER);

        daoManager.getMembershipDAO().insert(newMembership);

        AddMemberOutputModel output = new AddMemberOutputModel();
        output.setMembership(newMembership);
        return Ok(output);
    }

    public RpcResponse<Object> kickMember(KickMemberInputModel model) {
        Membership membership = findMembership(model.getChatId(), model.getMemberId());
        if (membership == null) {
            return BadRequest("Member not found in this chat.");
        }

        if (membership.getType() == MembershipType.OWNER) {
            return BadRequest("Cannot kick the owner of the chat.");
        }

        daoManager.getMembershipDAO().delete(membership);

        return Ok(new KickMemberOutputModel("Member successfully kicked."));
    }

    public RpcResponse<Object> joinChat(JoinChatInputModel model) {
        Chat chat = daoManager.getChatDAO().findById(model.getChatId());
        if (chat == null) {
            return BadRequest("Chat not found.");
        }

        Account user = daoManager.getAccountDAO().findById(model.getUserId());
        if (user == null) {
            return BadRequest("User not found.");
        }

        if (findMembership(model.getChatId(), model.getUserId()) != null) {
            return BadRequest("User is already a member of this chat.");
        }

        Membership newMembership = new Membership();
        newMembership.setAccount(user);
        newMembership.setChat(chat);
        newMembership.setJoinDate(LocalDateTime.now());
        newMembership.setType(MembershipType.MEMBER);

        daoManager.getMembershipDAO().insert(newMembership);

        JoinChatOutputModel output = new JoinChatOutputModel();
        output.setMembership(newMembership);
        return Ok(output);
    }

    public RpcResponse<Object> leaveChat(LeaveChatInputModel model) {
        Membership membership = findMembership(model.getChatId(), model.getUserId());
        if (membership == null) {
            return BadRequest("You are not a member of this chat.");
        }

        if (membership.getType() == MembershipType.OWNER) {
            return BadRequest("Owner cannot leave the chat. Please transfer ownership first.");
        }

        daoManager.getMembershipDAO().delete(membership);

        return Ok(new LeaveChatOutputModel("Successfully left the chat."));
    }
}