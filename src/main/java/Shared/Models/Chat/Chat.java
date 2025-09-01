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

    private String title;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;


    /**
     * Default constructor required by JPA.
     */
    protected Chat() {
    }

    /**
     * Constructor for creating a new Chat instance.
     * Subclasses will typically call this using `super(...)`.
     *
     * @param type The type of the chat (e.g., PRIVATE, GROUP, CHANNEL).
     * @param title The title of the chat. Can be null for private chats initially.
     * @param profilePictureUrl The URL to the chat's profile picture. Can be null.
     */
    protected Chat(ChatType type, String title, String profilePictureUrl) {
        this.type = type;
        this.title = title;
        this.profilePictureUrl = profilePictureUrl;
    }

    // --- Getters ---

    public ChatType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    // --- Setters ---

    public void setType(ChatType type) {
        // You might want to add validation here, e.g., disallowing changing type after creation.
        // For now, a simple setter is fine.
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
}