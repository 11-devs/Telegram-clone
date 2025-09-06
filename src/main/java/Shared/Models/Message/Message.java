package Shared.Models.Message;

import Shared.Models.Account.Account;
import Shared.Models.BaseEntity;
import Shared.Models.Chat.Chat;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "messages")
@SQLDelete(sql = "UPDATE messages SET is_deleted = true WHERE id = ? and version = ?")
@Where(clause = "is_deleted = false")
public abstract class Message extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id")
    private Account sender;

    @ManyToOne(optional = false)
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "is_edited")
    private boolean isEdited;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    // --- Start of added setters ---
    public void setSender(Account sender) {
        this.sender = sender;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
    // --- End of added setters ---

    // --- Getters ---
    public Account getSender() {
        return sender;
    }

    public Chat getChat() {
        return chat;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public MessageType getType() {
        return type;
    }
}