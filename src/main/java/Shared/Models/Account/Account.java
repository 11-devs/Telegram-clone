package Shared.Models.Account;

import Shared.Models.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "accounts")
@SQLDelete(sql = "UPDATE accounts SET is_deleted = true WHERE id = ? and version = ?")
@Where(clause = "is_deleted = false")
public class Account extends BaseEntity {

    // The Fix: Provide a default value for existing rows
    @Column(nullable = false, columnDefinition = "TEXT NOT NULL DEFAULT ''")
    private String firstName;

    @Column(nullable = true)
    private String lastName;

    @Column(nullable = true)
    private String email;
    @Column(nullable = true)
    private String hashedPassword;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(unique = true)
    private String username;

    @Column(name = "profile_picture_id")
    private String profilePictureId;

    @Column(name = "bio", length = 160)
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private AccountStatus status;


    public Account(String firstName,String lastName, String hashedPassword, String username, String profilePictureId, String bio) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.hashedPassword = hashedPassword;
        this.username = username;
        this.profilePictureId = profilePictureId;
        this.bio = bio;
    }
    public Account() {

    }
    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePictureId() {
        return profilePictureId;
    }

    public void setProfilePictureId(String profilePictureId) {
        this.profilePictureId = profilePictureId;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }
}