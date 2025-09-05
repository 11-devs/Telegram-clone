package Shared.Events.Models;

import java.time.LocalDateTime;
import java.util.UUID;

public class MessageDeliveredEventModel {
    private UUID messageId;
    private UUID chatId;
    private UUID receiverId;
    private String deliveredTimestamp; // Changed from LocalDateTime to String

    public MessageDeliveredEventModel() {}

    public MessageDeliveredEventModel(UUID messageId, UUID chatId, UUID receiverId, LocalDateTime deliveredTimestamp) {
        this.messageId = messageId;
        this.chatId = chatId;
        this.receiverId = receiverId;
        this.deliveredTimestamp = deliveredTimestamp != null ? deliveredTimestamp.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }

    public UUID getMessageId() { return messageId; }
    public void setMessageId(UUID messageId) { this.messageId = messageId; }
    public UUID getChatId() { return chatId; }
    public void setChatId(UUID chatId) { this.chatId = chatId; }
    public UUID getReceiverId() { return receiverId; }
    public void setReceiverId(UUID receiverId) { this.receiverId = receiverId; }
    public String getDeliveredTimestamp() { return deliveredTimestamp; }
    public void setDeliveredTimestamp(String deliveredTimestamp) { this.deliveredTimestamp = deliveredTimestamp; }
}