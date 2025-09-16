package Shared.Api.Models.ViewController;

import java.util.UUID;

public class GetMessageViewsInputModel {
    private UUID messageId;

    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }
}