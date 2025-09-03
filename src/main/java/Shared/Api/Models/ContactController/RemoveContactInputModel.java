package Shared.Api.Models.ContactController;

import java.util.UUID;

public class RemoveContactInputModel {
    private UUID ownerId;
    private UUID contactId;

    // Getters and Setters
    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
    public UUID getContactId() { return contactId; }
    public void setContactId(UUID contactId) { this.contactId = contactId; }
}