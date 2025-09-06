package Server;

import Shared.Database.Database;
import Shared.Models.Account.Account;
import Shared.Models.Account.AccountStatus;
import Shared.Models.Chat.*;
import Shared.Models.Contact.Contact;
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
            em.close();
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

    private void seedAccountsAndSavedMessages() {
        Console.print("\n--- Seeding Accounts and Saved Messages ---", Console.Color.BLUE);
        List<Map<String, String>> users = List.of(
                Map.of("firstName", "Alice", "lastName", "Smith", "phone", "+15550000001", "username", "alice_s", "bio", "Software Engineer | Coffee enthusiast ☕"),
                Map.of("firstName", "Bob", "lastName", "Johnson", "phone", "+15550000002", "username", "bobbyj", "bio", "Designer and illustrator."),
                Map.of("firstName", "Charlie", "lastName", "Brown", "phone", "+15550000003", "username", "good_grief", "bio", "Just a kid with a dog."),
                Map.of("firstName", "Diana", "lastName", "Prince", "phone", "+15550000004", "username", "wonderwoman", "bio", "Fighting for those who cannot fight for themselves."),
                Map.of("firstName", "Ethan", "lastName", "Hunt", "phone", "+15550000005", "username", "imf_agent", "bio", "Your mission, should you choose to accept it..."),
                Map.of("firstName", "Fiona", "lastName", "Glenanne", "phone", "+15550000006", "username", "fiona_g", "bio", "Ogres are like onions."),
                Map.of("firstName", "George", "lastName", "Costanza", "phone", "+15550000007", "username", "art_vandelay", "bio", "Architect and importer/exporter."),
                Map.of("firstName", "Hermione", "lastName", "Granger", "phone", "+15550000008", "username", "brightest_witch", "bio", "It's Levi-O-sa, not Levio-SA!"),
                Map.of("firstName", "Indiana", "lastName", "Jones", "phone", "+15550000009", "username", "indy", "bio", "It belongs in a museum!"),
                Map.of("firstName", "Jack", "lastName", "Sparrow", "phone", "+15550000010", "username", "captain_jack", "bio", "Why is the rum gone?"),
                Map.of("firstName", "My", "lastName", "Account", "phone", "+989123456789", "username", "me", "bio", "This is my test account.")
        );

        users.forEach(userData -> {
            Account account = new Account();
            account.setFirstName(userData.get("firstName"));
            account.setLastName(userData.get("lastName"));
            account.setPhoneNumber(userData.get("phone"));
            account.setUsername(userData.get("username"));
            account.setBio(userData.get("bio"));
            account.setHashedPassword(PasswordUtil.hash("12345678")); // Default password for all
            account.setStatus(random.nextBoolean() ? AccountStatus.ONLINE : AccountStatus.OFFLINE);
            account.setEmail(userData.get("username") + "@example.com"); // Unique email
            daoManager.getAccountDAO().insert(account);
            seededAccounts.add(account);
            Console.log("Created account: " + account.getFirstName() + " " + account.getLastName());

            // Each user gets a "Saved Messages" chat
            SavedMessages savedMessages = new SavedMessages(account);
            savedMessages.setTitle("Saved Messages");
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
        // Group 1: Project Team
        Account projectLead = seededAccounts.get(0); // Alice
        List<Account> projectMembers = List.of(seededAccounts.get(1), seededAccounts.get(4), seededAccounts.get(7), seededAccounts.get(10));
        createGroupChat("🚀 Project Phoenix", "Weekly syncs and updates.", projectLead, projectMembers);

        // Group 2: Family
        Account parent1 = seededAccounts.get(2); // Charlie
        List<Account> familyMembers = List.of(seededAccounts.get(3), seededAccounts.get(5), seededAccounts.get(8));
        createGroupChat("👨‍👩‍👧‍👦 Family Reunion", "Planning for the annual get-together!", parent1, familyMembers);
    }

    private void seedChannels() {
        Console.print("\n--- Seeding Channels ---", Console.Color.BLUE);
        Account newsCreator = seededAccounts.get(9); // Jack Sparrow
        List<Account> subscribers = IntStream.range(0, 8).mapToObj(seededAccounts::get).collect(Collectors.toList());
        createChannel("Tech Today 📰", "Daily dose of technology news and updates.", newsCreator, subscribers, true);

        Account companyAnnouncer = seededAccounts.get(6); // George
        List<Account> employees = List.of(seededAccounts.get(0), seededAccounts.get(1), seededAccounts.get(4));
        createChannel("Vandelay Industries Memos", "Official company announcements.", companyAnnouncer, employees, false);
    }

    private void seedMessages() {
        Console.print("\n--- Seeding Messages ---", Console.Color.BLUE);
        List<String> sampleMessages = List.of(
                "Hey, how are you?", "Did you see the latest news?", "Let's catch up tomorrow.", "I'm running a bit late, sorry!",
                "Can you send me the file?", "Thanks!", "😂 That's hilarious!", "What's the plan for the weekend?", "I agree.",
                "Let me check and get back to you.", "Meeting at 3 PM.", "Don't forget the presentation.", "Lunch today?",
                "Good morning!", "See you soon."
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
                            .orElse(members.get(0).getAccount());
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
        Contact contact = new Contact();
        contact.setOwner(owner);
        contact.setContact(contactUser);
        contact.setSavedName(savedName);
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

    private void createGroupChat(String title, String description, Account owner, List<Account> members) {
        GroupChat group = new GroupChat(title, null, owner, description);
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

    private void createChannel(String title, String description, Account owner, List<Account> members, boolean isPublic) {
        Channel channel = new Channel(title, null, owner, description, isPublic);
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