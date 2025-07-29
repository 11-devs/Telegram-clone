package Shared.Database.Repository;

import Shared.Database.DAO.GenericDAO;
import Shared.Models.Message.Message;
import Shared.Models.Notification.Notification;

import java.util.UUID;


public class NotificationRepository extends Repository<Notification, UUID> {
    public NotificationRepository(GenericDAO<Notification> primaryDAO, GenericDAO<Notification> cacheDAO) {
        super(Notification.class, primaryDAO, cacheDAO);
    }
}