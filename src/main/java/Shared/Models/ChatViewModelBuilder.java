package Shared.Models;

public class ChatViewModelBuilder {
    private final ChatViewModel user = new ChatViewModel();

    public ChatViewModelBuilder displayName(String name) {
        user.setDisplayName(name);
        return this;
    }

    public ChatViewModelBuilder username(String name) {
        user.setUsername(name);
        return this;
    }
    public ChatViewModelBuilder isFromPublicSearch(boolean fromPublicSearch) {
        user.setFromPublicSearch(fromPublicSearch);
        return this;
    }
    public ChatViewModelBuilder isContact(boolean isContact) {
        user.setIsContact(isContact);
        return this;
    }
    public ChatViewModelBuilder subtitle(String subtitle) {
        user.setSubtitle(subtitle);
        return this;
    }
    public ChatViewModelBuilder lastMessage(String message) {
        user.setLastMessage(message);
        return this;
    }
    public ChatViewModelBuilder userMembershipType(String message) {
        user.setUserMembershipType(message);
        return this;
    }

    public ChatViewModelBuilder time(String time) {
        user.setTime(time);
        return this;
    }

    public ChatViewModelBuilder notificationsNumber(String number) {
        user.setNotificationsNumber(number);
        return this;
    }

    public ChatViewModelBuilder avatarId(String id) {
        user.setAvatarId(id);
        return this;
    }

    public ChatViewModelBuilder isOnline(boolean online) {
        user.setOnline(online);
        return this;
    }

    public ChatViewModelBuilder isMuted(boolean muted) {
        user.setMuted(muted);
        return this;
    }

    public ChatViewModelBuilder isPinned(boolean pinned) {
        user.setPinned(pinned);
        return this;
    }

    public ChatViewModelBuilder isVerified(boolean verified) {
        user.setVerified(verified);
        return this;
    }

    public ChatViewModelBuilder userStatus(String status) {
        user.setUserStatus(status);
        return this;
    }

    public ChatViewModelBuilder messageStatus(String status) {
        user.setMessageStatus(status);
        return this;
    }

    public ChatViewModelBuilder isTyping(boolean typing) {
        user.setTyping(typing);
        return this;
    }

    public ChatViewModelBuilder lastSeen(String lastSeen) {
        user.setLastSeen(lastSeen);
        return this;
    }

    public ChatViewModelBuilder type(String type) {
        user.setType(type);
        return this;
    }

    public ChatViewModelBuilder userRole(String role) {
        user.setUserRole(role);
        return this;
    }

    public ChatViewModelBuilder messagePreview(String preview) {
        user.setMessagePreview(preview);
        return this;
    }

    public ChatViewModelBuilder chatId(String id) {
        user.setChatId(id);
        return this;
    }
    public ChatViewModelBuilder userId(String id) {
        user.setUserId(id);
        return this;
    }

    public ChatViewModelBuilder isDraft(boolean draft) {
        user.setDraft(draft);
        return this;
    }

    public ChatViewModelBuilder bio(String bio) {
        user.setBio(bio);
        return this;
    }

    public ChatViewModelBuilder phoneNumber(String phoneNumber) {
        user.setPhoneNumber(phoneNumber);
        return this;
    }

    public ChatViewModelBuilder membersCount(int membersCount) {
        user.setMembersCount(membersCount);
        return this;
    }

    public ChatViewModel build() {
        return user;
    }
}