package Shared.Models.Chat;

import Shared.Models.Account.Account;
import jakarta.persistence.*;

@Entity
@Table(name = "saved_messages")
public class SavedMessages extends Chat {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Account owner;

    protected SavedMessages() {
        super();
        super.setType(ChatType.SAVED_MESSAGES);
    }

    public SavedMessages(Account owner) {
        super(ChatType.SAVED_MESSAGES, null, null);
        this.owner = owner;
    }
}