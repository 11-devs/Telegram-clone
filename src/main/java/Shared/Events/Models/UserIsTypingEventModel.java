package Shared.Events.Models;

import java.util.UUID;

public class UserIsTypingEventModel {
    public UUID chatId;
    public UUID senderId;
    public String senderName;
    public boolean isTyping;

    public UserIsTypingEventModel() {}

    public UserIsTypingEventModel(UUID chatId, UUID senderId, String senderName, boolean isTyping){
        this.chatId = chatId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.isTyping = isTyping;
    }

    public UUID getChatId() { return chatId; }
    public void setChatId(UUID chatId) { this.chatId = chatId; }
    public UUID getSenderId() { return senderId; }
    public void setSenderId(UUID senderId) { this.senderId = senderId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public boolean isTyping() { return isTyping; }
    public void setTyping(boolean typing) { isTyping = typing; }
}