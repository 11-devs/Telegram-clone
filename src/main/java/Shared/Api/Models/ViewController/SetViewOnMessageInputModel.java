package Shared.Api.Models.ViewController;

import java.util.UUID;

public class SetViewOnMessageInputModel {
    private UUID messageId;
    private UUID userId;

    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}