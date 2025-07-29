package Shared.Database.Repository;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.Relationship.Relationship;
import Shared.Models.Session.Session;

import java.util.UUID;


public class SessionRepository extends Repository<Session, UUID> {
    public SessionRepository(GenericDAO<Session> primaryDAO, GenericDAO<Session> cacheDAO) {
        super(Session.class, primaryDAO, cacheDAO);
    }
}