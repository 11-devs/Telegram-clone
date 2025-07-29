package Shared.Database.Repository;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.Chat.Chat;

import java.util.UUID;

public class ChatRepository extends Repository<Chat, UUID> {
    public ChatRepository(GenericDAO<Chat> primaryDAO, GenericDAO<Chat> cacheDAO) {
        super(Chat.class, primaryDAO, cacheDAO);
    }
}