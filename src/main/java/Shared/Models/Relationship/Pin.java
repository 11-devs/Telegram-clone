package Shared.Models.Relationship;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "pinned_chats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"owner_account_id", "target_account_id"}),
        indexes = @Index(name = "idx_pin_owner_id", columnList = "owner_account_id"))
public class Pin extends Relationship {
    // Inherits 'owner' and 'target'
    // 'owner' is the user who is pinning the chat.
    // 'target' is the other user in the private chat being pinned.
}