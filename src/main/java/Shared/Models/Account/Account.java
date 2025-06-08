package Shared.Models.Account;

import Shared.Models.BaseEntity;
import Shared.Models.Chat.Chat;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "accounts")
public class Account extends BaseEntity {

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private String hashedPassword;

    @Column(unique = true, nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "bio", length = 160)
    private String bio;
}
