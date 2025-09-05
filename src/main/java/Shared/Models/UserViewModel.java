package Shared.Models;

import javafx.beans.property.*;

public class UserViewModel {

    private final StringProperty userName = new SimpleStringProperty();
    private final StringProperty lastMessage = new SimpleStringProperty();
    private final StringProperty time = new SimpleStringProperty();
    private final StringProperty notificationsNumber = new SimpleStringProperty("0");
    private final StringProperty avatarId = new SimpleStringProperty();
    private final BooleanProperty isOnline = new SimpleBooleanProperty(false);
    private final BooleanProperty isMuted = new SimpleBooleanProperty(false);
    private final BooleanProperty isPinned = new SimpleBooleanProperty(false);
    private final BooleanProperty isVerified = new SimpleBooleanProperty(false);
    private final StringProperty userStatus = new SimpleStringProperty("offline");
    private final StringProperty messageStatus = new SimpleStringProperty("none");
    private final BooleanProperty isTyping = new SimpleBooleanProperty(false);
    private final StringProperty lastSeen = new SimpleStringProperty("never");
    private final ObjectProperty<UserType> type = new SimpleObjectProperty<>(UserType.USER);
    private final StringProperty messagePreview = new SimpleStringProperty();
    private final StringProperty userId = new SimpleStringProperty();
    private final ReadOnlyBooleanWrapper isMessageSentByCurrentUser = new ReadOnlyBooleanWrapper(false);
    private final BooleanProperty isDraft = new SimpleBooleanProperty(false);
    private final StringProperty bio = new SimpleStringProperty();
    private final StringProperty phoneNumber = new SimpleStringProperty();
    private final IntegerProperty membersCount = new SimpleIntegerProperty(0);
    private final StringProperty typingUserName = new SimpleStringProperty();

    public UserViewModel() {
        userId.addListener((obs, oldVal, newVal) -> updateIsMessageSentByCurrentUser());
    }
    // Property getters
    public StringProperty userNameProperty() { return userName; }
    public StringProperty lastMessageProperty() { return lastMessage; }
    public StringProperty timeProperty() { return time; }
    public StringProperty notificationsNumberProperty() { return notificationsNumber; }
    public StringProperty avatarIdProperty() { return avatarId; }
    public BooleanProperty isOnlineProperty() { return isOnline; }
    public BooleanProperty isMutedProperty() { return isMuted; }
    public BooleanProperty isPinnedProperty() { return isPinned; }
    public BooleanProperty isVerifiedProperty() { return isVerified; }
    public StringProperty userStatusProperty() { return userStatus; }
    public StringProperty messageStatusProperty() { return messageStatus; }
    public BooleanProperty isTypingProperty() { return isTyping; }
    public StringProperty lastSeenProperty() { return lastSeen; }
    public ObjectProperty<UserType> typeProperty() { return type; }
    public StringProperty messagePreviewProperty() { return messagePreview; }
    public StringProperty userIdProperty() { return userId; }
    public ReadOnlyBooleanProperty isMessageSentByCurrentUserProperty() { return isMessageSentByCurrentUser.getReadOnlyProperty(); }
    public BooleanProperty isDraftProperty() { return isDraft; }
    public StringProperty bioProperty() { return bio; }
    public StringProperty phoneNumberProperty() { return phoneNumber; }
    public IntegerProperty membersCountProperty() { return membersCount; }
    public StringProperty typingUserNameProperty() { return typingUserName; }
    // Getters
    public String getUserName() { return userName.get(); }
    public String getLastMessage() { return lastMessage.get(); }
    public String getTime() { return time.get(); }
    public String getNotificationsNumber() { return notificationsNumber.get(); }
    public String getAvatarId() { return avatarId.get(); }
    public boolean isOnline() { return isOnline.get(); }
    public boolean isMuted() { return isMuted.get(); }
    public boolean isPinned() { return isPinned.get(); }
    public boolean isVerified() { return isVerified.get(); }
    public String getUserStatus() { return userStatus.get(); }
    public String getMessageStatus() { return messageStatus.get(); }
    public boolean isTyping() { return isTyping.get(); }
    public String getLastSeen() { return lastSeen.get(); }
    public UserType getType() { return type.get(); }
    public String getMessagePreview() { return messagePreview.get(); }
    public String getUserId() { return userId.get(); }
    public boolean isMessageSentByCurrentUser() { return isMessageSentByCurrentUser.get(); }
    public boolean isDraft() { return isDraft.get(); }
    public String getBio() { return bio.get(); }
    public String getPhoneNumber() { return phoneNumber.get(); }
    public int getMembersCount() { return membersCount.get(); }
    public String getTypingUserName() { return typingUserName.get(); }
    // Setters
    public void setUserName(String userName) { this.userName.set(userName); }
    public void setLastMessage(String lastMessage) { this.lastMessage.set(lastMessage); }
    public void setTime(String time) { this.time.set(time); }
    public void setNotificationsNumber(String notificationsNumber) { this.notificationsNumber.set(notificationsNumber); }
    public void setAvatarId(String avatarId) { this.avatarId.set(avatarId); }
    public void setOnline(boolean isOnline) { this.isOnline.set(isOnline); }
    public void setMuted(boolean isMuted) { this.isMuted.set(isMuted); }
    public void setPinned(boolean isPinned) { this.isPinned.set(isPinned); }
    public void setVerified(boolean isVerified) { this.isVerified.set(isVerified); }
    public void setUserStatus(String userStatus) { this.userStatus.set(userStatus); }
    public void setMessageStatus(String messageStatus) { this.messageStatus.set(messageStatus); }
    public void setTyping(boolean isTyping) { this.isTyping.set(isTyping); }
    public void setLastSeen(String lastSeen) { this.lastSeen.set(lastSeen); }
    public void setType(UserType type) { this.type.set(type); }
    public void setType(String typeString) { this.type.set(UserType.fromString(typeString)); }
    public void setMessagePreview(String messagePreview) { this.messagePreview.set(messagePreview); }
    public void setUserId(String userId) { this.userId.set(userId); updateIsMessageSentByCurrentUser(); }
    public void setDraft(boolean isDraft) { this.isDraft.set(isDraft); }
    public void setBio(String bio) { this.bio.set(bio); }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber.set(phoneNumber); }
    public void setMembersCount(int membersCount) { this.membersCount.set(membersCount); }
    public void setTypingUserName(String typingUserName) { this.typingUserName.set(typingUserName); }
    // Utility methods
    private void updateIsMessageSentByCurrentUser() {
        String currentUserId = "currentUserId"; // TODO: Replace with real logic
        isMessageSentByCurrentUser.set(currentUserId.equals(getUserId()));
    }

    public void incrementUnreadCount() {
        try {
            int count = Integer.parseInt(getNotificationsNumber());
            setNotificationsNumber(String.valueOf(count + 1));
        } catch (NumberFormatException e) {
            setNotificationsNumber("1");
        }
    }

    public void clearUnreadCount() {
        setNotificationsNumber("0");
    }

    public boolean hasUnreadMessages() {
        try {
            int count = Integer.parseInt(getNotificationsNumber());
            return count > 0;
        } catch (NumberFormatException e) {
            return false; // In case of error, we assume there is no unread message.
        }
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
                ", userId=" + getUserId() +
                ", phoneNumber=" + getPhoneNumber() +
                ", bio=" + getBio() +
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
                ", isDraft=" + isDraft() +
                ", membersCount=" + getMembersCount() +
                '}';
    }
}