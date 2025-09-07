package Server.Controllers;

import JSocket2.Protocol.Rpc.RpcControllerBase;
import JSocket2.Protocol.Rpc.RpcResponse;
import Server.DaoManager;
import Shared.Api.Models.ChatController.*;
import Shared.Models.Account.Account;
import Shared.Models.Chat.*;
import Shared.Models.Media.Media;
import Shared.Models.Membership.Membership;
import Shared.Models.Membership.MembershipType;
import Shared.Models.Message.Message;
import Shared.Models.Message.TextMessage;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChatRpcController extends RpcControllerBase {
    private final DaoManager daoManager;

    public ChatRpcController(DaoManager daoManager) {
        this.daoManager = daoManager;
    }

    public RpcResponse<List<GetChatInfoOutputModel>> getChatsByUser() {
        UUID currentUserId = UUID.fromString(getCurrentUser().getUserId());
        List<Membership> memberships = daoManager.getMembershipDAO()
                .findAllByField("account.id", currentUserId);

        List<GetChatInfoOutputModel> chatInfoList = memberships.stream()
                .map(membership -> {
                    Chat chat = membership.getChat();
                    GetChatInfoOutputModel output = new GetChatInfoOutputModel();
                    output.setId(chat.getId());
                    output.setType(chat.getType().toString());
                    output.setUserMembershipType(membership.getType().toString());
                    if (chat.getType() == ChatType.PRIVATE) {
                        List<Membership> chatMembers = daoManager.getMembershipDAO().findAllByField("chat.id", chat.getId());
                        Optional<Account> otherUserOpt = chatMembers.stream()
                                .map(Membership::getAccount)
                                .filter(account -> !account.getId().equals(currentUserId))
                                .findFirst();

                        if (otherUserOpt.isPresent()) {
                            Account otherUser = otherUserOpt.get();
                            String otherUserName = otherUser.getFirstName() + (otherUser.getLastName() != null ? " " + otherUser.getLastName() : "");
                            output.setTitle(otherUserName.trim());
                            output.setOnline(getServerSessionManager().isUserOnline(otherUser.getId().toString()));
                            if (otherUser.getLastSeen() != null) {
                                output.setLastSeen(otherUser.getLastSeen().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                            }

                            if(otherUser.getProfilePictureId() != null && !otherUser.getProfilePictureId().trim().isEmpty()) {
                                Media media = daoManager.getEntityManager().find(Media.class, UUID.fromString(otherUser.getProfilePictureId()));
                                if (media != null) {
                                    output.setProfilePictureId(media.getFileId());
                                }
                            }
                        } else {
                            output.setTitle("Deleted Account");
                            output.setProfilePictureId(null);
                        }
                    } else {
                        output.setTitle(chat.getTitle());
                        if(chat.getProfilePictureId() != null && !chat.getProfilePictureId().trim().isEmpty()) {
                        Media media = daoManager.getEntityManager().find(Media.class, UUID.fromString(chat.getProfilePictureId()));
                        if (media != null) {
                            output.setProfilePictureId(media.getFileId());
                        }
                        }
                    }

                    Message lastMessage = daoManager.getMessageDAO().findOneByJpql("SELECT m FROM Message m JOIN FETCH m.sender WHERE m.chat.id = :chatId AND m.isDeleted = false ORDER BY m.timestamp DESC", query -> {
                        query.setParameter("chatId", chat.getId());
                        query.setMaxResults(1);
                    });

                    if (lastMessage != null) {
                        switch (lastMessage.getType()) {
                            case TEXT:
                                TextMessage textMessage = daoManager.getEntityManager().find(TextMessage.class, lastMessage.getId());
                                if (textMessage != null) {
                                    output.setLastMessage(textMessage.getTextContent());
                                }
                                break;
                            case MEDIA:
                                output.setLastMessage("Media");
                                break;
                            case VOICE:
                                output.setLastMessage("Voice Message");
                                break;
                            case VIDEO:
                                output.setLastMessage("Video");
                                break;
                            default:
                                output.setLastMessage("...");
                                break;
                        }
                        output.setLastMessageTimestamp(lastMessage.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                        Account sender = lastMessage.getSender();
                        if (sender != null) {
                            output.setLastMessageSenderName(sender.getFirstName());
                        }
                    } else {
                        output.setLastMessage("");
                        output.setLastMessageTimestamp(null);
                        output.setLastMessageSenderName("");
                    }

                    // SOFT-DELETE FIX: Safely get the last read timestamp without touching the proxy object.
                    LocalDateTime lastReadTimestamp = null;
                    try {
                        TypedQuery<LocalDateTime> tsQuery = daoManager.getEntityManager().createQuery(
                                "SELECT msg.timestamp FROM Membership mem JOIN mem.lastReadMessage msg " +
                                        "WHERE mem.id = :membershipId AND msg.isDeleted = false", LocalDateTime.class);
                        tsQuery.setParameter("membershipId", membership.getId());
                        lastReadTimestamp = tsQuery.getSingleResult();
                    } catch (NoResultException e) {
                        // Expected if last read message was deleted or never existed.
                    }

                    long unreadCount;
                    if (lastReadTimestamp == null) {
                        // If no valid last read message exists, count all non-deleted messages not sent by the current user.
                        String jpql = "SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId AND m.sender.id != :senderId AND m.isDeleted = false";
                        unreadCount = daoManager.getMessageDAO().countByJpql(jpql, query -> {
                            query.setParameter("chatId", chat.getId());
                            query.setParameter("senderId", currentUserId);
                        });
                    } else {
                        // Count non-deleted messages newer than the last valid one read.
                        final LocalDateTime finalLastReadTimestamp = lastReadTimestamp;
                        String jpql = "SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId AND m.timestamp > :lastReadTimestamp AND m.sender.id != :senderId AND m.isDeleted = false";
                        unreadCount = daoManager.getMessageDAO().countByJpql(jpql, query -> {
                            query.setParameter("chatId", chat.getId());
                            query.setParameter("lastReadTimestamp", finalLastReadTimestamp);
                            query.setParameter("senderId", currentUserId);
                        });
                    }
                    output.setUnreadCount((int) unreadCount);
                    output.setMuted(membership.isMuted());
                    return output;
                })
                .collect(Collectors.toList());

        return Ok(chatInfoList);
    }
    public RpcResponse<Object> toggleChatMute(ToggleChatMuteInputModel model) {
        UUID currentUserId = UUID.fromString(getCurrentUser().getUserId());

        Membership membership = daoManager.getMembershipDAO().findOneByJpql(
                "SELECT m FROM Membership m WHERE m.chat.id = :chatId AND m.account.id = :accountId",
                query -> {
                    query.setParameter("chatId", model.getChatId());
                    query.setParameter("accountId", currentUserId);
                }
        );

        if (membership == null) {
            return NotFound();
        }

        membership.setMuted(model.isMuted());
        daoManager.getMembershipDAO().update(membership);

        return Ok();
    }
    public RpcResponse<Object> getChatByUsername(String username) {
        UUID currentUserId = UUID.fromString(getCurrentUser().getUserId());
        Account targetUser = daoManager.getAccountDAO().findByField("username", username.toLowerCase());
        if (targetUser == null) {
            return NotFound();
        }
        if (targetUser.getId().equals(currentUserId)) {
            return BadRequest("Cannot open a chat with yourself this way.");
        }

        String jpql = "SELECT pc FROM PrivateChat pc WHERE " +
                "(pc.user1.id = :user1Id AND pc.user2.id = :user2Id) OR " +
                "(pc.user1.id = :user2Id AND pc.user2.id = :user1Id)";
        PrivateChat privateChat = daoManager.getPrivateChatDAO().findOneByJpql(jpql, query -> {
            query.setParameter("user1Id", currentUserId);
            query.setParameter("user2Id", targetUser.getId());
        });

        if (privateChat == null) {
            Account currentUser = daoManager.getAccountDAO().findById(currentUserId);
            if (currentUser == null) {
                return BadRequest("Could not find current user account.");
            }
            privateChat = new PrivateChat(currentUser, targetUser);
            daoManager.getPrivateChatDAO().insert(privateChat);

            Membership currentUserMembership = new Membership();
            currentUserMembership.setAccount(currentUser);
            currentUserMembership.setChat(privateChat);
            currentUserMembership.setType(MembershipType.MEMBER);
            currentUserMembership.setJoinDate(LocalDateTime.now());
            daoManager.getMembershipDAO().insert(currentUserMembership);

            Membership targetUserMembership = new Membership();
            targetUserMembership.setAccount(targetUser);
            targetUserMembership.setChat(privateChat);
            targetUserMembership.setType(MembershipType.MEMBER);
            targetUserMembership.setJoinDate(LocalDateTime.now());
            daoManager.getMembershipDAO().insert(targetUserMembership);
        }

        GetChatInfoOutputModel output = new GetChatInfoOutputModel();
        output.setId(privateChat.getId());
        output.setType(privateChat.getType().toString());

        String otherUserName = targetUser.getFirstName() + (targetUser.getLastName() != null ? " " + targetUser.getLastName() : "");
        output.setTitle(otherUserName.trim());
        output.setProfilePictureId(targetUser.getProfilePictureId());
        output.setLastMessage("");
        output.setUnreadCount(0);

        return Ok(output);
    }
}