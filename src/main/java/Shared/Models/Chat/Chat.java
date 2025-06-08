package Shared.Models.Chat;

import Shared.Models.Account.*;
import Shared.Models.BaseEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "chats")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Chat extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatType type;

    private String title;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;



    // Getters and setters...
}
