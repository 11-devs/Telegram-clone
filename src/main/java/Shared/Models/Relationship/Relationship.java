package Shared.Models.Relationship;

import Shared.Models.Account.Account;
import Shared.Models.BaseEntity;
import jakarta.persistence.*;

@MappedSuperclass // Designates this as a template for subclasses, not an entity itself
public abstract class Relationship extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_account_id", nullable = false)
    private Account owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_account_id", nullable = false)
    private Account target;

    // --- Getters and Setters ---

    public Account getOwner() {
        return owner;
    }

    public void setOwner(Account owner) {
        this.owner = owner;
    }

    public Account getTarget() {
        return target;
    }

    public void setTarget(Account target) {
        this.target = target;
    }
}