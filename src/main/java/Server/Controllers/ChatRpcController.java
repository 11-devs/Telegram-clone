package Server.Controllers;

import JSocket2.Protocol.Rpc.RpcControllerBase;
import JSocket2.Protocol.Rpc.RpcResponse;
import Server.DaoManager;
import Shared.Api.Models.ChatController.*;
import Shared.Models.Account.Account;
import Shared.Models.Chat.*;
import Shared.Models.Contact.Contact;
import Shared.Models.Media.Media;
import Shared.Models.Membership.Membership;
import Shared.Models.Membership.MembershipType;
import Shared.Models.Message.Message;
import Shared.Models.Message.TextMessage;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChatRpcController extends RpcControllerBase {
    private final DaoManager daoManager;

    public ChatRpcController(DaoManager daoManager) {
        this.daoManager = daoManager;
    }

    public RpcResponse<Object> getChatsByUser() {
        UUID currentUserId = UUID.fromString(getCurrentUser().getUserId());
        List<Membership> memberships = daoManager.getMembershipDAO()
                .findAllByField("account.id", currentUserId);

        List<GetChatInfoOutputModel> chatInfoList = memberships.stream()
                .map(this::mapToGetChatInfoOutputModel)
                .collect(Collectors.toList());

        return Ok(chatInfoList);
    }
    public RpcResponse<Object> createGroup(CreateGroupInputModel model) {
        UUID creatorId = UUID.fromString(getCurrentUser().getUserId());
        Account creator = daoManager.getAccountDAO().findById(creatorId);
        if (creator == null) {
            return BadRequest();
        }

        GroupChat groupChat = new GroupChat(model.getTitle(), model.getProfilePictureId(), creator, model.getDescription());
        daoManager.getGroupChatDAO().insert(groupChat);

        // Add creator as owner
        Membership ownerMembership = new Membership();
        ownerMembership.setAccount(creator);
        ownerMembership.setChat(groupChat);
        ownerMembership.setInvitedBy(null);
        ownerMembership.setType(MembershipType.OWNER);
        ownerMembership.setJoinDate(LocalDateTime.now());
        daoManager.getMembershipDAO().insert(ownerMembership);

        // Add initial members
        if(model.getMemberIds() != null) {
            for (UUID memberId : model.getMemberIds()) {
                if (!memberId.equals(creatorId)) {
                    Account member = daoManager.getEntityManager().find(Account.class,memberId);
                    if (member != null) {
                        Membership memberMembership = new Membership();
                        memberMembership.setAccount(member);
                        memberMembership.setChat(groupChat);
                        memberMembership.setInvitedBy(creator);
                        memberMembership.setType(MembershipType.MEMBER);
                        memberMembership.setJoinDate(LocalDateTime.now());
                        daoManager.getMembershipDAO().insert(memberMembership);
                        System.out.println("membership id: "+ memberMembership.getId() + ", chat id: "+ memberMembership.getChat().getId());
                    }
                }
            }
        }
        CreateGroupOutputModel output = new CreateGroupOutputModel(groupChat.getId(), groupChat.getType(), groupChat.getTitle(), groupChat.getProfilePictureId(), groupChat.getDescription(), creatorId, model.getMemberIds());
        return Ok(output);
    }

    public RpcResponse<Object> createChannel(CreateChannelInputModel model) {
        UUID creatorId = UUID.fromString(getCurrentUser().getUserId());
        Account creator = daoManager.getAccountDAO().findById(creatorId);
        if (creator == null) {
            return BadRequest();
        }

        Channel channel = new Channel(model.getTitle(), model.getProfilePictureId(), creator, model.getDescription(), model.isPublic());
        daoManager.getChannelDAO().insert(channel);

        Membership ownerMembership = new Membership();
        ownerMembership.setAccount(creator);
        ownerMembership.setChat(channel);
        ownerMembership.setInvitedBy(null);
        ownerMembership.setType(MembershipType.OWNER);
        daoManager.getMembershipDAO().insert(ownerMembership);

        CreateChannelOutputModel output = new CreateChannelOutputModel(channel.getId(), channel.getType(), channel.getTitle(), channel.getProfilePictureId(), channel.getDescription(), channel.isPublic(), creatorId);
        return Ok(output);
    }

    public RpcResponse<Object> updateChatInfo(UpdateChatInfoInputModel model) {
        UUID currentUserId = UUID.fromString(getCurrentUser().getUserId());
        Membership membership = daoManager.getMembershipDAO().findOneByJpql("SELECT m FROM Membership m WHERE m.chat.id = :chatId AND m.account.id = :accountId", q -> {
            q.setParameter("chatId", model.getChatId());
            q.setParameter("accountId", currentUserId);
        });

        if (membership == null || (membership.getType() != MembershipType.OWNER && membership.getType() != MembershipType.ADMIN)) {
            return Forbidden("You do not have permission to edit this chat's info.");
        }

        Chat chat = membership.getChat();
        chat.setTitle(model.getTitle());
        chat.setProfilePictureId(model.getProfilePictureId());

        daoManager.getChatDAO().update(chat);

        UpdateChatInfoOutputModel output = new UpdateChatInfoOutputModel();
        output.setId(chat.getId());
        output.setType(chat.getType());
        output.setTitle(chat.getTitle());
        output.setProfilePictureId(chat.getProfilePictureId());
        return Ok(output);
    }
    public RpcResponse<Object> getChatByUserId(UUID userId){
        UUID currentUserId = UUID.fromString(getCurrentUser().getUserId());
        Account currentUser = daoManager.getAccountDAO().findById(currentUserId);
        if (currentUser == null) {
            return BadRequest("Could not find current user account.");
        }

        Account targetUser = daoManager.getEntityManager().find(Account.class, userId);
        if (targetUser == null) {
            return NotFound();
        }

        PrivateChat privateChat = findOrCreatePrivateChat(currentUser, targetUser);
        if (privateChat == null) {
            return BadRequest("Failed to create or find private chat.");
        }

        GetChatInfoOutputModel output = mapNewPrivateChatToOutput(privateChat, targetUser);
        return Ok(output);
    }
    public RpcResponse<Object> deleteChat(UUID chatId) {
        UUID currentUserId = UUID.fromString(getCurrentUser().getUserId());
        Membership membership = daoManager.getMembershipDAO().findOneByJpql("SELECT m FROM Membership m WHERE m.chat.id = :chatId AND m.account.id = :accountId", q -> {
            q.setParameter("chatId", chatId);
            q.setParameter("accountId", currentUserId);
        });

        if (membership == null || membership.getType() != MembershipType.OWNER) {
            return Forbidden("Only the owner can delete the chat.");
        }

        daoManager.getChatDAO().delete(membership.getChat());
        return Ok("Chat deleted successfully.");
    }

    public RpcResponse<Object> toggleChatMute(ToggleChatMuteInputModel model) {
        UUID currentUserId = UUID.fromString(getCurrentUser().getUserId());

        Membership membership = daoManager.getMembershipDAO().findOneByJpql(
                "SELECT m FROM Membership m WHERE m.chat.id = :chatId AND m.account.id = :accountId",
                query -> {
                    query.setParameter("chatId", model.getChatId());
                    query.setParameter("accountId", currentUserId);
                }
        );

        if (membership == null) {
            return NotFound();
        }

        membership.setMuted(model.isMuted());
        daoManager.getMembershipDAO().update(membership);

        return Ok();
    }
    public RpcResponse<Object> getChatByUsername(String username) {
        UUID currentUserId = UUID.fromString(getCurrentUser().getUserId());
        Account targetUser = daoManager.getAccountDAO().findByField("username", username.toLowerCase());

        if (targetUser == null) {
            return NotFound();
        }

        Account currentUser = daoManager.getAccountDAO().findById(currentUserId);
        if (currentUser == null) {
            return BadRequest("Could not find current user account.");
        }

        if (targetUser.getId().equals(currentUserId)) {
            // Request for "Saved Messages"
            SavedMessages savedMessages = findOrCreateSavedMessages(currentUser);
            GetChatInfoOutputModel output = new GetChatInfoOutputModel();
            output.setChatId(savedMessages.getId());
            output.setType(savedMessages.getType().toString());
            output.setTitle("Saved Messages");
            output.setLastMessage("");
            output.setUnreadCount(0);
            return Ok(output);
        } else {
            // Request for a private chat with another user
            PrivateChat privateChat = findOrCreatePrivateChat(currentUser, targetUser);
            if (privateChat == null) {
                return BadRequest("Failed to create or find private chat.");
            }
            GetChatInfoOutputModel output = mapNewPrivateChatToOutput(privateChat, targetUser);
            return Ok(output);
        }
    }

    public RpcResponse<Object> getSavedMessage(){
        UUID currentUserId = UUID.fromString(getCurrentUser().getUserId());
        Account account = daoManager.getAccountDAO().findById(currentUserId);
        if (account == null) {
            return BadRequest("User not found.");
        }

        SavedMessages chat = findOrCreateSavedMessages(account);

        GetChatInfoOutputModel output = new GetChatInfoOutputModel();
        output.setChatId(chat.getId());
        output.setType(chat.getType().toString());
        output.setTitle("Saved Messages");
        output.setLastMessage("");
        output.setUnreadCount(0);
        return Ok(output);
    }
    public RpcResponse<GetChatInfoOutputModel[]> searchPublic(String query) {
        UUID currentUserId = UUID.fromString(getCurrentUser().getUserId());
        String searchQuery = "%" + query.toLowerCase() + "%";
        List<GetChatInfoOutputModel> results = new ArrayList<>();

        // 1. Find accounts
        String accountJpql = "SELECT a FROM Account a WHERE a.id != :currentUserId AND (LOWER(a.username) LIKE :query OR LOWER(CONCAT(a.firstName, ' ', a.lastName)) LIKE :query)";
        List<Account> foundAccounts = daoManager.getAccountDAO().findByJpql(accountJpql, q -> {
            q.setParameter("currentUserId", currentUserId);
            q.setParameter("query", searchQuery);
            q.setMaxResults(10);
        });
        foundAccounts.stream()
                .map(this::mapAccountToSearchOutput)
                .forEach(results::add);

        // 2. Find public channels
        String channelJpql = "SELECT c FROM Channel c WHERE c.isPublic = true AND LOWER(c.title) LIKE :query";
        List<Channel> foundChannels = daoManager.getChannelDAO().findByJpql(channelJpql, q -> {
            q.setParameter("query", searchQuery);
            q.setMaxResults(10);
        });
        foundChannels.stream()
                .map(this::mapPublicChatToSearchOutput)
                .forEach(results::add);

        // 3. Find groups
        String groupJpql = "SELECT g FROM GroupChat g WHERE LOWER(g.title) LIKE :query";
        List<GroupChat> foundGroups = daoManager.getGroupChatDAO().findByJpql(groupJpql, q -> {
            q.setParameter("query", searchQuery);
            q.setMaxResults(10);
        });
        foundGroups.stream()
                .map(this::mapPublicChatToSearchOutput)
                .forEach(results::add);

        return Ok(results.toArray(new GetChatInfoOutputModel[0]));
    }

    // <editor-fold desc="Private Helper Methods">

    /**
     * Maps a Membership entity to a GetChatInfoOutputModel, populating all necessary fields.
     * This is the main orchestrator for building chat list items.
     */
    private GetChatInfoOutputModel mapToGetChatInfoOutputModel(Membership membership) {
        UUID currentUserId = UUID.fromString(getCurrentUser().getUserId());
        Chat chat = membership.getChat();
        GetChatInfoOutputModel output = new GetChatInfoOutputModel();

        output.setChatId(chat.getId());
        output.setUserId(membership.getAccount().getId());
        output.setType(chat.getType().toString());
        output.setUserMembershipType(membership.getType().toString());

        if (chat.getType() == ChatType.PRIVATE) {
            populatePrivateChatInfo(output, chat, currentUserId);
        } else {
            populateGroupOrChannelInfo(output, chat);
        }

        populateLastMessageInfo(output, chat);
        populateUnreadCount(output, membership, currentUserId);
        output.setMuted(membership.isMuted());

        return output;
    }

    /**
     * Populates fields specific to a private chat (the other user's info).
     */
    private void populatePrivateChatInfo(GetChatInfoOutputModel output, Chat chat, UUID currentUserId) {
        List<Membership> chatMembers = daoManager.getMembershipDAO().findAllByField("chat.id", chat.getId());
        Optional<Account> otherUserOpt = chatMembers.stream()
                .map(Membership::getAccount)
                .filter(account -> !account.getId().equals(currentUserId))
                .findFirst();

        if (otherUserOpt.isPresent()) {
            Account otherUser = otherUserOpt.get();

            // Check if the other user is a contact and set title accordingly
            Optional<Contact> contactOpt = findContact(currentUserId, otherUser.getId());
            String chatTitle;
            if (contactOpt.isPresent() && contactOpt.get().getSavedName() != null && !contactOpt.get().getSavedName().trim().isEmpty()) {
                chatTitle = contactOpt.get().getSavedName();
                output.setContact(true);
            } else {
                chatTitle = otherUser.getFirstName() + (otherUser.getLastName() != null ? " " + otherUser.getLastName() : "");
                output.setContact(false);
            }
            output.setTitle(chatTitle.trim());

            output.setPhoneNumber(otherUser.getPhoneNumber());
            output.setUsername(otherUser.getUsername());
            output.setBio(otherUser.getBio());
            output.setOnline(getServerSessionManager().isUserOnline(otherUser.getId().toString()));
            if (otherUser.getLastSeen() != null) {
                output.setLastSeen(otherUser.getLastSeen().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            output.setProfilePictureId(getFileIdFromMediaId(otherUser.getProfilePictureId()));
        } else {
            output.setTitle("Deleted Account");
        }
    }

    /**
     * Populates fields for group or channel chats (title and profile picture).
     */
    private void populateGroupOrChannelInfo(GetChatInfoOutputModel output, Chat chat) {
        output.setTitle(chat.getTitle());
        if(chat.getType() == ChatType.CHANNEL){
            var channel = daoManager.getEntityManager().find(Channel.class,chat.getId());
            output.setBio(channel.getDescription());
        }
        else if(chat.getType() == ChatType.GROUP){
            var groupChat = daoManager.getEntityManager().find(GroupChat.class,chat.getId());
            output.setBio(groupChat.getDescription());
        }
        List<Membership> memberships = daoManager.getMembershipDAO().findAllByField("chat.id",chat.getId());
        if(memberships != null)
        output.setMemberCount(memberships.size());
        output.setProfilePictureId(getFileIdFromMediaId(chat.getProfilePictureId()));
    }

    /**
     * Finds the last message in a chat and populates the output model with its content and timestamp.
     */
    private void populateLastMessageInfo(GetChatInfoOutputModel output, Chat chat) {
        Message lastMessage = daoManager.getMessageDAO().findOneByJpql(
                "SELECT m FROM Message m JOIN FETCH m.sender WHERE m.chat.id = :chatId AND m.isDeleted = false ORDER BY m.timestamp DESC",
                query -> {
                    query.setParameter("chatId", chat.getId());
                    query.setMaxResults(1);
                });

        if (lastMessage == null) {
            output.setLastMessage("");
            return;
        }

        String lastMessageText;
        switch (lastMessage.getType()) {
            case TEXT -> {
                TextMessage textMessage = daoManager.getEntityManager().find(TextMessage.class, lastMessage.getId());
                lastMessageText = (textMessage != null) ? textMessage.getTextContent() : "...";
            }
            case MEDIA -> lastMessageText = "Media";
            case VOICE -> lastMessageText = "Voice Message";
            case VIDEO -> lastMessageText = "Video";
            default -> lastMessageText = "...";
        }
        output.setLastMessage(lastMessageText);
        output.setLastMessageTimestamp(lastMessage.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        if (lastMessage.getSender() != null) {
            output.setLastMessageSenderName(lastMessage.getSender().getFirstName());
        }
    }

    /**
     * Calculates the number of unread messages for the current user in a chat.
     */
    private void populateUnreadCount(GetChatInfoOutputModel output, Membership membership, UUID currentUserId) {
        LocalDateTime lastReadTimestamp = null;
        try {
            TypedQuery<LocalDateTime> tsQuery = daoManager.getEntityManager().createQuery(
                    "SELECT msg.timestamp FROM Membership mem JOIN mem.lastReadMessage msg " +
                            "WHERE mem.id = :membershipId AND msg.isDeleted = false", LocalDateTime.class);
            tsQuery.setParameter("membershipId", membership.getId());
            lastReadTimestamp = tsQuery.getSingleResult();
        } catch (NoResultException e) {
            // Expected if last read message was deleted or never existed.
        }

        long unreadCount;
        if (lastReadTimestamp == null) {
            String jpql = "SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId AND m.sender.id != :senderId AND m.isDeleted = false";
            unreadCount = daoManager.getMessageDAO().countByJpql(jpql, query -> {
                query.setParameter("chatId", membership.getChat().getId());
                query.setParameter("senderId", currentUserId);
            });
        } else {
            final LocalDateTime finalLastReadTimestamp = lastReadTimestamp;
            String jpql = "SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId AND m.timestamp > :lastReadTimestamp AND m.sender.id != :senderId AND m.isDeleted = false";
            unreadCount = daoManager.getMessageDAO().countByJpql(jpql, query -> {
                query.setParameter("chatId", membership.getChat().getId());
                query.setParameter("lastReadTimestamp", finalLastReadTimestamp);
                query.setParameter("senderId", currentUserId);
            });
        }
        output.setUnreadCount((int) unreadCount);
    }

    /**
     * Finds an existing private chat between two users or creates a new one if it doesn't exist.
     */
    private PrivateChat findOrCreatePrivateChat(Account user1, Account user2) {
        String jpql = "SELECT pc FROM PrivateChat pc WHERE " +
                "(pc.user1.id = :user1Id AND pc.user2.id = :user2Id) OR " +
                "(pc.user1.id = :user2Id AND pc.user2.id = :user1Id)";

        PrivateChat privateChat = daoManager.getPrivateChatDAO().findOneByJpql(jpql, query -> {
            query.setParameter("user1Id", user1.getId());
            query.setParameter("user2Id", user2.getId());
        });

        if (privateChat == null) {
            privateChat = new PrivateChat(user1, user2);
            daoManager.getPrivateChatDAO().insert(privateChat);

            Membership m1 = new Membership();
            m1.setAccount(user1);
            m1.setChat(privateChat);
            m1.setType(MembershipType.MEMBER);
            m1.setJoinDate(LocalDateTime.now());
            daoManager.getMembershipDAO().insert(m1);

            Membership m2 = new Membership();
            m2.setAccount(user2);
            m2.setChat(privateChat);
            m2.setType(MembershipType.MEMBER);
            m2.setJoinDate(LocalDateTime.now());
            daoManager.getMembershipDAO().insert(m2);
        }
        return privateChat;
    }

    /**
     * Finds the "Saved Messages" chat for a user or creates it if it doesn't exist.
     */
    private SavedMessages findOrCreateSavedMessages(Account owner) {
        String jpql = "SELECT sm FROM SavedMessages sm WHERE sm.owner.id = :ownerId";
        SavedMessages savedMessages = daoManager.getSavedMessagesDAO().findOneByJpql(jpql, query -> query.setParameter("ownerId", owner.getId()));

        if (savedMessages == null) {
            savedMessages = new SavedMessages(owner);
            daoManager.getSavedMessagesDAO().insert(savedMessages);
        }
        return savedMessages;
    }

    /**
     * Finds a contact relationship between two users.
     *
     * @param ownerId The ID of the user whose contact list is being checked.
     * @param contactUserId The ID of the user to check for in the contact list.
     * @return An Optional containing the Contact if found, otherwise an empty Optional.
     */
    private Optional<Contact> findContact(UUID ownerId, UUID contactUserId) {
        String jpql = "SELECT c FROM Contact c WHERE c.owner.id = :ownerId AND c.contact.id = :contactUserId";
        Contact contact = daoManager.getContactDAO().findOneByJpql(jpql, query -> {
            query.setParameter("ownerId", ownerId);
            query.setParameter("contactUserId", contactUserId);
        });
        return Optional.ofNullable(contact);
    }

    /**
     * Maps a newly created/fetched PrivateChat to a basic GetChatInfoOutputModel.
     */
    private GetChatInfoOutputModel mapNewPrivateChatToOutput(PrivateChat chat, Account otherUser) {
        GetChatInfoOutputModel output = new GetChatInfoOutputModel();
        output.setChatId(chat.getId());
        output.setType(chat.getType().toString());
        String otherUserName = otherUser.getFirstName() + (otherUser.getLastName() != null ? " " + otherUser.getLastName() : "");
        output.setTitle(otherUserName.trim());
        output.setProfilePictureId(getFileIdFromMediaId(otherUser.getProfilePictureId()));
        output.setLastMessage("");
        output.setUnreadCount(0);
        return output;
    }

    /**
     * Maps an Account entity to a GetChatInfoOutputModel for public search results.
     */
    private GetChatInfoOutputModel mapAccountToSearchOutput(Account account) {
        GetChatInfoOutputModel output = new GetChatInfoOutputModel();
        output.setChatId(account.getId()); // Note: This is the Account ID for search results
        output.setType(ChatType.PRIVATE.toString());
        output.setTitle(account.getFirstName() + " " + account.getLastName());
        output.setUsername(account.getUsername());
        output.setProfilePictureId(getFileIdFromMediaId(account.getProfilePictureId()));
        output.setLastMessage("@" + account.getUsername()); // Use lastMessage for subtitle
        output.setOnline(getServerSessionManager().isUserOnline(account.getId().toString()));
        if (account.getLastSeen() != null) {
            output.setLastSeen(account.getLastSeen().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        return output;
    }

    /**
     * Maps a public Chat (Group or Channel) to a GetChatInfoOutputModel for search results.
     */
    private GetChatInfoOutputModel mapPublicChatToSearchOutput(Chat chat) {
        GetChatInfoOutputModel output = new GetChatInfoOutputModel();
        output.setChatId(chat.getId());
        output.setType(chat.getType().toString());
        output.setTitle(chat.getTitle());
        output.setProfilePictureId(getFileIdFromMediaId(chat.getProfilePictureId()));
        long memberCount = daoManager.getMembershipDAO().countByJpql(
                "SELECT COUNT(m) FROM Membership m WHERE m.chat.id = :chatId",
                q -> q.setParameter("chatId", chat.getId()));
        String subtitle = (chat.getType() == ChatType.CHANNEL) ? "subscribers" : "members";
        output.setLastMessage(memberCount + " " + subtitle); // Use lastMessage for subtitle
        return output;
    }

    /**
     * Safely retrieves a media file ID from a media entity ID string.
     */
    private String getFileIdFromMediaId(String mediaIdStr) {
        if (mediaIdStr == null || mediaIdStr.trim().isEmpty()) {
            return null;
        }
        try {
            Media media = daoManager.getEntityManager().find(Media.class, UUID.fromString(mediaIdStr));
            return (media != null) ? media.getFileId() : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    // </editor-fold>
}