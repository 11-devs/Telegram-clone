package Server.Controllers;

import JSocket2.Protocol.Rpc.RpcControllerBase;
import JSocket2.Protocol.Rpc.RpcResponse;
import Server.DaoManager;
import Shared.Api.Models.MessageController.GetMessageByChatInputModel;
import Shared.Api.Models.MessageController.GetMessageByIdInputModel;
import Shared.Api.Models.MessageController.SendMessageInputModel;
import Shared.Api.Models.MessageController.SendMessageOutputModel;
import Shared.Models.Account.Account;
import Shared.Models.Chat.Chat;
import Shared.Models.Media.Media;
import Shared.Models.Message.MediaMessage;
import Shared.Models.Message.Message;
import Shared.Models.Message.MessageType;
import Shared.Models.Message.TextMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MessageRpcController extends RpcControllerBase {
    private final DaoManager daoManager;

    public MessageRpcController(DaoManager daoManager) {
        this.daoManager = daoManager;
    }

    public static class GetMessageOutputModel {
        private UUID messageId;
        private UUID senderId;
        private String senderName;
        private UUID chatId;
        private LocalDateTime timestamp;
        private boolean isEdited;
        private MessageType messageType;
        private String textContent;
        private UUID mediaId;

        //<editor-fold desc="Getters and Setters">
        public UUID getMessageId() {
            return messageId;
        }

        public void setMessageId(UUID messageId) {
            this.messageId = messageId;
        }

        public UUID getSenderId() {
            return senderId;
        }

        public void setSenderId(UUID senderId) {
            this.senderId = senderId;
        }

        public String getSenderName() {
            return senderName;
        }

        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }

        public UUID getChatId() {
            return chatId;
        }

        public void setChatId(UUID chatId) {
            this.chatId = chatId;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public boolean isEdited() {
            return isEdited;
        }

        public void setEdited(boolean edited) {
            isEdited = edited;
        }

        public MessageType getMessageType() {
            return messageType;
        }

        public void setMessageType(MessageType messageType) {
            this.messageType = messageType;
        }

        public String getTextContent() {
            return textContent;
        }

        public void setTextContent(String textContent) {
            this.textContent = textContent;
        }

        public UUID getMediaId() {
            return mediaId;
        }

        public void setMediaId(UUID mediaId) {
            this.mediaId = mediaId;
        }
        //</editor-fold>
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
        output.setTimestamp(message.getTimestamp());
        output.setEdited(message.isEdited());
        output.setMessageType(message.getType());

        if (message instanceof TextMessage) {
            output.setTextContent(((TextMessage) message).getTextContent());
        } else if (message instanceof MediaMessage) {
            Media media = ((MediaMessage) message).getMedia();
            if (media != null) {
                output.setMediaId(media.getId());
            }
        }

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

    public RpcResponse<Object> getMessagesByChat(GetMessageByChatInputModel model) {
        List<Message> messages = daoManager.getMessageDAO().findAllByField("chat.id", model.getChatId());
        if (messages == null || messages.isEmpty()) {
            return Ok(List.of());
        }
        List<GetMessageOutputModel> outputList = messages.stream()
                .map(this::mapMessageToOutputModel)
                .collect(Collectors.toList());
        return Ok(outputList);
    }

    public RpcResponse<Object> sendMessage(SendMessageInputModel model) {
        Account sender = daoManager.getAccountDAO().findById(model.getSenderId());
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

        SendMessageOutputModel output = new SendMessageOutputModel(
                newMessage.getId(),
                newMessage.getTimestamp(),
                "Message sent successfully"
        );

        return Ok(output);
    }
}