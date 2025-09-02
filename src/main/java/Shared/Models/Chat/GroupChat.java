package Shared.Models.Chat;

import Shared.Models.Account.Account;
import Shared.Models.Membership.Membership;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "groups")
public class GroupChat extends Chat {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private Account createdBy;

    @Column
    private String description;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Membership> members;

    protected GroupChat() {
        super();
        super.setType(ChatType.GROUP);
    }

    public GroupChat(String title, String profilePictureId, Account createdBy, String description) {
        super(ChatType.GROUP, title, profilePictureId);
        this.createdBy = createdBy;
        this.description = description;
    }
}