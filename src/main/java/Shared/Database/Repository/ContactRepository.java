package Shared.Database.Repository;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.Chat.Chat;
import Shared.Models.Contact.Contact;

import java.util.UUID;

public class ContactRepository extends Repository<Contact, UUID> {
    public ContactRepository(GenericDAO<Contact> primaryDAO, GenericDAO<Contact> cacheDAO) {
        super(Contact.class, primaryDAO, cacheDAO);
    }
}