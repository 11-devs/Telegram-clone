package Shared.Events.Models;

import java.time.LocalDateTime;
import java.util.UUID;

public class MessageEditedEventModel {
    private UUID messageId;
    private UUID chatId;
    private UUID editorId;
    private String newContent;
    private String timestamp; // Changed from LocalDateTime to String for serialization

    public MessageEditedEventModel() {}

    public MessageEditedEventModel(UUID messageId, UUID chatId, UUID editorId, String newContent, LocalDateTime timestamp) {
        this.messageId = messageId;
        this.chatId = chatId;
        this.editorId = editorId;
        this.newContent = newContent;
        this.timestamp = timestamp != null ? timestamp.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }

    public UUID getMessageId() { return messageId; }
    public void setMessageId(UUID messageId) { this.messageId = messageId; }
    public UUID getChatId() { return chatId; }
    public void setChatId(UUID chatId) { this.chatId = chatId; }
    public UUID getEditorId() { return editorId; }
    public void setEditorId(UUID editorId) { this.editorId = editorId; }
    public String getNewContent() { return newContent; }
    public void setNewContent(String newContent) { this.newContent = newContent; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}