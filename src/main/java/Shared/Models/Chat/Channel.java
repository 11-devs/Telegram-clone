package Shared.Models.Chat;

import Shared.Models.Account.Account;
import Shared.Models.Membership.Membership;
import jakarta.persistence.*;
import java.util.List;


@Entity
@Table(name = "channels")
public class Channel extends Chat {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private Account createdBy;

    @Column
    private String description;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Membership> members;

    protected Channel() {
        super();
        super.setType(ChatType.CHANNEL);
    }

    public Channel(String title, String profilePictureId, Account createdBy, String description, boolean isPublic) {
        super(ChatType.CHANNEL, title, profilePictureId);
        this.createdBy = createdBy;
        this.description = description;
        this.isPublic = isPublic;
    }
}