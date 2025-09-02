package Shared.Api.Models.MembershipController;

import java.util.UUID;

public class LeaveChatInputModel {
    private UUID chatId;
    private UUID userId;

    public UUID getChatId() {
        return chatId;
    }

    public void setChatId(UUID chatId) {
        this.chatId = chatId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}