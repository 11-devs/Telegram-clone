// CREATE NEW FILE: main/java/Server/ClientLifecycleManager.java
package Server;

import JSocket2.Core.Server.ServerSession;
import JSocket2.Core.Server.ServerSessionManager;
import JSocket2.DI.Inject;
import JSocket2.Core.Server.IClientLifecycleListener;
import Server.Events.UserStatusChangedEvent;
import Shared.Events.Models.UserStatusChangedEventModel;
import Shared.Models.Account.Account;
import Shared.Models.Membership.Membership;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class ClientLifecycleManager implements IClientLifecycleListener {

    private final DaoManager daoManager;
    private final UserStatusChangedEvent userStatusChangedEvent;
    private ServerSessionManager serverSessionManager;
    @Inject
    public ClientLifecycleManager(DaoManager daoManager, UserStatusChangedEvent userStatusChangedEvent) {
        this.daoManager = daoManager;
        this.userStatusChangedEvent = userStatusChangedEvent;
    }

    @Override
    public void onClientAuthenticated(ServerSession session) {
        if (session == null || session.getActiveUser() == null) return;
        serverSessionManager = session.getServerSessionManager();
        UUID currentUserId = UUID.fromString(session.getActiveUser().getUserId());
        UserStatusChangedEventModel onlineEvent = new UserStatusChangedEventModel(currentUserId, true, null);

        broadcastStatusUpdate(currentUserId, onlineEvent);
    }

    @Override
    public void onClientDisconnected(ServerSession session) {
        if (session == null || session.getActiveUser() == null) return;

        UUID disconnectedUserId = UUID.fromString(session.getActiveUser().getUserId());
        Account userAccount = daoManager.getAccountDAO().findById(disconnectedUserId);

        if (userAccount != null) {
            LocalDateTime now = LocalDateTime.now();
            userAccount.setLastSeen(now);
            daoManager.getAccountDAO().update(userAccount);

            UserStatusChangedEventModel offlineEvent = new UserStatusChangedEventModel(
                    disconnectedUserId,
                    false,
                    now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            broadcastStatusUpdate(disconnectedUserId, offlineEvent);
        }
    }

    private void broadcastStatusUpdate(UUID userId, UserStatusChangedEventModel event) {
        // This logic finds every chat the user is in and notifies every *other* member of that chat.
        List<Membership> userMemberships = daoManager.getMembershipDAO().findAllByField("account.id", userId);
        for (Membership userMembership : userMemberships) {
            List<Membership> chatPeers = daoManager.getMembershipDAO().findAllByField("chat.id", userMembership.getChat().getId());
            for (Membership peer : chatPeers) {
                // Don't send the notification to the user themselves
                if (!peer.getAccount().getId().equals(userId)) {
                    try {
                        userStatusChangedEvent.Invoke(
                                serverSessionManager,
                                peer.getAccount().getId().toString(),
                                event
                        );
                    } catch (IOException ex) {
                        System.err.println("Failed to send status event to " + peer.getAccount().getId() + ": " + ex.getMessage());
                    }
                }
            }
        }
    }
}