package Shared.Models.Chat;

import Shared.Models.Account.Account;
import Shared.Models.BaseEntity;
import Shared.Models.Membership.MembershipType;
import Shared.Models.Message.Message;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "memberships")
public class Membership extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(optional = false)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipType type;

    @Column(name = "join_date", nullable = false)
    private LocalDateTime joinDate;

    @Column(name = "is_muted", nullable = false)
    private boolean isMuted = false;

    @Column(name = "is_banned", nullable = false)
    private boolean isBanned = false;

    @Column(name = "leave_date")
    private LocalDateTime leaveDate;

    @ManyToOne
    @JoinColumn(name = "invited_by_account_id")
    private Account invitedBy;

    // Optional: Track last read message per user in this chat
    @ManyToOne
    @JoinColumn(name = "last_read_message_id")
    private Message lastReadMessage;
}
