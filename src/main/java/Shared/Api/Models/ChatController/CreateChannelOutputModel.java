package Shared.Api.Models.ChatController;

import Shared.Models.Chat.ChatType;
import java.util.UUID;

public class CreateChannelOutputModel {
    private UUID id;
    private ChatType type;
    private String title;
    private String profilePictureId;
    private String description;
    private boolean isPublic;
    private UUID creatorId;

    public CreateChannelOutputModel() {
    }

    public CreateChannelOutputModel(UUID id, ChatType type, String title, String profilePictureId, String description, boolean isPublic, UUID creatorId) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.profilePictureId = profilePictureId;
        this.description = description;
        this.isPublic = isPublic;
        this.creatorId = creatorId;
    }

    // Getters and Setters
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(UUID creatorId) {
        this.creatorId = creatorId;
    }
}