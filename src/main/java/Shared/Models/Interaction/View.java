package Shared.Models.Interaction;

import Shared.Models.Message.Message;
import jakarta.persistence.*;

@Entity
@Table(name = "views")
public class View extends Interaction {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    // --- Getters and Setters ---

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}