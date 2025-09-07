package Shared.Events.Models;

import java.util.UUID;

public class ChatInfoChangedEventModel {
    private UUID chatId;
    private String newTitle;
    private String newProfilePictureId; // Can be null if not changed

    public ChatInfoChangedEventModel(UUID chatId, String newTitle, String newProfilePictureId) {
        this.chatId = chatId;
        this.newTitle = newTitle;
        this.newProfilePictureId = newProfilePictureId;
    }
    public UUID getChatId() {
        return chatId;
    }

    public void setChatId(UUID chatId) {
        this.chatId = chatId;
    }

    public String getNewTitle() {
        return newTitle;
    }

    public void setNewTitle(String newTitle) {
        this.newTitle = newTitle;
    }

    public String getNewProfilePictureId() {
        return newProfilePictureId;
    }

    public void setNewProfilePictureId(String newProfilePictureId) {
        this.newProfilePictureId = newProfilePictureId;
    }
}