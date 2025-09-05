package Shared.Api.Models.MessageController;

import java.util.UUID;

public class DeleteMessageInputModel {
    private UUID messageId;
    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }
}