package Shared.Database.Repository;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.Notification.Notification;
import Shared.Models.Relationship.Relationship;

import java.util.UUID;


public class RelationshipRepository extends Repository<Relationship, UUID> {
    public RelationshipRepository(GenericDAO<Relationship> primaryDAO, GenericDAO<Relationship> cacheDAO) {
        super(Relationship.class, primaryDAO, cacheDAO);
    }
}