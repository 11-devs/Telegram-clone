package Shared.Api.Models.AccountController;

import Shared.Models.Account.AccountStatus;

public class GetAccountInfoOutputModel {
    private String firstName;
    private String lastName;
    private String username;
    private String bio;
    private String phoneNumber;
    private String profilePictureMediaId;
    private String profilePictureFileId;
    private AccountStatus status;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getProfilePictureMediaId() { return profilePictureMediaId; }
    public void setProfilePictureMediaId(String profilePictureMediaId) { this.profilePictureMediaId = profilePictureMediaId; }
    public String getProfilePictureFileId() { return profilePictureFileId; }
    public void setProfilePictureFileId(String profilePictureFileId) { this.profilePictureFileId = profilePictureFileId; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }
}