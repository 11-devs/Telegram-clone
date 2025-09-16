package Shared.Api.Models.ChatController;

import Shared.Models.Chat.ChatType;
import java.util.List;
import java.util.UUID;

public class CreateGroupOutputModel {
    private UUID id;
    private ChatType type;
    private String title;
    private String profilePictureId;
    private String description;
    private UUID creatorId;
    private List<UUID> initialMemberIds;

    public CreateGroupOutputModel() {
    }

    public CreateGroupOutputModel(UUID id, ChatType type, String title, String profilePictureId, String description, UUID creatorId, List<UUID> initialMemberIds) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.profilePictureId = profilePictureId;
        this.description = description;
        this.creatorId = creatorId;
        this.initialMemberIds = initialMemberIds;
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

    public UUID getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(UUID creatorId) {
        this.creatorId = creatorId;
    }

    public List<UUID> getInitialMemberIds() {
        return initialMemberIds;
    }

    public void setInitialMemberIds(List<UUID> initialMemberIds) {
        this.initialMemberIds = initialMemberIds;
    }
}