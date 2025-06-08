package Shared.Models.Interaction;

import Shared.Models.Account.Account;
import Shared.Models.BaseEntity;
import jakarta.persistence.*;

@MappedSuperclass
public abstract class Interaction extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
}
