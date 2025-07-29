package Shared.Database.Repository;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.Membership.Membership;
import Shared.Models.Message.Message;

import java.util.UUID;


public class MessageRepository extends Repository<Message, UUID> {
    public MessageRepository(GenericDAO<Message> primaryDAO, GenericDAO<Message> cacheDAO) {
        super(Message.class, primaryDAO, cacheDAO);
    }
}