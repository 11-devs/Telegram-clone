package Shared.Api.Models.ContactController;

import java.util.UUID;

public class AddContactInputModel {
    private UUID contactId;
    private String savedName;

    public UUID getContactId() { return contactId; }
    public void setContactId(UUID contactId) { this.contactId = contactId; }
    public String getSavedName() { return savedName; }
    public void setSavedName(String savedName) { this.savedName = savedName; }
}