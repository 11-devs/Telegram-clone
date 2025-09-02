package Shared.Api.Models.ChatController;

import java.util.UUID;

public class GetChatByIdInputModel {
    private UUID chatId;

    public UUID getChatId() {
        return chatId;
    }

    public void setChatId(UUID chatId) {
        this.chatId = chatId;
    }
}