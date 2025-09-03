package Shared.Api.Models.ContactController;

import java.util.UUID;

public class GetContactsInputModel {
    private UUID ownerId;

    // Getters and Setters
    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
}