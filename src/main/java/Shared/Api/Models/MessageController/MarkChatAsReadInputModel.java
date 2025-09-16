package Shared.Api.Models.MessageController;

import java.util.UUID;

public class MarkChatAsReadInputModel {
    private UUID chatId;

    public UUID getChatId() {
        return chatId;
    }

    public void setChatId(UUID chatId) {
        this.chatId = chatId;
    }
}