package Shared.Models.Notification;

import Shared.Models.Account.Account;
import Shared.Models.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "notifications")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Account receiver;

    @Column(nullable = false)
    private boolean isSeen = false;
}