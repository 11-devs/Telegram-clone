package Shared.Api.Models.MessageController;

import Shared.Models.Message.MessageType;
import java.util.UUID;

public class SendMessageInputModel {
    private UUID senderId;
    private UUID chatId;
    private MessageType messageType;
    private String textContent;
    private UUID mediaId; // Added for Media Messages

    public UUID getRepliedToMessageId() {
        return repliedToMessageId;
    }

    public void setRepliedToMessageId(UUID repliedToMessageId) {
        this.repliedToMessageId = repliedToMessageId;
    }

    private UUID repliedToMessageId;
    //<editor-fold desc="Getters and Setters">
    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public UUID getChatId() {
        return chatId;
    }

    public void setChatId(UUID chatId) {
        this.chatId = chatId;
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