package Server;

import Shared.Database.Database;
import Shared.Models.Account.Account;
import Shared.Models.Account.AccountStatus;
import Shared.Models.Chat.*;
import Shared.Models.Contact.Contact;
import Shared.Models.Media.Media;
import Shared.Models.Media.MediaType;
import Shared.Models.Membership.Membership;
import Shared.Models.Membership.MembershipType;
import Shared.Models.Message.Message;
import Shared.Models.Message.MessageType;
import Shared.Models.Message.TextMessage;
import Shared.Utils.Console;
import Shared.Utils.PasswordUtil;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A comprehensive data seeder for the TelegramClone application.
 * This class can be run to clear the database and populate it with a rich set of sample data.
 */
public class DataSeeder {
    private final DaoManager daoManager;
    private final Database database;
    private final Random random = new Random();

    // Store created entities to build relationships
    private final List<Account> seededAccounts = new ArrayList<>();
    private final Map<UUID, Chat> seededChats = new HashMap<>();

    public DataSeeder(DaoManager daoManager, Database database) {
        this.daoManager = daoManager;
        this.database = database;
    }

    /**
     * The main entry point to run the seeder.
     */
    public static void main(String[] args) {
        Console.print("Starting database seeder...", Console.Color.CYAN);
        Database db = new Database();
        EntityManager em = db.getEntityManager();
        DaoManager dm = new DaoManager(em);
        DataSeeder seeder = new DataSeeder(dm, db);

        try {
            seeder.seed(true); // true to clear the database before seeding
            Console.print("Database seeding completed successfully!", Console.Color.GREEN);
        } catch (Exception e) {
            Console.error("An error occurred during seeding: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (em.isOpen()) {
                em.close();
            }
            Console.print("EntityManager closed.", Console.Color.YELLOW);
        }
    }

    /**
     * Orchestrates the entire seeding process.
     * @param clearFirst If true, all tables will be cleared before seeding.
     */
    public void seed(boolean clearFirst) {
        if (clearFirst) {
            Console.print("Clearing all tables...", Console.Color.YELLOW);
            database.clearAllTables();
            Console.print("Tables cleared.", Console.Color.GREEN);
        }

        seedAccountsAndSavedMessages();
        seedContacts();
        seedPrivateChats();
        seedGroupChats();
        seedChannels();
        seedMessages();
        seedMessageViews();
    }

    /**
     * Helper method to create and save a Media entity, returning its UUID (the media entity's primary key ID).
     * This ID will be used as `profilePictureId` in `Account` and `Chat` entities.
     * The `fileId` within the `Media` entity is a separate UUID that clients request for actual file transfer.
     * @param fileName The original file name (e.g., "my_avatar.png").
     * @param mediaType The type of media (e.g., IMAGE, DOCUMENT).
     * @param size The size of the file in bytes.
     * @return The UUID string of the created Media entity.
     */
    private String createAndSaveMedia(String fileName, MediaType mediaType, long size) {
        Media media = new Media();
        UUID mediaEntityId = UUID.randomUUID(); // This is the ID for the Media entity
        UUID fileTransferId = UUID.randomUUID();  // This is the fileId clients will request for transfer

        media.setId(mediaEntityId); // Set the entity ID
        media.setFileId(fileTransferId.toString()); // Set the file ID for actual download
        media.setFileName(fileName);
        media.setFileExtension(Shared.Utils.FileUtil.getFileExtension(fileName));
        media.setSize(size);
        media.setType(mediaType);
        media.setMimeType(getMimeTypeForExtension(media.getFileExtension()));

        daoManager.getMediaDAO().insert(media);
        Console.log("  - Created Media entry: " + fileName + " (Media ID: " + media.getId() + ", File ID: " + media.getFileId() + ")");
        return media.getId().toString(); // Return the Media entity ID to be used as profilePictureId
    }

    /**
     * Determines a MIME type based on a file extension.
     */
    private String getMimeTypeForExtension(String extension) {
        return switch (extension.toLowerCase()) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "pdf" -> "application/pdf";
            case "doc", "docx" -> "application/msword";
            case "mp4" -> "video/mp4";
            case "mp3" -> "audio/mpeg";
            default -> "application/octet-stream";
        };
    }

    private void seedAccountsAndSavedMessages() {
        Console.print("\n--- Seeding Accounts and Saved Messages ---", Console.Color.BLUE);
        // User data with associated avatar file names
        List<Map<String, String>> usersData = List.of(
                Map.of("firstName", "Bruce", "lastName", "Wayne", "phone", "+15550000001", "username", "batman_dk", "bio", "The hero Gotham deserves.", "avatar", "batman_profile.png"),
                Map.of("firstName", "Clark", "lastName", "Kent", "phone", "+15550000002", "username", "last_son_krypton", "bio", "Truth, Justice, and the American Way.", "avatar", "superman_profile.png"),
                Map.of("firstName", "Barry", "lastName", "Allen", "phone", "+15550000003", "username", "fastest_man_alive", "bio", "Speedster from Central City.", "avatar", "flash_profile.png"),
                Map.of("firstName", "Oliver", "lastName", "Queen", "phone", "+15550000004", "username", "star_city_archer", "bio", "You have failed this city!", "avatar", "arrow_profile.png"),
                Map.of("firstName", "Rick", "lastName", "Sanchez", "phone", "+15550000005", "username", "rick_c137", "bio", "Wubba lubba dub dub!", "avatar", "rick_profile.png"),
                Map.of("firstName", "Morty", "lastName", "Smith", "phone", "+15550000006", "username", "oh_jeez_morty", "bio", "Aw, geez.", "avatar", "morty_profile.png"),
                Map.of("firstName", "Nolan", "lastName", "Grayson", "phone", "+15550000007", "username", "viltrumite_conqueror", "bio", "Think, Mark, think!", "avatar", "omniman_profile.png"),
                Map.of("firstName", "My", "lastName", "Account", "phone", "+989123456789", "username", "me", "bio", "This is my test account.", "avatar", "default_user_profile.png")
        );

        usersData.forEach(userData -> {
            Account account = new Account();
            account.setFirstName(userData.get("firstName"));
            account.setLastName(userData.get("lastName"));
            account.setPhoneNumber(userData.get("phone"));
            account.setUsername(userData.get("username"));
            account.setBio(userData.get("bio"));
            account.setHashedPassword(PasswordUtil.hash("12345678")); // Default password for all
            account.setStatus(random.nextBoolean() ? AccountStatus.ONLINE : AccountStatus.OFFLINE);
            account.setEmail(userData.get("username") + "@example.com"); // Unique email

            // Create and assign profile picture Media entity ID
            String profilePictureMediaId = createAndSaveMedia(userData.get("avatar"), MediaType.IMAGE, 50 * 1024); // 50KB dummy size
            account.setProfilePictureId(profilePictureMediaId);

            daoManager.getAccountDAO().insert(account);
            seededAccounts.add(account);
            Console.log("Created account: " + account.getFirstName() + " " + account.getLastName());

            // Each user gets a "Saved Messages" chat
            SavedMessages savedMessages = new SavedMessages(account);
            savedMessages.setTitle("Saved Messages");
            // Saved messages usually have a generic icon.
            String savedMessagesIconId = createAndSaveMedia("saved_messages_icon.png", MediaType.IMAGE, 10 * 1024);
            savedMessages.setProfilePictureId(savedMessagesIconId);

            daoManager.getSavedMessagesDAO().insert(savedMessages);
            seededChats.put(savedMessages.getId(), savedMessages);
            // Add user to their own saved messages chat
            createMembership(account, savedMessages, MembershipType.OWNER, null);
        });
    }

    private void seedContacts() {
        Console.print("\n--- Seeding Contacts ---", Console.Color.BLUE);
        Account me = seededAccounts.stream().filter(a -> "me".equals(a.getUsername())).findFirst().orElseThrow();

        // 'Me' has everyone else as a contact
        seededAccounts.stream().filter(a -> !a.equals(me)).forEach(contactUser -> {
            createContact(me, contactUser, null); // Use their real name
        });

        // Add some random contacts for other users
        for (int i = 0; i < seededAccounts.size() - 1; i++) {
            Account owner = seededAccounts.get(i);
            int numContacts = random.nextInt(3) + 2; // 2 to 4 contacts
            for (int j = 0; j < numContacts; j++) {
                Account contactUser = seededAccounts.get(random.nextInt(seededAccounts.size()));
                // Ensure not adding self and contact doesn't already exist
                if (!owner.equals(contactUser) && daoManager.getContactDAO().findAllByField("owner.id", owner.getId()).stream().noneMatch(c -> c.getContact().equals(contactUser))) {
                    createContact(owner, contactUser, null);
                }
            }
        }
    }

    private void seedPrivateChats() {
        Console.print("\n--- Seeding Private Chats ---", Console.Color.BLUE);
        Account me = seededAccounts.stream().filter(a -> "me".equals(a.getUsername())).findFirst().orElseThrow();
        List<Contact> myContacts = daoManager.getContactDAO().findAllByField("owner.id", me.getId());

        myContacts.forEach(contact -> {
            Account otherUser = contact.getContact();
            PrivateChat chat = new PrivateChat(me, otherUser);
            daoManager.getPrivateChatDAO().insert(chat);
            seededChats.put(chat.getId(), chat);
            Console.log("Created private chat between 'Me' and " + otherUser.getFirstName());

            createMembership(me, chat, MembershipType.MEMBER, null);
            createMembership(otherUser, chat, MembershipType.MEMBER, null);
        });
    }

    private void seedGroupChats() {
        Console.print("\n--- Seeding Group Chats ---", Console.Color.BLUE);

        Account batman = seededAccounts.stream().filter(a -> "batman_dk".equals(a.getUsername())).findFirst().orElseThrow();
        Account superman = seededAccounts.stream().filter(a -> "last_son_krypton".equals(a.getUsername())).findFirst().orElseThrow();
        Account flash = seededAccounts.stream().filter(a -> "fastest_man_alive".equals(a.getUsername())).findFirst().orElseThrow();
        Account arrow = seededAccounts.stream().filter(a -> "star_city_archer".equals(a.getUsername())).findFirst().orElseThrow();
        Account rickC137 = seededAccounts.stream().filter(a -> "rick_c137".equals(a.getUsername())).findFirst().orElseThrow();
        Account mortySmith = seededAccounts.stream().filter(a -> "oh_jeez_morty".equals(a.getUsername())).findFirst().orElseThrow();
        Account nolanGrayson = seededAccounts.stream().filter(a -> "viltrumite_conqueror".equals(a.getUsername())).findFirst().orElseThrow();
        Account myAccount = seededAccounts.stream().filter(a -> "me".equals(a.getUsername())).findFirst().orElseThrow();

        // Group 1: Justice League
        String jlGroupPicId = createAndSaveMedia("justice_league_group.png", MediaType.IMAGE, 100 * 1024);
        createGroupChat("🛡️ Justice League", "United for justice.", batman, List.of(superman, flash, arrow), jlGroupPicId);

        // Group 2: The Council of Ricks
        List<Account> otherRicksAndMorty = seededAccounts.stream()
                .filter(a -> !a.equals(rickC137) && !a.equals(mortySmith))
                .limit(2).collect(Collectors.toList());
        otherRicksAndMorty.add(mortySmith);

        String ricksCouncilPicId = createAndSaveMedia("ricks_council_group.png", MediaType.IMAGE, 80 * 1024);
        createGroupChat("🌌 Council of Ricks", "For Ricks, by Ricks, of Ricks.", rickC137, otherRicksAndMorty, ricksCouncilPicId);

        // Group 3: Invincible's Friends
        List<Account> invincibleFriends = List.of(myAccount, superman, flash); // Mix with other characters

        String invincibleGroupPicId = createAndSaveMedia("invincible_friends_group.png", MediaType.IMAGE, 70 * 1024);
        createGroupChat("🦸‍♂️ Invincible & Co.", "Keeping the city safe, one villain at a time.", nolanGrayson, invincibleFriends, invincibleGroupPicId);

        // Group 4: Gotham Vigilantes (Smaller, focused group)
        String gothamVigilantesPicId = createAndSaveMedia("gotham_vigilantes_group.png", MediaType.IMAGE, 65 * 1024);
        createGroupChat("🦇 Gotham Vigilantes", "Protecting Gotham's streets. Keep comms secure.", batman, List.of(arrow), gothamVigilantesPicId);
    }

    private void seedChannels() {
        Console.print("\n--- Seeding Channels ---", Console.Color.BLUE);

        Account batman = seededAccounts.stream().filter(a -> "batman_dk".equals(a.getUsername())).findFirst().orElseThrow();
        Account superman = seededAccounts.stream().filter(a -> "last_son_krypton".equals(a.getUsername())).findFirst().orElseThrow();
        Account flash = seededAccounts.stream().filter(a -> "fastest_man_alive".equals(a.getUsername())).findFirst().orElseThrow();
        Account arrow = seededAccounts.stream().filter(a -> "star_city_archer".equals(a.getUsername())).findFirst().orElseThrow();
        Account rickC137 = seededAccounts.stream().filter(a -> "rick_c137".equals(a.getUsername())).findFirst().orElseThrow();
        Account mortySmith = seededAccounts.stream().filter(a -> "oh_jeez_morty".equals(a.getUsername())).findFirst().orElseThrow();

        // Channel 1: Daily Planet News (Public)
        List<Account> dailyPlanetSubscribers = seededAccounts.stream()
                .filter(a -> !a.equals(superman))
                .limit(3)
                .collect(Collectors.toList());

        String dailyPlanetChannelPicId = createAndSaveMedia("daily_planet_channel.png", MediaType.IMAGE, 60 * 1024);
        createChannel("Daily Planet 🌎", "All the news that's fit to print!", superman, dailyPlanetSubscribers, true, dailyPlanetChannelPicId);

        // Channel 2: Wayne Enterprises Announcements (Private)
        List<Account> wayneEnterprisesStaff = seededAccounts.stream()
                .filter(a -> !a.equals(batman))
                .limit(2)
                .collect(Collectors.toList());

        String wayneEnterprisesChannelPicId = createAndSaveMedia("wayne_enterprises_channel.png", MediaType.IMAGE, 90 * 1024);
        createChannel("💰 Wayne Enterprises", "Official announcements for WE employees.", batman, wayneEnterprisesStaff, false, wayneEnterprisesChannelPicId);

        // Channel 3: Speed Force Updates (Public)
        List<Account> speedsterFans = seededAccounts.stream()
                .filter(a -> !a.equals(flash))
                .skip(4)
                .limit(2)
                .collect(Collectors.toList());

        String speedForceChannelPicId = createAndSaveMedia("speed_force_channel.png", MediaType.IMAGE, 55 * 1024);
        createChannel("⚡ Speed Force News", "Faster than a speeding bullet! Updates from the Speed Force.", flash, speedsterFans, true, speedForceChannelPicId);

        // Channel 4: Citadel of Ricks Broadcast (Public)
        List<Account> citadelSubscribers = seededAccounts.stream()
                .filter(a -> !a.equals(rickC137))
                .limit(4)
                .collect(Collectors.toList());

        String citadelChannelPicId = createAndSaveMedia("citadel_of_ricks_channel.png", MediaType.IMAGE, 75 * 1024);
        createChannel("🏛️ Citadel of Ricks", "Official broadcasts from the Council of Ricks. Wubba lubba dub dub!", rickC137, citadelSubscribers, true, citadelChannelPicId);
    }


    private void seedMessages() {
        Console.print("\n--- Seeding Messages ---\n", Console.Color.BLUE);
        List<String> sampleMessages = List.of(
                "Hey, how are you?", "Did you see the latest news?", "Let's catch up tomorrow.", "I'm running a bit late, sorry!",
                "Can you send me the file?", "Thanks!", "😂 That's hilarious!", "What's the plan for the weekend?", "I agree.",
                "Let me check and get back to you.", "Meeting at 3 PM.", "Don't forget the presentation.", "Lunch today?",
                "Good morning!", "See you soon.",
                "**Important update** on the project deadline.",
                "__Please read carefully__ before proceeding.",
                "++Spoiler: The villain was actually Batman!++",
                "Did anyone see @flash_profile's new record?",
                "Working on a new gadget, stay tuned!",
                "Just finished a patrol. Gotham is quiet tonight.",
                "Need to brief the team about Project Phoenix.",
                "I've got a bad feeling about this, Morty.",
                "Just another typical Tuesday dimension-hopping.",
                "Think, Mark, think! What will you have after 500 years?"
        );

        seededChats.values().forEach(chat -> {
            List<Membership> members = daoManager.getMembershipDAO().findAllByField("chat.id", chat.getId());
            if (members.isEmpty()) return;

            int messageCount = chat.getType() == ChatType.PRIVATE ? random.nextInt(15) + 10 : random.nextInt(30) + 20;
            Console.log("Seeding " + messageCount + " messages in chat: '" + getChatTitle(chat, members.get(0).getAccount()) + "'");
            LocalDateTime timestamp = LocalDateTime.now().minusDays(5);

            for (int i = 0; i < messageCount; i++) {
                Account sender = members.get(random.nextInt(members.size())).getAccount();
                String content = sampleMessages.get(random.nextInt(sampleMessages.size()));

                // For channels, only the owner/admin can post
                if (chat.getType() == ChatType.CHANNEL) {
                    sender = members.stream()
                            .filter(m -> m.getType() == MembershipType.OWNER || m.getType() == MembershipType.ADMIN)
                            .findFirst()
                            .map(Membership::getAccount)
                            .orElse(members.get(0).getAccount()); // Fallback to first member if no admin/owner (shouldn't happen)
                }

                TextMessage message = new TextMessage();
                message.setChat(chat);
                message.setSender(sender);
                message.setTextContent(content);
                message.setTimestamp(timestamp);
                message.setEdited(random.nextDouble() < 0.1);
                message.setType(MessageType.TEXT);
                daoManager.getMessageDAO().insert(message);

                timestamp = timestamp.plusMinutes(random.nextInt(60) + 1).plusSeconds(random.nextInt(60));
            }
        });
    }

    private void seedMessageViews() {
        Console.print("\n--- Seeding Message Views (Last Read) ---", Console.Color.BLUE);
        seededChats.values().forEach(chat -> {
            List<Message> messages = daoManager.getMessageDAO().findAllByField("chat.id", chat.getId());
            if (messages.isEmpty()) return;

            // Sort messages by timestamp to find the latest
            messages.sort(Comparator.comparing(Message::getTimestamp));

            List<Membership> members = daoManager.getMembershipDAO().findAllByField("chat.id", chat.getId());
            members.forEach(member -> {
                // 50% chance a user has read everything
                if (random.nextBoolean()) {
                    member.setLastReadMessage(messages.get(messages.size() - 1));
                } else {
                    // Otherwise, they've read a random message from the last 10 (or fewer)
                    int lastIndex = messages.size() - 1;
                    int readIndex = Math.max(0, lastIndex - random.nextInt(Math.min(10, messages.size())));
                    member.setLastReadMessage(messages.get(readIndex));
                }
                daoManager.getMembershipDAO().update(member);
            });
            Console.log("Updated last read messages for chat: '" + getChatTitle(chat, members.get(0).getAccount()) + "'");
        });
    }

    // --- Helper Methods ---

    private void createContact(Account owner, Account contactUser, String savedName) {
        // Prevent duplicate contacts
        if (daoManager.getContactDAO().findAllByField("owner.id", owner.getId()).stream()
                .anyMatch(c -> c.getContact().equals(contactUser))) {
            return; // Contact already exists, skip
        }

        Contact contact = new Contact();
        contact.setOwner(owner);
        contact.setContact(contactUser);
        contact.setSavedName(savedName != null ? savedName : contactUser.getFirstName() + " " + contactUser.getLastName());
        daoManager.getContactDAO().insert(contact);
        Console.log(String.format("  - %s added %s to contacts.", owner.getFirstName(), contactUser.getFirstName()));
    }

    private Membership createMembership(Account account, Chat chat, MembershipType type, Account invitedBy) {
        Membership membership = new Membership();
        membership.setAccount(account);
        membership.setChat(chat);
        membership.setType(type);
        membership.setJoinDate(LocalDateTime.now().minusDays(ThreadLocalRandom.current().nextInt(1, 30)));
        if (invitedBy != null) {
            membership.setInvitedBy(invitedBy);
        }
        daoManager.getMembershipDAO().insert(membership);
        return membership;
    }

    // Modified createGroupChat to accept profilePictureMediaId
    private void createGroupChat(String title, String description, Account owner, List<Account> members, String profilePictureMediaId) {
        GroupChat group = new GroupChat(title, profilePictureMediaId, owner, description);
        daoManager.getGroupChatDAO().insert(group);
        seededChats.put(group.getId(), group);
        Console.log("Created group chat: " + title);

        createMembership(owner, group, MembershipType.OWNER, null);
        Console.log("  - Added " + owner.getFirstName() + " as OWNER");

        members.forEach(member -> {
            if (!member.equals(owner)) {
                createMembership(member, group, MembershipType.MEMBER, owner);
                Console.log("  - Added " + member.getFirstName() + " as MEMBER");
            }
        });
    }

    // Modified createChannel to accept profilePictureMediaId
    private void createChannel(String title, String description, Account owner, List<Account> members, boolean isPublic, String profilePictureMediaId) {
        Channel channel = new Channel(title, profilePictureMediaId, owner, description, isPublic);
        daoManager.getChannelDAO().insert(channel);
        seededChats.put(channel.getId(), channel);
        Console.log("Created channel: " + title);

        createMembership(owner, channel, MembershipType.OWNER, null);
        Console.log("  - Added " + owner.getFirstName() + " as OWNER");

        members.forEach(member -> {
            if (!member.equals(owner)) {
                createMembership(member, channel, MembershipType.MEMBER, owner);
                Console.log("  - Added " + member.getFirstName() + " as SUBSCRIBER");
            }
        });
    }

    private String getChatTitle(Chat chat, Account currentUser) {
        if (chat.getTitle() != null && !chat.getTitle().isEmpty()) {
            return chat.getTitle();
        }
        if (chat.getType() == ChatType.PRIVATE) {
            return daoManager.getMembershipDAO().findAllByField("chat.id", chat.getId()).stream()
                    .map(Membership::getAccount)
                    .filter(acc -> !acc.getId().equals(currentUser.getId()))
                    .map(acc -> acc.getFirstName() + " " + acc.getLastName())
                    .findFirst()
                    .orElse("Private Chat");
        }
        return "Chat " + chat.getId().toString().substring(0, 8);
    }
}