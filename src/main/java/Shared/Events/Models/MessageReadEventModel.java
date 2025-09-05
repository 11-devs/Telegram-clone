package Shared.Events.Models;

import java.time.LocalDateTime;
import java.util.UUID;

public class MessageReadEventModel {
    private UUID messageId;
    private UUID chatId;
    private UUID readerId;
    private String readTimestamp; // Changed from LocalDateTime to String

    public MessageReadEventModel() {}

    public MessageReadEventModel(UUID messageId, UUID chatId, UUID readerId, LocalDateTime readTimestamp) {
        this.messageId = messageId;
        this.chatId = chatId;
        this.readerId = readerId;
        this.readTimestamp = readTimestamp != null ? readTimestamp.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }

    public UUID getMessageId() { return messageId; }
    public void setMessageId(UUID messageId) { this.messageId = messageId; }
    public UUID getChatId() { return chatId; }
    public void setChatId(UUID chatId) { this.chatId = chatId; }
    public UUID getReaderId() { return readerId; }
    public void setReaderId(UUID readerId) { this.readerId = readerId; }
    public String getReadTimestamp() { return readTimestamp; }
    public void setReadTimestamp(String readTimestamp) { this.readTimestamp = readTimestamp; }
}