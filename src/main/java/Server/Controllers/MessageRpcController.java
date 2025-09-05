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
import java.time.format.DateTimeFormatter; // Import for formatting
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
        // Convert LocalDateTime to String for the output model
        output.setTimestamp(message.getTimestamp() != null ? message.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        output.setEdited(message.isEdited());
        output.setMessageType(message.getType());
        output.setOutgoing(Objects.equals(output.getSenderId().toString(), getCurrentUser().getUserId()));
        if (message instanceof TextMessage) {
            output.setTextContent(((TextMessage) message).getTextContent());
        } else if (message instanceof MediaMessage) {
            Media media = ((MediaMessage) message).getMedia();
            if (media != null) {
                output.setMediaId(media.getId());
                output.setFileId(media.getFileId());
            }
        }

        return output;
    }

    // Corrected method signature and logic for getMessageById
    // Changed return type from Object to GetMessageOutputModel
    public RpcResponse<Object> getMessageById(GetMessageByIdInputModel model) {
        Message message = daoManager.getMessageDAO().findById(model.getMessageId()); // Used model.getMessageId()
        if (message == null) {
            return BadRequest("Message not found.");
        }
        GetMessageOutputModel output = mapMessageToOutputModel(message);
        return Ok(output);
    }

    // Return type already correct
    public RpcResponse<List<GetMessageOutputModel>> getMessagesByChat(GetMessageByChatInputModel model) {
        List<Message> messages = daoManager.getMessageDAO().findAllByField("chat.id", model.getChatId());
        if (messages == null || messages.isEmpty()) {
            return Ok(List.of());
        }
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

        if (chat.getType() == Shared.Models.Chat.ChatType.PRIVATE) {
            List<Membership> members = daoManager.getMembershipDAO().findAllByField("chat.id", model.getChatId());
            members.stream()
                    .map(Membership::getAccount)
                    .filter(account -> !account.getId().equals(readerId))
                    .findFirst()
                    .ifPresent(otherUser -> {
                        MessageReadEventModel eventModel = new MessageReadEventModel(
                                null,
                                model.getChatId(),
                                readerId,
                                LocalDateTime.now()
                        );
                        try {
                            messageReadEvent.Invoke(getServerSessionManager(), otherUser.getId().toString(), eventModel);
                        } catch (IOException e) {
                            System.err.println("Failed to send message read event to " + otherUser.getId() + ": " + e.getMessage());
                        }
                    });
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