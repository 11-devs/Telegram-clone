// CREATE NEW FILE: main/java/Client/Models/ContactViewModel.java
package Client;

import Shared.Api.Models.ContactController.ContactInfo;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.UUID;

public class ContactViewModel {
    private final UUID contactAccountId;
    private final StringProperty savedName;
    private final StringProperty profilePictureId;

    public ContactViewModel(ContactInfo contactInfo) {
        this.contactAccountId = contactInfo.getUserId();
        this.savedName = new SimpleStringProperty(contactInfo.getFirstName() + " " + contactInfo.getLastName());
        this.profilePictureId = new SimpleStringProperty(contactInfo.getProfilePictureId());
    }

    public UUID getContactAccountId() {
        return contactAccountId;
    }

    public String getSavedName() {
        return savedName.get();
    }

    public StringProperty savedNameProperty() {
        return savedName;
    }

    public String getProfilePictureId() {
        return profilePictureId.get();
    }

    public StringProperty profilePictureIdProperty() {
        return profilePictureId;
    }
}