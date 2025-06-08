package Shared.Models.Setting;

import Shared.Models.Account.Account;
import Shared.Models.BaseEntity;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "settings")
public abstract class Setting extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // Getters and setters
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
}
