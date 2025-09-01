package Shared.Models.Message;

import Shared.Models.Account.Account;
import Shared.Models.BaseEntity;
import Shared.Models.Chat.Chat;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "messages")
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
}
