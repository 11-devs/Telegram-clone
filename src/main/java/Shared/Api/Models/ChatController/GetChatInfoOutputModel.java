package Shared.Api.Models.ChatController;

import Shared.Models.Chat.ChatType;
import java.util.UUID;

public class GetChatInfoOutputModel {
    private UUID id;
    private ChatType type;
    private String title;
    private String profilePictureId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ChatType getType() {
        return type;
    }

    public void setType(ChatType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProfilePictureId() {
        return profilePictureId;
    }

    public void setProfilePictureId(String profilePictureId) {
        this.profilePictureId = profilePictureId;
    }
}