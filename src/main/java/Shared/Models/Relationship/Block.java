package Shared.Models.Relationship;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "blocked_users",
        uniqueConstraints = @UniqueConstraint(columnNames = {"owner_account_id", "target_account_id"}),
        indexes = @Index(name = "idx_block_owner_id", columnList = "owner_account_id"))
public class Block extends Relationship {
    // Inherits 'owner' and 'target'
    // 'owner' is the user who is blocking.
    // 'target' is the user who is being blocked.
}