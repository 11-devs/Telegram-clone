package Shared.Models.Folder;

import Shared.Models.Account.Account;
import Shared.Models.BaseEntity;
import Shared.Models.Chat.Chat;
import jakarta.persistence.*;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "chat_folders")
public class Folder extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Account owner;

    @ManyToMany
    @JoinTable(
            name = "chat_folder_chats",
            joinColumns = @JoinColumn(name = "folder_id"),
            inverseJoinColumns = @JoinColumn(name = "chat_id")
    )
    private Set<Chat> chats = new HashSet<>();

    // Getters and setters...
}
