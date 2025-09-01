package Shared.Models.Interaction;

import Shared.Models.Message.Message;
import jakarta.persistence.*;

@Entity
@Table(name = "reactions")
public class Reaction extends Interaction {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @Column(nullable = false)
    private String reactionEmoji;  // e.g., "👍", "❤️"

    // --- Getters and Setters ---

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public String getReactionEmoji() {
        return reactionEmoji;
    }

    public void setReactionEmoji(String reactionEmoji) {
        this.reactionEmoji = reactionEmoji;
    }
}