package Shared.Models.Setting;

import jakarta.persistence.*;

@Entity
@Table(name = "notification_settings")
public class NotificationSetting extends Setting {
    @Column(name = "mute_notifications")
    private Boolean muteNotifications = false;

    @Column(name = "show_message_preview")
    private Boolean showMessagePreview = true;

    // Getters and setters

    public Boolean getMuteNotifications() {
        return muteNotifications;
    }

    public void setMuteNotifications(Boolean muteNotifications) {
        this.muteNotifications = muteNotifications;
    }

    public Boolean getShowMessagePreview() {
        return showMessagePreview;
    }

    public void setShowMessagePreview(Boolean showMessagePreview) {
        this.showMessagePreview = showMessagePreview;
    }
}
