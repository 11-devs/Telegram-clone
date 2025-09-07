package Shared.Api.Models.MembershipController;

import java.util.UUID;

public class MemberInfo {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String username;
    private String profilePictureFileId;
    private String role;
    private boolean isOnline;
    private String lastSeen;

    public MemberInfo() {}

    public MemberInfo(UUID userId, String firstName, String lastName, String username, String profilePictureFileId, String role, boolean isOnline, String lastSeen) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.profilePictureFileId = profilePictureFileId;
        this.role = role;
        this.isOnline = isOnline;
        this.lastSeen = lastSeen;
    }
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePictureFileId() {
        return profilePictureFileId;
    }

    public void setProfilePictureFileId(String profilePictureFileId) {
        this.profilePictureFileId = profilePictureFileId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }
    //</editor-fold>
}