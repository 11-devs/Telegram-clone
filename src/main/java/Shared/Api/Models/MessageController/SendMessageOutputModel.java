package Shared.Api.Models.MessageController;

import java.time.LocalDateTime;
import java.util.UUID;

public class SendMessageOutputModel {
    private UUID messageId;
    private LocalDateTime timestamp;
    private String status;

    public SendMessageOutputModel(UUID messageId, LocalDateTime timestamp, String status) {
        this.messageId = messageId;
        this.timestamp = timestamp;
        this.status = status;
    }

    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}