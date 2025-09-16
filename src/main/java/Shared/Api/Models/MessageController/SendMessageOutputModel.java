package Shared.Api.Models.MessageController;

import java.time.LocalDateTime;
import java.util.UUID;
import java.time.format.DateTimeFormatter; // Import for formatting

public class SendMessageOutputModel {
    private UUID messageId;
    private String timestamp; // Changed from LocalDateTime to String
    private String status;

    public SendMessageOutputModel(UUID messageId, LocalDateTime timestamp, String status) {
        this.messageId = messageId;
        // Convert LocalDateTime to String using ISO_LOCAL_DATE_TIME format
        this.timestamp = timestamp != null ? timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
        this.status = status;
    }

    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public String getTimestamp() { // Changed getter return type
        return timestamp;
    }

    public void setTimestamp(String timestamp) { // Changed setter parameter type
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}