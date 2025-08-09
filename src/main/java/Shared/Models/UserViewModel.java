package Shared.Models;

import javafx.beans.property.*;

public class UserViewModel {
    private final StringProperty userName = new SimpleStringProperty();
    private final StringProperty lastMessage = new SimpleStringProperty();
    private final StringProperty time = new SimpleStringProperty();
    private final StringProperty notificationsNumber = new SimpleStringProperty("0");
    private final StringProperty avatarPath = new SimpleStringProperty();
    private final BooleanProperty isOnline = new SimpleBooleanProperty(false);
    private final BooleanProperty isMuted = new SimpleBooleanProperty(false);
    private final BooleanProperty isPinned = new SimpleBooleanProperty(false);
    private final BooleanProperty isVerified = new SimpleBooleanProperty(false);
    private final StringProperty userStatus = new SimpleStringProperty("offline");
    private final StringProperty messageStatus = new SimpleStringProperty("none");
    private final BooleanProperty isTyping = new SimpleBooleanProperty(false);
    private final StringProperty lastSeen = new SimpleStringProperty("never");
    private final StringProperty type = new SimpleStringProperty("user");
    private final StringProperty messagePreview = new SimpleStringProperty();
    private final StringProperty userId = new SimpleStringProperty();
    private final ReadOnlyBooleanWrapper isMessageSentByCurrentUser = new ReadOnlyBooleanWrapper(false);
    private final BooleanProperty isDraft = new SimpleBooleanProperty(false);

    public UserViewModel() {
        userId.addListener((obs, oldVal, newVal) -> updateIsMessageSentByCurrentUser());
    }

    // Property getters
    public StringProperty userNameProperty() { return userName; }
    public StringProperty lastMessageProperty() { return lastMessage; }
    public StringProperty timeProperty() { return time; }
    public StringProperty notificationsNumberProperty() { return notificationsNumber; }
    public StringProperty avatarPathProperty() { return avatarPath; }
    public BooleanProperty isOnlineProperty() { return isOnline; }
    public BooleanProperty isMutedProperty() { return isMuted; }
    public BooleanProperty isPinnedProperty() { return isPinned; }
    public BooleanProperty isVerifiedProperty() { return isVerified; }
    public StringProperty userStatusProperty() { return userStatus; }
    public StringProperty messageStatusProperty() { return messageStatus; }
    public BooleanProperty isTypingProperty() { return isTyping; }
    public StringProperty lastSeenProperty() { return lastSeen; }
    public StringProperty typeProperty() { return type; }
    public StringProperty messagePreviewProperty() { return messagePreview; }
    public StringProperty userIdProperty() { return userId; }
    public ReadOnlyBooleanProperty isMessageSentByCurrentUserProperty() { return isMessageSentByCurrentUser.getReadOnlyProperty(); }
    public BooleanProperty isDraftProperty() { return isDraft; }

    // Getters
    public String getUserName() { return userName.get(); }
    public String getLastMessage() { return lastMessage.get(); }
    public String getTime() { return time.get(); }
    public String getNotificationsNumber() { return notificationsNumber.get(); }
    public String getAvatarPath() { return avatarPath.get(); }
    public boolean isOnline() { return isOnline.get(); }
    public boolean isMuted() { return isMuted.get(); }
    public boolean isPinned() { return isPinned.get(); }
    public boolean isVerified() { return isVerified.get(); }
    public String getUserStatus() { return userStatus.get(); }
    public String getMessageStatus() { return messageStatus.get(); }
    public boolean isTyping() { return isTyping.get(); }
    public String getLastSeen() { return lastSeen.get(); }
    public String getType() { return type.get(); }
    public String getMessagePreview() { return messagePreview.get(); }
    public String getUserId() { return userId.get(); }
    public boolean isMessageSentByCurrentUser() { return isMessageSentByCurrentUser.get(); }
    public boolean isDraft() { return isDraft.get(); }

    // Setters
    public void setUserName(String userName) { this.userName.set(userName); }
    public void setLastMessage(String lastMessage) { this.lastMessage.set(lastMessage); }
    public void setTime(String time) { this.time.set(time); }
    public void setNotificationsNumber(String notificationsNumber) { this.notificationsNumber.set(notificationsNumber); }
    public void setAvatarPath(String avatarPath) { this.avatarPath.set(avatarPath); }
    public void setOnline(boolean isOnline) { this.isOnline.set(isOnline); }
    public void setMuted(boolean isMuted) { this.isMuted.set(isMuted); }
    public void setPinned(boolean isPinned) { this.isPinned.set(isPinned); }
    public void setVerified(boolean isVerified) { this.isVerified.set(isVerified); }
    public void setUserStatus(String userStatus) { this.userStatus.set(userStatus); }
    public void setMessageStatus(String messageStatus) { this.messageStatus.set(messageStatus); }
    public void setTyping(boolean isTyping) { this.isTyping.set(isTyping); }
    public void setLastSeen(String lastSeen) { this.lastSeen.set(lastSeen); }
    public void setType(String type) { this.type.set(type); }
    public void setMessagePreview(String messagePreview) { this.messagePreview.set(messagePreview); }
    public void setUserId(String userId) { this.userId.set(userId); updateIsMessageSentByCurrentUser(); }
    public void setDraft(boolean isDraft) { this.isDraft.set(isDraft); }

    // Utility methods
    private void updateIsMessageSentByCurrentUser() {
        String currentUserId = "currentUserId"; // TODO: Replace with real logic
        isMessageSentByCurrentUser.set(currentUserId.equals(getUserId()));
    }

    public void incrementNotifications() {
        try {
            int count = Integer.parseInt(getNotificationsNumber());
            setNotificationsNumber(String.valueOf(count + 1));
        } catch (NumberFormatException e) {
            setNotificationsNumber("1");
        }
    }

    public void clearNotifications() {
        setNotificationsNumber("0");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserViewModel that)) return false;
        return getUserId() != null && getUserId().equals(that.getUserId());
    }

    @Override
    public int hashCode() {
        return getUserId() != null ? getUserId().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "UserViewModel{" +
                "userName=" + getUserName() +
                ", lastMessage=" + getLastMessage() +
                ", time=" + getTime() +
                ", notificationsNumber=" + getNotificationsNumber() +
                ", isOnline=" + isOnline() +
                ", isMuted=" + isMuted() +
                ", isPinned=" + isPinned() +
                ", isVerified=" + isVerified() +
                ", userStatus=" + getUserStatus() +
                ", messageStatus=" + getMessageStatus() +
                ", isTyping=" + isTyping() +
                ", lastSeen=" + getLastSeen() +
                ", type=" + getType() +
                ", messagePreview=" + getMessagePreview() +
                ", userId=" + getUserId() +
                ", isDraft=" + isDraft() +
                '}';
    }
}
