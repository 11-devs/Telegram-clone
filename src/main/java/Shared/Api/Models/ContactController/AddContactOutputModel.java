package Shared.Api.Models.ContactController;

import java.util.UUID;

public class AddContactOutputModel {
    private String status;
    private UUID contactId;

    public AddContactOutputModel(String status, UUID contactId) {
        this.status = status;
        this.contactId = contactId;
    }
    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public UUID getContactId() { return contactId; }
    public void setContactId(UUID contactId) { this.contactId = contactId; }
}