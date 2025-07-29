package Shared.Database.Repository;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.Folder.Folder;
import Shared.Models.Interaction.Interaction;

import java.util.UUID;

public class InteractionRepository extends Repository<Interaction, UUID> {
    public InteractionRepository(GenericDAO<Interaction> primaryDAO, GenericDAO<Interaction> cacheDAO) {
        super(Interaction.class, primaryDAO, cacheDAO);
    }
}