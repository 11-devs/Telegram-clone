package Shared.Database.Repository;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.Contact.Contact;
import Shared.Models.Folder.Folder;

import java.util.UUID;

public class FolderRepository extends Repository<Folder, UUID> {
    public FolderRepository(GenericDAO<Folder> primaryDAO, GenericDAO<Folder> cacheDAO) {
        super(Folder.class, primaryDAO, cacheDAO);
    }
}