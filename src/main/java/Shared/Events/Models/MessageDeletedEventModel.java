package Shared.Events.Models;

import java.util.UUID;

public class MessageDeletedEventModel {
    private UUID messageId;
    private UUID chatId;
    private UUID deleterId;

    public MessageDeletedEventModel() {}

    public MessageDeletedEventModel(UUID messageId, UUID chatId, UUID deleterId) {
        this.messageId = messageId;
        this.chatId = chatId;
        this.deleterId = deleterId;
    }

    public UUID getMessageId() { return messageId; }
    public void setMessageId(UUID messageId) { this.messageId = messageId; }
    public UUID getChatId() { return chatId; }
    public void setChatId(UUID chatId) { this.chatId = chatId; }
    public UUID getDeleterId() { return deleterId; }
    public void setDeleterId(UUID deleterId) { this.deleterId = deleterId; }
}