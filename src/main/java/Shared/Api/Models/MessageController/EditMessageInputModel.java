package Shared.Api.Models.MessageController;

import java.util.UUID;

public class EditMessageInputModel {
    private UUID messageId;
    private String newContent;
    public UUID getMessageId() {
        return messageId;
    }
    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }
    public String getNewContent() {
        return newContent;
    }
    public void setNewContent(String newContent) {
        this.newContent = newContent;
    }
}