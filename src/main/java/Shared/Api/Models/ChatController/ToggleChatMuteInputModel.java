package Shared.Api.Models.ChatController;

import java.io.Serializable;
import java.util.UUID;

public class ToggleChatMuteInputModel{
    private UUID chatId;
    private boolean isMuted;

    //<editor-fold desc="Getters and Setters">
    public UUID getChatId() {
        return chatId;
    }

    public void setChatId(UUID chatId) {
        this.chatId = chatId;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }
    //</editor-fold>
}