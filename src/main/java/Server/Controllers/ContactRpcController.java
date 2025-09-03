package Server.Controllers;

import JSocket2.Protocol.Rpc.RpcControllerBase;
import JSocket2.Protocol.Rpc.RpcResponse;
import Server.DaoManager;
import Shared.Api.Models.ContactController.*;
import Shared.Models.Account.Account;
import Shared.Models.Contact.Contact;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ContactRpcController extends RpcControllerBase {
    private final DaoManager daoManager;

    public ContactRpcController(DaoManager daoManager) {
        this.daoManager = daoManager;
    }

    public RpcResponse<Object> addContact(AddContactInputModel model) {
        Account owner = daoManager.getAccountDAO().findById(model.getOwnerId());
        if (owner == null) {
            return BadRequest("Owner account not found.");
        }

        Account contactUser = daoManager.getAccountDAO().findById(model.getContactId());
        if (contactUser == null) {
            return BadRequest("Contact user account not found.");
        }

        if (Objects.equals(model.getOwnerId(), model.getContactId())) {
            return BadRequest("Cannot add yourself as a contact.");
        }

        List<Contact> existingContacts = daoManager.getContactDAO().findAllByField("owner.id", model.getOwnerId());
        boolean alreadyExists = existingContacts.stream()
                .anyMatch(c -> c.getContact().getId().equals(model.getContactId()));

        if (alreadyExists) {
            return BadRequest("This user is already in your contacts.");
        }

        Contact newContact = new Contact();
        newContact.setOwner(owner);
        newContact.setContact(contactUser);
        newContact.setSavedName(model.getSavedName());

        daoManager.getContactDAO().insert(newContact);

        return Ok(new AddContactOutputModel("Contact added successfully.", newContact.getId()));
    }

    public RpcResponse<Object> getContacts(GetContactsInputModel model) {
        List<Contact> contacts = daoManager.getContactDAO().findAllByField("owner.id", model.getOwnerId());

        List<ContactInfo> contactInfos = contacts.stream()
                .map(contact -> {
                    Account contactUser = contact.getContact();
                    String firstName;
                    String lastName;

                    // If a custom name is saved, use it. Otherwise, use the contact's actual name.
                    if (contact.getSavedName() != null && !contact.getSavedName().trim().isEmpty()) {
                        firstName = contact.getSavedName();
                        lastName = ""; // Custom name is a single field
                    } else {
                        firstName = contactUser.getFirstName();
                        lastName = contactUser.getLastName();
                    }

                    return new ContactInfo(
                            contactUser.getId(),
                            firstName,
                            lastName,
                            contactUser.getUsername(),
                            contactUser.getStatus(),
                            contactUser.getProfilePictureId()
                    );
                })
                .collect(Collectors.toList());

        return Ok(new GetContactsOutputModel(contactInfos));
    }

    public RpcResponse<Object> removeContact(RemoveContactInputModel model) {
        List<Contact> contacts = daoManager.getContactDAO().findAllByField("owner.id", model.getOwnerId());

        Contact contactToRemove = contacts.stream()
                .filter(c -> c.getContact().getId().equals(model.getContactId()))
                .findFirst()
                .orElse(null);

        if (contactToRemove == null) {
            return BadRequest("Contact not found.");
        }

        daoManager.getContactDAO().delete(contactToRemove);

        return Ok(new RemoveContactOutputModel("Contact removed successfully."));
    }
    public RpcResponse<Object> getContact(GetContactInputModel model) {
        List<Contact> contacts = daoManager.getContactDAO().findAllByField("owner.id", model.getOwnerId());

        Contact contact = contacts.stream()
                .filter(c -> c.getContact().getId().equals(model.getContactId()))
                .findFirst()
                .orElse(null);

        if (contact == null) {
            return BadRequest("Contact not found.");
        }

        Account contactUser = contact.getContact();
        String firstName;
        String lastName;

        // If a custom name is saved, use it. Otherwise, use the contact's actual name.
        if (contact.getSavedName() != null && !contact.getSavedName().trim().isEmpty()) {
            firstName = contact.getSavedName();
            lastName = ""; // Custom name is a single field
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