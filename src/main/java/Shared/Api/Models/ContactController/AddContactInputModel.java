package Shared.Api.Models.ContactController;

import java.util.UUID;

public class AddContactInputModel {
    private String phoneNumber;
    private String savedName;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSavedName() {
        return savedName;
    }

    public void setSavedName(String savedName) {
        this.savedName = savedName;
    }
}