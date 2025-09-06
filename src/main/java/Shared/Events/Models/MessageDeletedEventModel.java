package Shared.Events.Models;

import java.io.Serializable;
import java.util.UUID;

public class MessageDeletedEventModel implements Serializable {
    private UUID messageId;
    private UUID chatId;

    private boolean lastMessageDeleted;
    private String newLastMessageContent;
    private String newLastMessageTimestamp;

    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public UUID getChatId() {
        return chatId;
    }

    public void setChatId(UUID chatId) {
        this.chatId = chatId;
    }

    public boolean isLastMessageDeleted() {
        return lastMessageDeleted;
    }

    public void setLastMessageDeleted(boolean lastMessageDeleted) {
        this.lastMessageDeleted = lastMessageDeleted;
    }

    public String getNewLastMessageContent() {
        return newLastMessageContent;
    }

    public void setNewLastMessageContent(String newLastMessageContent) {
        this.newLastMessageContent = newLastMessageContent;
    }

    public String getNewLastMessageTimestamp() {
        return newLastMessageTimestamp;
    }

    public void setNewLastMessageTimestamp(String newLastMessageTimestamp) {
        this.newLastMessageTimestamp = newLastMessageTimestamp;
    }
}