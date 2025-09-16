package Shared.Models.Relationship;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "muted_users",
        uniqueConstraints = @UniqueConstraint(columnNames = {"owner_account_id", "target_account_id"}),
        indexes = @Index(name = "idx_mute_owner_id", columnList = "owner_account_id"))
public class Mute extends Relationship {
    // Inherits 'owner' and 'target'
    // 'owner' is the user who is muting the chat.
    // 'target' is the other user in the private chat being muted.
}