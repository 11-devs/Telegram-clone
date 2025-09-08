package Server.Controllers;

import JSocket2.Protocol.Rpc.RpcControllerBase;
import JSocket2.Protocol.Rpc.RpcResponse;
import Server.DaoManager;
import Shared.Api.Models.ContactController.*;
import Shared.Models.Account.Account;
import Shared.Models.Chat.PrivateChat;
import Shared.Models.Contact.Contact;
import Shared.Models.Media.Media;
import Shared.Models.Membership.Membership;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ContactRpcController extends RpcControllerBase {
    private final DaoManager daoManager;

    public ContactRpcController(DaoManager daoManager) {
        this.daoManager = daoManager;
    }

    public RpcResponse<Object> addContact(AddContactInputModel model) {
        UUID currentUserId = UUID.fromString(getCurrentUser().getUserId());
        Account owner = daoManager.getAccountDAO().findById(currentUserId);
        if (owner == null) {
            return Forbidden("Owner account not found or invalid session.");
        }

        if (model.getPhoneNumber() == null || model.getPhoneNumber().trim().isEmpty()) {
            return BadRequest("Phone number cannot be empty.");
        }

        Account contactUser = daoManager.getAccountDAO().findByField("phoneNumber", model.getPhoneNumber());
        if (contactUser == null) {
            return BadRequest("User with the specified phone number not found.");
        }

        if (owner.getId().equals(contactUser.getId())) {
            return BadRequest("You cannot add yourself as a contact.");
        }

        List<Contact> existingContacts = daoManager.getContactDAO().findAllByField("owner.id", currentUserId);
        boolean alreadyExists = existingContacts.stream()
                .anyMatch(c -> c.getContact().getId().equals(contactUser.getId()));

        if (alreadyExists) {
            return BadRequest("This user is already in your contacts.");
        }

        Contact newContact = new Contact();
        newContact.setOwner(owner);
        newContact.setContact(contactUser);

        String savedName = model.getSavedName();
        if (savedName == null || savedName.trim().isEmpty()) {
            savedName = contactUser.getFirstName() + (contactUser.getLastName() != null ? " " + contactUser.getLastName() : "");
        }
        newContact.setSavedName(savedName.trim());

        daoManager.getContactDAO().insert(newContact);

        return Ok(new AddContactOutputModel("ContactViewModel added successfully.", newContact.getId()));
    }

    public RpcResponse<Object> getContacts() {
        UUID currentUserId = UUID.fromString(getCurrentUser().getUserId());
        List<Contact> contacts = daoManager.getContactDAO().findAllByField("owner.id", currentUserId);
        List<ContactInfo> contactInfos = contacts.stream()
                .map(contact -> {
                    Account contactUser = contact.getContact();
                    String firstName;
                    String lastName;
                    String fileId = "";
                    // If a custom name is saved, use it. Otherwise, use the contact's actual name.
                    if (contact.getSavedName() != null && !contact.getSavedName().trim().isEmpty()) {
                        String[] names = contact.getSavedName().split(" ", 2);
                        firstName = names[0];
                        lastName = names.length > 1 ? names[1] : "";
                    } else {
                        firstName = contactUser.getFirstName();
                        lastName = contactUser.getLastName();
                    }
                    if(contactUser.getProfilePictureId() != null && !contactUser.getProfilePictureId().trim().isEmpty()) {
                        Media media = daoManager.getEntityManager().find(Media.class, UUID.fromString(contactUser.getProfilePictureId()));
                        if (media != null) {
                            fileId = media.getFileId();
                        }
                    }
                    return new ContactInfo(
                            contactUser.getId(),
                            firstName,
                            lastName,
                            contactUser.getUsername(),
                            contactUser.getStatus(),
                            fileId
                    );
                })
                .collect(Collectors.toList());

        return Ok(new GetContactsOutputModel(contactInfos));
    }

    public RpcResponse<Object> removeContact(UUID userId) {

        UUID currentUserId = UUID.fromString(getCurrentUser().getUserId());
        List<Contact> contacts = daoManager.getContactDAO().findAllByField("owner.id", currentUserId);
        Contact contactToRemove = contacts.stream()
                .filter(c -> c.getContact().getId().equals(userId))
                .findFirst()
                .orElse(null);

        if (contactToRemove == null) {
            return BadRequest("not found.");
        }
        var accountFullName = contactToRemove.getContact().getFirstName() + " " + contactToRemove.getContact().getLastName();
        daoManager.getContactDAO().delete(contactToRemove);
        return Ok(accountFullName);
    }
    public RpcResponse<Object> getContact(GetContactInputModel model) {
        UUID currentUserId = UUID.fromString(getCurrentUser().getUserId());
        List<Contact> contacts = daoManager.getContactDAO().findAllByField("owner.id",currentUserId);

        Contact contact = contacts.stream()
                .filter(c -> c.getContact().getId().equals(model.getContactId()))
                .findFirst()
                .orElse(null);

        if (contact == null) {
            return BadRequest("ContactViewModel not found.");
        }

        Account contactUser = contact.getContact();
        String firstName;
        String lastName;

        if (contact.getSavedName() != null && !contact.getSavedName().trim().isEmpty()) {
            String[] names = contact.getSavedName().split(" ", 2);
            firstName = names[0];
            lastName = names.length > 1 ? names[1] : "";
        } else {
            firstName = contactUser.getFirstName();
            lastName = contactUser.getLastName();
        }

        ContactInfo contactInfo = new ContactInfo(
                contactUser.getId(),
                firstName,
                lastName,
                contactUser.getUsername(),
                contactUser.getStatus(),
                contactUser.getProfilePictureId()
        );

        return Ok(contactInfo);
    }
}