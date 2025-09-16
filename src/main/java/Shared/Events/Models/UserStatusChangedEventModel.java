package Shared.Events.Models;

import java.util.UUID;

public class UserStatusChangedEventModel {
    private UUID userId;

    public UUID getChatId() {
        return chatId;
    }

    public void setChatId(UUID chatId) {
        this.chatId = chatId;
    }

    private UUID chatId;
    private boolean isOnline;
    private String lastSeenTimestamp; // ISO-8601 format

    public UserStatusChangedEventModel(UUID userId,boolean isOnline, String lastSeenTimestamp) {
        this.userId = userId;
        this.isOnline = isOnline;
        this.lastSeenTimestamp = lastSeenTimestamp;
    }

    //<editor-fold desc="Getters and Setters">
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getLastSeenTimestamp() {
        return lastSeenTimestamp;
    }

    public void setLastSeenTimestamp(String lastSeenTimestamp) {
        this.lastSeenTimestamp = lastSeenTimestamp;
    }
    //</editor-fold>
}