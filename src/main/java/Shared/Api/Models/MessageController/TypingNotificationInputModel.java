package Shared.Api.Models.MessageController;

import java.util.UUID;

public class TypingNotificationInputModel {
    private UUID chatId;
    private boolean isTyping;
    public UUID getChatId() {
        return chatId;
    }

    public void setChatId(UUID chatId) {
        this.chatId = chatId;
    }

    public boolean isTyping() {
        return isTyping;
    }

    public void setTyping(boolean typing) {
        isTyping = typing;
    }
}