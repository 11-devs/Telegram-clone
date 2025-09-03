package Shared.Api.Models.ContactController;

import Shared.Models.Account.AccountStatus;
import java.util.UUID;

public class ContactInfo {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String username;
    private AccountStatus status;
    private String profilePictureId;

    public ContactInfo(UUID userId, String firstName, String lastName, String username, AccountStatus status, String profilePictureId) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.status = status;
        this.profilePictureId = profilePictureId;
    }
    // Getters and Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }
    public String getProfilePictureId() { return profilePictureId; }
    public void setProfilePictureId(String profilePictureId) { this.profilePictureId = profilePictureId; }
}