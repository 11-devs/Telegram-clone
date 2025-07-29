package Shared.Database.Repository;

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
import Shared.Models.Relationship.Relationship;
import Shared.Models.Session.Session;
import Shared.Models.Setting.Setting;
import jakarta.persistence.EntityManager;

public class RepositoryManager {
    public static AccountRepository accountRepository;
    public static ChatRepository chatRepository;
    public static ContactRepository contactRepository;
    public static FolderRepository folderRepository;
    public static InteractionRepository interactionRepository;
    public static MediaRepository mediaRepository;
    public static MembershipRepository membershipRepository;
    public static MessageRepository messageRepository;
    public static NotificationRepository notificationRepository;
    public static RelationshipRepository relationshipRepository;
    public static SessionRepository sessionRepository;
    public static SettingRepository settingRepository;

    public static void initialize(EntityManager primaryEntityManager, EntityManager localEntityManager) {
        // Primary DAOs
        GenericDAO<Account> accountDAO = new GenericDAO<>(Account.class, primaryEntityManager);
        GenericDAO<Chat> chatDAO = new GenericDAO<>(Chat.class, primaryEntityManager);
        GenericDAO<Contact> contactDAO = new GenericDAO<>(Contact.class, primaryEntityManager);
        GenericDAO<Folder> folderDAO = new GenericDAO<>(Folder.class, primaryEntityManager);
        GenericDAO<Interaction> interactionDAO = new GenericDAO<>(Interaction.class, primaryEntityManager);
        GenericDAO<Media> mediaDAO = new GenericDAO<>(Media.class, primaryEntityManager);
        GenericDAO<Membership> membershipDAO = new GenericDAO<>(Membership.class, primaryEntityManager);
        GenericDAO<Message> messageDAO = new GenericDAO<>(Message.class, primaryEntityManager);
        GenericDAO<Notification> notificationDAO = new GenericDAO<>(Notification.class, primaryEntityManager);
        GenericDAO<Relationship> relationshipDAO = new GenericDAO<>(Relationship.class, primaryEntityManager);
        GenericDAO<Session> sessionDAO = new GenericDAO<>(Session.class, primaryEntityManager);
        GenericDAO<Setting> settingDAO = new GenericDAO<>(Setting.class, primaryEntityManager);

        // Local/Cache DAOs
        GenericDAO<Account> accountLocalDAO = new GenericDAO<>(Account.class, localEntityManager);
        GenericDAO<Chat> chatLocalDAO = new GenericDAO<>(Chat.class, localEntityManager);
        GenericDAO<Contact> contactLocalDAO = new GenericDAO<>(Contact.class, localEntityManager);
        GenericDAO<Folder> folderLocalDAO = new GenericDAO<>(Folder.class, localEntityManager);
        GenericDAO<Interaction> interactionLocalDAO = new GenericDAO<>(Interaction.class, localEntityManager);
        GenericDAO<Media> mediaLocalDAO = new GenericDAO<>(Media.class, localEntityManager);
        GenericDAO<Membership> membershipLocalDAO = new GenericDAO<>(Membership.class, localEntityManager);
        GenericDAO<Message> messageLocalDAO = new GenericDAO<>(Message.class, localEntityManager);
        GenericDAO<Notification> notificationLocalDAO = new GenericDAO<>(Notification.class, localEntityManager);
        GenericDAO<Relationship> relationshipLocalDAO = new GenericDAO<>(Relationship.class, localEntityManager);
        GenericDAO<Session> sessionLocalDAO = new GenericDAO<>(Session.class, localEntityManager);
        GenericDAO<Setting> settingLocalDAO = new GenericDAO<>(Setting.class, localEntityManager);

        // Repositories with both primary and local DAOs
        accountRepository = new AccountRepository(accountDAO, accountLocalDAO);
        chatRepository = new ChatRepository(chatDAO, chatLocalDAO);
        contactRepository = new ContactRepository(contactDAO, contactLocalDAO);
        folderRepository = new FolderRepository(folderDAO, folderLocalDAO);
        interactionRepository = new InteractionRepository(interactionDAO, interactionLocalDAO);
        mediaRepository = new MediaRepository(mediaDAO, mediaLocalDAO);
        membershipRepository = new MembershipRepository(membershipDAO, membershipLocalDAO);
        messageRepository = new MessageRepository(messageDAO, messageLocalDAO);
        notificationRepository = new NotificationRepository(notificationDAO, notificationLocalDAO);
        relationshipRepository = new RelationshipRepository(relationshipDAO, relationshipLocalDAO);
        sessionRepository = new SessionRepository(sessionDAO, sessionLocalDAO);
        settingRepository = new SettingRepository(settingDAO, settingLocalDAO);
    }

}
