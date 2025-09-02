package Shared.Models.Chat;

import Shared.Models.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "chats")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Chat extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatType type;

    @Column
    private String title;

    @Column(name = "profile_picture_id")
    private String profilePictureId;


    protected Chat() {
    }

    protected Chat(ChatType type, String title, String profilePictureId) {
        this.type = type;
        this.title = title;
        this.profilePictureId = profilePictureId;
    }

    // --- Getters ---

    public ChatType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getProfilePictureId() {
        return profilePictureId;
    }

    // --- Setters ---

    public void setType(ChatType type) {
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setProfilePictureId(String profilePictureId) {
        this.profilePictureId = profilePictureId;
    }
}