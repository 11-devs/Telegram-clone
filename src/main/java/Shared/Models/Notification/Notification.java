package Shared.Models.Notification;

import Shared.Models.Account.Account;
import Shared.Models.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_receiver_id", columnList = "receiver_id")
})
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Account receiver;

    @Column(name = "is_seen", nullable = false)
    private boolean isSeen = false;

    // --- Getters and Setters ---

    public Account getReceiver() {
        return receiver;
    }

    public void setReceiver(Account receiver) {
        this.receiver = receiver;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}