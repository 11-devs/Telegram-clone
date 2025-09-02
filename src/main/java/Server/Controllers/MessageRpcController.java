package Server.Controllers;

import Server.DaoManager;
import JSocket2.Protocol.Rpc.*;
import Shared.Api.Models.MessageController.GetMessageByChatInputModel;
import Shared.Api.Models.MessageController.GetMessageByIdInputModel;
import Shared.Models.Message.Message;
import Shared.Models.Message.MessageType;
import Shared.Models.Message.TextMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MessageRpcController extends RpcControllerBase{
    private final DaoManager daoManager;
    public MessageRpcController(DaoManager daoManager){
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

}