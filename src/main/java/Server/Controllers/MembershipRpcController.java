package Server.Controllers;

import JSocket2.Protocol.Rpc.RpcControllerBase;
import JSocket2.Protocol.Rpc.RpcResponse;
import Server.DaoManager;
import Shared.Api.Models.MembershipController.*;
import Shared.Models.Account.Account;
import Shared.Models.Chat.Chat;
import Shared.Models.Media.Media;
import Shared.Models.Membership.Membership;
import Shared.Models.Membership.MembershipType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

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
        UUID inviterId = UUID.fromString(getCurrentUser().getUserId());
        Membership inviterMembership = findMembership(model.getChatId(), inviterId);

        if (inviterMembership == null || (inviterMembership.getType() != MembershipType.OWNER && inviterMembership.getType() != MembershipType.ADMIN)) {
            return Forbidden("You do not have permission to add members to this chat.");
        }

        Chat chat = daoManager.getChatDAO().findById(model.getChatId());
        if (chat == null) {
            return BadRequest("Chat not found.");
        }

        Account memberToAdd = daoManager.getAccountDAO().findById(model.getMemberId());
        if (memberToAdd == null) {
            return BadRequest("User to add not found.");
        }

        Account inviter = inviterMembership.getAccount();

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
        UUID kickerId = UUID.fromString(getCurrentUser().getUserId());
        Membership kickerMembership = findMembership(model.getChatId(), kickerId);

        if (kickerMembership == null || (kickerMembership.getType() != MembershipType.OWNER && kickerMembership.getType() != MembershipType.ADMIN)) {
            return Forbidden("You do not have permission to remove members from this chat.");
        }

        Membership memberToKick = findMembership(model.getChatId(), model.getMemberId());
        if (memberToKick == null) {
            return BadRequest("Member not found in this chat.");
        }

        if (memberToKick.getType() == MembershipType.OWNER) {
            return BadRequest("Cannot kick the owner of the chat.");
        }

        if (kickerMembership.getType() == MembershipType.ADMIN && memberToKick.getType() == MembershipType.ADMIN) {
            return Forbidden("Admins cannot kick other admins.");
        }

        daoManager.getMembershipDAO().delete(memberToKick);

        return Ok(new KickMemberOutputModel("Member successfully kicked."));
    }

    public RpcResponse<Object> getChatMembers(UUID chatId) {
        UUID currentUserId = UUID.fromString(getCurrentUser().getUserId());
        if (findMembership(chatId, currentUserId) == null) {
            return Forbidden("You are not a member of this chat.");
        }

        List<Membership> memberships = daoManager.getMembershipDAO().findAllByField("chat.id",chatId);
        List<MemberInfo> memberInfos = memberships.stream().map(m -> {
            Account acc = m.getAccount();
            String profilePicFileId = null;
            if (acc.getProfilePictureId() != null && !acc.getProfilePictureId().isBlank()) {
                Media media = daoManager.getEntityManager().find(Media.class, UUID.fromString(acc.getProfilePictureId()));
                if (media != null) {
                    profilePicFileId = media.getFileId();
                }
            }
            boolean isOnline = getServerSessionManager().isUserOnline(acc.getId().toString());
            String lastSeen = isOnline || acc.getLastSeen() == null ? null : acc.getLastSeen().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            return new MemberInfo(acc.getId(), acc.getFirstName(), acc.getLastName(), acc.getUsername(), profilePicFileId, m.getType().name(), isOnline, lastSeen);
        }).collect(Collectors.toList());

        return Ok(new GetChatMembersOutputModel(memberInfos));
    }

    public RpcResponse<Object> updateMemberRole(UpdateMemberRoleInputModel model) {
        UUID currentUserId = UUID.fromString(getCurrentUser().getUserId());
        Membership currentUserMembership = findMembership(model.getChatId(), currentUserId);

        if (currentUserMembership == null || currentUserMembership.getType() != MembershipType.OWNER) {
            return Forbidden("Only the owner can change member roles.");
        }

        if (currentUserId.equals(model.getMemberId())) {
            return BadRequest("You cannot change your own role.");
        }

        Membership targetMembership = findMembership(model.getChatId(), model.getMemberId());
        if (targetMembership == null) {
            return BadRequest("Target member not found in this chat.");
        }

        try {
            MembershipType newRole = MembershipType.valueOf(model.getNewRole().toUpperCase());
            if (newRole == MembershipType.OWNER) {
                return BadRequest("Ownership can only be transferred, not assigned.");
            }
            targetMembership.setType(newRole);
            daoManager.getMembershipDAO().update(targetMembership);
            return Ok();
        } catch (IllegalArgumentException e) {
            return BadRequest("Invalid role specified.");
        }
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