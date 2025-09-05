package Server.Controllers;

import JSocket2.Protocol.Rpc.RpcControllerBase;
import JSocket2.Protocol.Rpc.RpcResponse;
import Server.DaoManager;
import Shared.Api.Models.ChatController.*;
import Shared.Models.Account.Account;
import Shared.Models.Chat.*;
import Shared.Models.Membership.Membership;
import Shared.Models.Membership.MembershipType;
import Shared.Models.Message.MediaMessage;
import Shared.Models.Message.Message;
import Shared.Models.Message.TextMessage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChatRpcController extends RpcControllerBase {
    private final DaoManager daoManager;
    //private final TypingEvent typingEvent;

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

                    // --- MODIFIED LOGIC: Handle title and picture for Private Chats ---
                    if (chat.getType() == ChatType.PRIVATE) {
                        // For a private chat, find the other member to use their name and profile picture.
                        List<Membership> chatMembers = daoManager.getMembershipDAO().findAllByField("chat.id", chat.getId());
                        Optional<Account> otherUserOpt = chatMembers.stream()
                                .map(Membership::getAccount)
                                .filter(account -> !account.getId().equals(currentUserId))
                                .findFirst();

                        if (otherUserOpt.isPresent()) {
                            Account otherUser = otherUserOpt.get();
                            String otherUserName = otherUser.getFirstName() + (otherUser.getLastName() != null ? " " + otherUser.getLastName() : "");
                            output.setTitle(otherUserName.trim());
                            output.setProfilePictureId(otherUser.getProfilePictureId());
                        } else {
                            // Fallback if the other user isn't found (e.g., deleted account)
                            output.setTitle("Deleted Account");
                            output.setProfilePictureId(null);
                        }
                    } else {
                        // For groups and channels, use their stored title and picture.
                        output.setTitle(chat.getTitle());
                        output.setProfilePictureId(chat.getProfilePictureId());
                    }
                    // --- END OF MODIFIED LOGIC --

                    // Find the last message for this chat
                    Message lastMessage = daoManager.getMessageDAO().findOneByJpql("SELECT m FROM Message m JOIN FETCH m.sender WHERE m.chat.id = :chatId ORDER BY m.timestamp DESC",query ->{
                        query.setParameter("chatId", chat.getId());
                        query.setMaxResults(1);
                    });

                    if (lastMessage != null) {
                        // Set last message content using a switch on the type for proxy safety
                        switch (lastMessage.getType()) {
                            case TEXT:
                                // FIX: The cast ((TextMessage) lastMessage) can fail if 'lastMessage' is a Hibernate proxy.
                                // To prevent this, we fetch the specific TextMessage entity using its ID, which is safe.
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

                        // Set timestamp in ISO format for client-side parsing
                        output.setLastMessageTimestamp(lastMessage.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

                        // Set sender name
                        Account sender = lastMessage.getSender();
                        if (sender != null) {
                            output.setLastMessageSenderName(sender.getFirstName());
                        }

                    } else {
                        // Handle chats with no messages
                        output.setLastMessage("");
                        output.setLastMessageTimestamp(null);
                        output.setLastMessageSenderName("");
                    }
                    Message lastReadMessage = membership.getLastReadMessage();
                    long unreadCount = 0;
                    if (lastReadMessage == null) {
                        // If user has never read anything, count all messages not sent by them.
                        String jpql = "SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId AND m.sender.id != :senderId";
                        unreadCount = daoManager.getMessageDAO().countByJpql(jpql, query -> {
                            query.setParameter("chatId", chat.getId());
                            query.setParameter("senderId", currentUserId);
                        });
                    } else {
                        // Count messages newer than the last one read that were not sent by the current user.
                        final LocalDateTime lastReadTimestamp = lastReadMessage.getTimestamp();
                        String jpql = "SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId AND m.timestamp > :lastReadTimestamp AND m.sender.id != :senderId";
                        unreadCount = daoManager.getMessageDAO().countByJpql(jpql, query -> {
                            query.setParameter("chatId", chat.getId());
                            query.setParameter("lastReadTimestamp", lastReadTimestamp);
                            query.setParameter("senderId", currentUserId);
                        });
                    }
                    output.setUnreadCount((int) unreadCount);
                    return output;
                })
                .collect(Collectors.toList());

        return Ok(chatInfoList);
    }
//... (rest of the file is unchanged)
}