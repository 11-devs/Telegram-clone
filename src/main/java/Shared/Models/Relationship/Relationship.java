package Shared.Models.Relationship;

import Shared.Models.Account.Account;
import Shared.Models.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
//@Inheritance(strategy = InheritanceType.JOINED)
@MappedSuperclass
@Table(name = "relationships")
public abstract class Relationship extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_account_id", nullable = false)
    private Account owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_account_id", nullable = false)
    private Account target;
}