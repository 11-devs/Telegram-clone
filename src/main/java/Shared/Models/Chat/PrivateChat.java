package Shared.Models.Chat;

import Shared.Models.Account.Account;
import jakarta.persistence.*;

@Entity
@Table(name = "private_chats")
public class PrivateChat extends Chat {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private Account user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private Account user2;

    protected PrivateChat() {
        super();
        super.setType(ChatType.PRIVATE);
    }

    public PrivateChat(Account user1, Account user2) {
        super(ChatType.PRIVATE, null, null);
        this.user1 = user1;
        this.user2 = user2;
    }
}