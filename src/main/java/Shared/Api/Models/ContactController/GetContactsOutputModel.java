package Shared.Api.Models.ContactController;

import java.util.List;

public class GetContactsOutputModel {
    private List<ContactInfo> contacts;

    public GetContactsOutputModel(List<ContactInfo> contacts) {
        this.contacts = contacts;
    }

    // Getters and Setters
    public List<ContactInfo> getContacts() { return contacts; }
    public void setContacts(List<ContactInfo> contacts) { this.contacts = contacts; }
}