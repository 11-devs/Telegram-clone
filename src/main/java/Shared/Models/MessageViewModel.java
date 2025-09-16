package Shared.Models;

public class MessageViewModel {
    private final String text;
    private final boolean isOutgoing;
    private final String timestamp;
    private String status;

    public MessageViewModel(String text, boolean isOutgoing, String timestamp, String status) {
        this.text = text;
        this.isOutgoing = isOutgoing;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters and setters
    public String getText() { return text; }
    public boolean isOutgoing() { return isOutgoing; }
    public String getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }}
