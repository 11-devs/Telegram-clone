package Shared.Models.Setting;

import jakarta.persistence.*;

@Entity
@Table(name = "privacy_settings")
public class PrivacySetting extends Setting {
    public enum LastSeenVisibility {
        EVERYONE,
        CONTACTS_ONLY,
        NOBODY
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "last_seen_visibility", nullable = false)
    private LastSeenVisibility lastSeenVisibility = LastSeenVisibility.EVERYONE;

    @Column(name = "profile_photo_visible")
    private Boolean profilePhotoVisible = true;
}