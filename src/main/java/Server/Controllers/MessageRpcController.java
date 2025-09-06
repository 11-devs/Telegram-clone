// Entire file content is provided for clarity as multiple methods are changed or added.
package Server.Controllers;

import JSocket2.Protocol.Rpc.RpcControllerBase;
import JSocket2.Protocol.Rpc.RpcResponse;
import Server.DaoManager;
import Server.Events.*;
import Shared.Api.Models.MessageController.*;
import Shared.Events.Models.*;
import Shared.Models.Account.Account;
import Shared.Models.Chat.Chat;
import Shared.Models.Media.Media;
import Shared.Models.Membership.Membership;
import Shared.Models.Message.MediaMessage;
import Shared.Models.Message.Message;
import Shared.Models.Message.TextMessage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class MessageRpcController extends RpcControllerBase {
    private final DaoManager daoManager;
    private final NewMessageEvent newMessageEvent;
    private final MessageEditedEvent messageEditedEvent;
    private final MessageDeletedEvent messageDeletedEvent;
    private final MessageReadEvent messageReadEvent;
    private final UserTypingEvent userTypingEvent;

    public MessageRpcController(DaoManager daoManager, NewMessageEvent newMessageEvent, MessageEditedEvent messageEditedEvent, MessageDeletedEvent messageDeletedEvent, MessageReadEvent messageReadEvent, UserTypingEvent userTypingEvent) {
        this.daoManager = daoManager;
        this.newMessageEvent = newMessageEvent;
        this.messageEditedEvent = messageEditedEvent;
        this.messageDeletedEvent = messageDeletedEvent;
        this.messageReadEvent = messageReadEvent;
        this.userTypingEvent = userTypingEvent;
    }


    private GetMessageOutputModel mapMessageToOutputModel(Message message) {
        if (message == null) {
            return null;
        }

        GetMessageOutputModel output = new GetMessageOutputModel();
        output.setMessageId(message.getId());
        if (message.getSender() != null) {
            output.setSenderId(message.getSender().getId());
            String senderName = message.getSender().getFirstName() +
                    (message.getSender().getLastName() != null ? " " + message.getSender().getLastName() : "");
            output.setSenderName(senderName.trim());
        }
        if (message.getChat() != null) {
            output.setChatId(message.getChat().getId());
        }
        output.setTimestamp(message.getTimestamp() != null ? message.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        output.setEdited(message.isEdited());
        output.setMessageType(message.getType());

        boolean isOutgoing = Objects.equals(output.getSenderId().toString(), getCurrentUser().getUserId());
        output.setOutgoing(isOutgoing);

        // Determine message status for outgoing messages
        if (isOutgoing) {
            List<Membership> otherMembers = daoManager.getMembershipDAO().findByJpql(
                    "SELECT m FROM Membership m LEFT JOIN FETCH m.lastReadMessage WHERE m.chat.id = :chatId AND m.account.id != :senderId",
                    q -> {
                        q.setParameter("chatId", message.getChat().getId());
                        q.setParameter("senderId", message.getSender().getId());
                    }
            );

            boolean isReadByAll = !otherMembers.isEmpty() && otherMembers.stream().allMatch(m ->
                    m.getLastReadMessage() != null && !m.getLastReadMessage().getTimestamp().isBefore(message.getTimestamp())
            );

            if (isReadByAll) {
                output.setMessageStatus("read");
            } else {
                output.setMessageStatus("delivered");
            }
        } else {
            output.setMessageStatus("none"); // Status is not relevant for incoming messages.
        }

        // ============================ THE DEFINITIVE FIX ============================
        // The 'message' object from the list is a proxy of the base class.
        // A direct cast will fail. We must use EntityManager.find() to get the
        // correctly-typed subclass instance. This is efficient as the data is already
        // in the L1 cache from the previous query.
        switch (message.getType()) {
            case TEXT:
                TextMessage textMessage = daoManager.getEntityManager().find(TextMessage.class, message.getId());
                if (textMessage != null) {
                    output.setTextContent(textMessage.getTextContent());
                }
                break;
            case MEDIA:
                MediaMessage mediaMessage = daoManager.getEntityManager().find(MediaMessage.class, message.getId());
                if (mediaMessage != null) {
                    Media media = mediaMessage.getMedia();
                    if (media != null) {
                        output.setMediaId(media.getId());
                        output.setFileId(media.getFileId());
                    }
                }
                break;
        }
        // ===========================================================================

        return output;
    }

    public RpcResponse<Object> getMessageById(GetMessageByIdInputModel model) {
        Message message = daoManager.getMessageDAO().findById(model.getMessageId());
        if (message == null) {
            return BadRequest("Message not found.");
        }
        GetMessageOutputModel output = mapMessageToOutputModel(message);
        return Ok(output);
    }

    public RpcResponse<List<GetMessageOutputModel>> getMessagesByChat(GetMessageByChatInputModel model) {
        // This query correctly and efficiently fetches all the necessary data.
        List<Message> messages = daoManager.getMessageDAO().findByJpql(
                "SELECT m FROM Message m JOIN FETCH m.sender s LEFT JOIN FETCH TREAT(m AS MediaMessage).media WHERE m.chat.id = :chatId",
                query -> query.setParameter("chatId", model.getChatId())
        );

        if (messages == null || messages.isEmpty()) {
            return Ok(List.of());
        }

        // The mapMessageToOutputModel method now safely handles the proxy objects from the list.
        List<GetMessageOutputModel> outputList = messages.stream()
                .map(this::mapMessageToOutputModel)
                .collect(Collectors.toList());
        return Ok(outputList);
    }

    private void broadcastNewMessageEvent(Message message) {
        List<Membership> members = daoManager.getMembershipDAO().findAllByField("chat.id", message.getChat().getId());
        String senderId = message.getSender().getId().toString();

        NewMessageEventModel eventModel = new NewMessageEventModel();
        eventModel.setMessageId(message.getId());
        eventModel.setSenderId(message.getSender().getId());
        String senderName = message.getSender().getFirstName() + (message.getSender().getLastName() != null ? " " + message.getSender().getLastName() : "");
        eventModel.setSenderName(senderName.trim());
        eventModel.setChatId(message.getChat().getId());
        eventModel.setTimestamp(message.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        eventModel.setEdited(message.isEdited());
        eventModel.setMessageType(message.getType());

        if (message instanceof TextMessage) {
            eventModel.setTextContent(((TextMessage) message).getTextContent());
        } else if (message instanceof MediaMessage) {
            Media media = ((MediaMessage) message).getMedia();
            if (media != null) {
                eventModel.setMediaId(media.getId());
                eventModel.setFileId(media.getFileId());
            }
        }

        for (Membership member : members) {
            if (!member.getAccount().getId().toString().equals(senderId)) {
                try {
                    newMessageEvent.Invoke(getServerSessionManager(), member.getAccount().getId().toString(), eventModel);
                } catch (IOException e) {
                    System.err.println("Failed to send new message event to " + member.getAccount().getId() + ": " + e.getMessage());
                }
            }
        }
    }

    public RpcResponse<Object> sendMessage(SendMessageInputModel model) {
        Account sender = daoManager.getAccountDAO().findById(UUID.fromString(getCurrentUser().getUserId()));
        if (sender == null) {
            return BadRequest("Sender account not found.");
        }

        Chat chat = daoManager.getChatDAO().findById(model.getChatId());
        if (chat == null) {
            return BadRequest("Chat not found.");
        }

        Message newMessage;

        switch (model.getMessageType()) {
            case TEXT:
                if (model.getTextContent() == null || model.getTextContent().trim().isEmpty()) {
                    return BadRequest("Text content cannot be empty for a TEXT message.");
                }
                TextMessage textMessage = new TextMessage();
                textMessage.setTextContent(model.getTextContent());
                newMessage = textMessage;
                break;

            case MEDIA:
                if (model.getMediaId() == null) {
                    return BadRequest("Media ID cannot be null for a MEDIA message.");
                }
                Media media = daoManager.getMediaDAO().findById(model.getMediaId());
                if (media == null) {
                    return BadRequest("Media not found.");
                }
                MediaMessage mediaMessage = new MediaMessage();
                mediaMessage.setMedia(media);
                newMessage = mediaMessage;
                break;

            default:
                return BadRequest("The specified message type is not supported yet.");
        }

        newMessage.setSender(sender);
        newMessage.setChat(chat);
        newMessage.setTimestamp(LocalDateTime.now());
        newMessage.setEdited(false);
        newMessage.setType(model.getMessageType());

        daoManager.getMessageDAO().insert(newMessage);
        Membership senderMembership = daoManager.getMembershipDAO().findAllByField("chat.id", chat.getId())
                .stream()
                .filter(m -> m.getAccount().getId().equals(sender.getId()))
                .findFirst()
                .orElse(null);

        if (senderMembership != null) {
            senderMembership.setLastReadMessage(newMessage);
            daoManager.getMembershipDAO().update(senderMembership);
        }
        broadcastNewMessageEvent(newMessage);

        SendMessageOutputModel output = new SendMessageOutputModel(
                newMessage.getId(),
                newMessage.getTimestamp(),
                "Message sent successfully"
        );

        return Ok(output);
    }

    public RpcResponse<Object> editMessage(EditMessageInputModel model) {
        Message message = daoManager.getMessageDAO().findById(model.getMessageId());
        if (message == null) {
            return NotFound();
        }
        if (!message.getSender().getId().toString().equals(getCurrentUser().getUserId())) {
            return Forbidden("You can only edit your own messages.");
        }
        if (!(message instanceof TextMessage textMessage)) {
            return BadRequest("Only text messages can be edited.");
        }

        textMessage.setTextContent(model.getNewContent());
        textMessage.setEdited(true);
        daoManager.getMessageDAO().update(textMessage);

        List<Membership> members = daoManager.getMembershipDAO().findAllByField("chat.id", message.getChat().getId());
        MessageEditedEventModel eventModel = new MessageEditedEventModel(
                message.getId(),
                message.getChat().getId(),
                message.getSender().getId(),
                model.getNewContent(),
                LocalDateTime.now()
        );

        for (Membership member : members) {
            try {
                messageEditedEvent.Invoke(getServerSessionManager(), member.getAccount().getId().toString(), eventModel);
            } catch (IOException e) {
                System.err.println("Failed to send message edited event to " + member.getAccount().getId() + ": " + e.getMessage());
            }
        }
        return Ok("Message edited successfully.");
    }

    public RpcResponse<Object> deleteMessage(DeleteMessageInputModel model) {
        Message message = daoManager.getMessageDAO().findById(model.getMessageId());
        if (message == null) {
            return NotFound();
        }
        if (!message.getSender().getId().toString().equals(getCurrentUser().getUserId())) {
            return Forbidden("You can only delete your own messages.");
        }

        daoManager.getMessageDAO().delete(message);

        List<Membership> members = daoManager.getMembershipDAO().findAllByField("chat.id", message.getChat().getId());
        MessageDeletedEventModel eventModel = new MessageDeletedEventModel(
                message.getId(),
                message.getChat().getId(),
                message.getSender().getId()
        );
        for (Membership member : members) {
            try {
                messageDeletedEvent.Invoke(getServerSessionManager(), member.getAccount().getId().toString(), eventModel);
            } catch (IOException e) {
                System.err.println("Failed to send message deleted event to " + member.getAccount().getId() + ": " + e.getMessage());
            }
        }
        return Ok("Message deleted successfully.");
    }

    public void markChatAsRead(MarkChatAsReadInputModel model) {
        UUID readerId = UUID.fromString(getCurrentUser().getUserId());
        Chat chat = daoManager.getChatDAO().findById(model.getChatId());
        if (chat == null) return;

        // 1. Find the latest message in the chat EFFICIENTLY
        Message lastMessage = daoManager.getMessageDAO().findOneByJpql(
                "SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.timestamp DESC",
                query -> {
                    query.setParameter("chatId", chat.getId());
                    query.setMaxResults(1);
                }
        );

        if (lastMessage == null) return; // No messages to mark as read

        // 2. Find the reader's membership EFFICIENTLY
        Membership readerMembership = daoManager.getMembershipDAO().findOneByJpql(
                "SELECT ms FROM Membership ms WHERE ms.chat.id = :chatId AND ms.account.id = :accountId",
                query -> {
                    query.setParameter("chatId", chat.getId());
                    query.setParameter("accountId", readerId);
                }
        );

        if (readerMembership != null) {
            // Only update and send events if there are actually new messages to mark as read.
            if (readerMembership.getLastReadMessage() == null || !lastMessage.getId().equals(readerMembership.getLastReadMessage().getId())) {
                readerMembership.setLastReadMessage(lastMessage);
                daoManager.getMembershipDAO().update(readerMembership);

                // 3. Notify all OTHER members that the user has read messages up to this point
                List<Membership> otherMembers = daoManager.getMembershipDAO().findByJpql(
                        "SELECT ms FROM Membership ms JOIN FETCH ms.account WHERE ms.chat.id = :chatId AND ms.account.id != :readerId",
                        query -> {
                            query.setParameter("chatId", model.getChatId());
                            query.setParameter("readerId", readerId);
                        }
                );

                for (Membership member : otherMembers) {
                    MessageReadEventModel eventModel = new MessageReadEventModel(
                            lastMessage.getId(),
                            model.getChatId(),
                            readerId,
                            LocalDateTime.now()
                    );
                    try {
                        messageReadEvent.Invoke(getServerSessionManager(), member.getAccount().getId().toString(), eventModel);
                    } catch (IOException e) {
                        System.err.println("Failed to send message read event to " + member.getAccount().getId() + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    public void sendTypingStatus(TypingNotificationInputModel model) {
        List<Membership> members = daoManager.getMembershipDAO().findAllByField("chat.id", model.getChatId());
        String senderId = getCurrentUser().getUserId();
        String senderName = getCurrentUser().getFirstName();

        UserIsTypingEventModel eventPayload = new UserIsTypingEventModel(model.getChatId(), UUID.fromString(senderId), senderName, model.isTyping());

        for (Membership member : members) {
            if (!member.getAccount().getId().toString().equals(senderId)) {
                try {
                    userTypingEvent.Invoke(getServerSessionManager(), member.getAccount().getId().toString(), eventPayload);
                } catch (IOException e) {
                    System.err.println("Error sending typing event to user " + member.getAccount().getId() + ": " + e.getMessage());
                }
            }
        }
    }
}