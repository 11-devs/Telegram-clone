package Shared.Models;

public class UserViewModelBuilder {
    private final UserViewModel user = new UserViewModel();

    public UserViewModelBuilder userName(String name) {
        user.setUserName(name);
        return this;
    }

    public UserViewModelBuilder lastMessage(String message) {
        user.setLastMessage(message);
        return this;
    }
    public UserViewModelBuilder userMembershipType(String message) {
        user.setUserMembershipType(message);
        return this;
    }

    public UserViewModelBuilder time(String time) {
        user.setTime(time);
        return this;
    }

    public UserViewModelBuilder notificationsNumber(String number) {
        user.setNotificationsNumber(number);
        return this;
    }

    public UserViewModelBuilder avatarId(String id) {
        user.setAvatarId(id);
        return this;
    }

    public UserViewModelBuilder isOnline(boolean online) {
        user.setOnline(online);
        return this;
    }

    public UserViewModelBuilder isMuted(boolean muted) {
        user.setMuted(muted);
        return this;
    }

    public UserViewModelBuilder isPinned(boolean pinned) {
        user.setPinned(pinned);
        return this;
    }

    public UserViewModelBuilder isVerified(boolean verified) {
        user.setVerified(verified);
        return this;
    }

    public UserViewModelBuilder userStatus(String status) {
        user.setUserStatus(status);
        return this;
    }

    public UserViewModelBuilder messageStatus(String status) {
        user.setMessageStatus(status);
        return this;
    }

    public UserViewModelBuilder isTyping(boolean typing) {
        user.setTyping(typing);
        return this;
    }

    public UserViewModelBuilder lastSeen(String lastSeen) {
        user.setLastSeen(lastSeen);
        return this;
    }

    public UserViewModelBuilder type(String type) {
        user.setType(type);
        return this;
    }

    public UserViewModelBuilder userRole(String role) {
        user.setUserRole(role);
        return this;
    }

    public UserViewModelBuilder messagePreview(String preview) {
        user.setMessagePreview(preview);
        return this;
    }

    public UserViewModelBuilder userId(String id) {
        user.setUserId(id);
        return this;
    }

    public UserViewModelBuilder isDraft(boolean draft) {
        user.setDraft(draft);
        return this;
    }

    public UserViewModelBuilder bio(String bio) {
        user.setBio(bio);
        return this;
    }

    public UserViewModelBuilder phoneNumber(String phoneNumber) {
        user.setPhoneNumber(phoneNumber);
        return this;
    }

    public UserViewModelBuilder membersCount(int membersCount) {
        user.setMembersCount(membersCount);
        return this;
    }

    public UserViewModel build() {
        return user;
    }
}