package Server;
import Shared.Database.DAO.GenericDAO;
import Shared.Models.Account.Account;
import Shared.Models.Chat.Chat;
import Shared.Models.Contact.Contact;
import Shared.Models.Folder.Folder;
import Shared.Models.Interaction.Interaction;
import Shared.Models.Media.Media;
import Shared.Models.Membership.Membership;
import Shared.Models.Message.Message;
import Shared.Models.Notification.Notification;
import Shared.Models.PendingAuth.PendingAuth;
import Shared.Models.Relationship.Relationship;
import Shared.Models.Session.Session;
import Shared.Models.Setting.Setting;
import jakarta.persistence.EntityManager;

public class DaoManager {
    private final EntityManager entityManager;

    // Lazy-loaded DAOs
    private GenericDAO<Account> accountDAO;
    private GenericDAO<Chat> chatDAO;
    private GenericDAO<Contact> contactDAO;
    private GenericDAO<Folder> folderDAO;
    private GenericDAO<Interaction> interactionDAO;
    private GenericDAO<Media> mediaDAO;
    private GenericDAO<Membership> membershipDAO;
    private GenericDAO<Message> messageDAO;
    private GenericDAO<Notification> notificationDAO;
    private GenericDAO<Relationship> relationshipDAO;
    private GenericDAO<Session> sessionDAO;
    private GenericDAO<Setting> settingDAO;
    private GenericDAO<PendingAuth> pendingAuthDAO;

    public DaoManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // Account DAO
    public GenericDAO<Account> getAccountDAO() {
        if (accountDAO == null) {
            accountDAO = new GenericDAO<>(Account.class, entityManager);
        }
        return accountDAO;
    }

    // Chat DAO
    public GenericDAO<Chat> getChatDAO() {
        if (chatDAO == null) {
            chatDAO = new GenericDAO<>(Chat.class, entityManager);
        }
        return chatDAO;
    }

    // Contact DAO
    public GenericDAO<Contact> getContactDAO() {
        if (contactDAO == null) {
            contactDAO = new GenericDAO<>(Contact.class, entityManager);
        }
        return contactDAO;
    }

    // Folder DAO
    public GenericDAO<Folder> getFolderDAO() {
        if (folderDAO == null) {
            folderDAO = new GenericDAO<>(Folder.class, entityManager);
        }
        return folderDAO;
    }

    // Interaction DAO
    public GenericDAO<Interaction> getInteractionDAO() {
        if (interactionDAO == null) {
            interactionDAO = new GenericDAO<>(Interaction.class, entityManager);
        }
        return interactionDAO;
    }

    // Media DAO
    public GenericDAO<Media> getMediaDAO() {
        if (mediaDAO == null) {
            mediaDAO = new GenericDAO<>(Media.class, entityManager);
        }
        return mediaDAO;
    }

    // Membership DAO
    public GenericDAO<Membership> getMembershipDAO() {
        if (membershipDAO == null) {
            membershipDAO = new GenericDAO<>(Membership.class, entityManager);
        }
        return membershipDAO;
    }

    // Message DAO
    public GenericDAO<Message> getMessageDAO() {
        if (messageDAO == null) {
            messageDAO = new GenericDAO<>(Message.class, entityManager);
        }
        return messageDAO;
    }

    // Notification DAO
    public GenericDAO<Notification> getNotificationDAO() {
        if (notificationDAO == null) {
            notificationDAO = new GenericDAO<>(Notification.class, entityManager);
        }
        return notificationDAO;
    }

    // Relationship DAO
    public GenericDAO<Relationship> getRelationshipDAO() {
        if (relationshipDAO == null) {
            relationshipDAO = new GenericDAO<>(Relationship.class, entityManager);
        }
        return relationshipDAO;
    }

    // Session DAO
    public GenericDAO<Session> getSessionDAO() {
        if (sessionDAO == null) {
            sessionDAO = new GenericDAO<>(Session.class, entityManager);
        }
        return sessionDAO;
    }

    // Setting DAO
    public GenericDAO<Setting> getSettingDAO() {
        if (settingDAO == null) {
            settingDAO = new GenericDAO<>(Setting.class, entityManager);
        }
        return settingDAO;
    }
    public GenericDAO<PendingAuth> getPendingAuthDAO() {
        if (pendingAuthDAO == null) {
            pendingAuthDAO = new GenericDAO<>(PendingAuth.class, entityManager);
        }
        return pendingAuthDAO;
    }

//    // Unit of Work methods
//    public void beginTransaction() {
//        if (!entityManager.getTransaction().isActive()) {
//            entityManager.getTransaction().begin();
//        }
//    }
//
//    public void commit() {
//        if (entityManager.getTransaction().isActive()) {
//            entityManager.getTransaction().commit();
//        }
//    }
//
//    public void rollback() {
//        if (entityManager.getTransaction().isActive()) {
//            entityManager.getTransaction().rollback();
//        }
//    }
//
//    public void close() {
//        if (entityManager != null && entityManager.isOpen()) {
//            entityManager.close();
//        }
//    }
}