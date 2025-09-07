package Shared.Api.Models.MessageController;

import Shared.Models.Message.MessageType;

import java.util.UUID;

public class GetMessageOutputModel {
    private UUID messageId;
    private UUID senderId;
    private String senderName;
    private UUID chatId;
    private String timestamp; // Changed from LocalDateTime to String
    private boolean isOutgoing;
    private boolean isEdited;
    private MessageType messageType;
    private String textContent;
    private UUID mediaId;
    private String fileId;
    private String messageStatus; // New field
    private String fileName;
    private long fileSize;
    private String fileExtension;
    private UUID repliedToMessageId;
    private String repliedToSenderName;
    private String repliedToMessageContent;
    private String forwardedFromSenderName;


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public boolean getOutgoing() {
        return isOutgoing;
    }

    public void setOutgoing(boolean outgoing) {
        isOutgoing = outgoing;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
    }

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

    public String getTimestamp() { // Changed getter return type
        return timestamp;
    }

    public void setTimestamp(String timestamp) { // Changed setter parameter type
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

    public UUID getRepliedToMessageId() {
        return repliedToMessageId;
    }

    public void setRepliedToMessageId(UUID repliedToMessageId) {
        this.repliedToMessageId = repliedToMessageId;
    }

    public String getRepliedToSenderName() {
        return repliedToSenderName;
    }

    public void setRepliedToSenderName(String repliedToSenderName) {
        this.repliedToSenderName = repliedToSenderName;
    }

    public String getRepliedToMessageContent() {
        return repliedToMessageContent;
    }

    public void setRepliedToMessageContent(String repliedToMessageContent) {
        this.repliedToMessageContent = repliedToMessageContent;
    }

    public String getForwardedFromSenderName() {
        return forwardedFromSenderName;
    }

    public void setForwardedFromSenderName(String forwardedFromSenderName) {
        this.forwardedFromSenderName = forwardedFromSenderName;
    }
    //</editor-fold>
}