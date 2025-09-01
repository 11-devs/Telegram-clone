package JSocket2.Core.Server;

import JSocket2.Core.Session;
import JSocket2.Protocol.Authentication.UserIdentity;


import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerSession extends Session {
    private final ServerSessionManager serverSessionManager;
    private final ClientHandler clientHandler;
    private final Map<String, UserIdentity> subscribedUsers = new ConcurrentHashMap<>();
    private UserIdentity activeUser = null;
    private boolean isAuthorized = false;

    public ServerSession(ClientHandler clientHandler, ServerSessionManager serverSessionManager) {
        super();
        this.clientHandler = clientHandler;
        this.serverSessionManager = serverSessionManager;
    }

    public ClientHandler getClientHandler(){
        return clientHandler;
    }

    public void subscribeUser(UserIdentity user) {
        if (subscribedUsers.put(user.getUserId(), user) == null) {
            serverSessionManager.indexSessionForUser(this, user.getUserId());
            setActiveUser(user);
        }
        isAuthorized = true;
    }

    public void unsubscribeUser(String userId) {
        if (subscribedUsers.remove(userId) != null) {
            serverSessionManager.deindexSessionForUser(this, userId);
        }
        if (activeUser != null && userId.equals(activeUser.getUserId())) {
            activeUser = null;
        }
        if (subscribedUsers.isEmpty()) {
            isAuthorized = false;
        }
    }

    public void close() throws IOException {
        for (var userId : subscribedUsers.keySet()) {
            serverSessionManager.deindexSessionForUser(this, userId);
        }
        subscribedUsers.clear();
        activeUser = null;
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public void setAESKey(SecretKey aesKey) {
        super.aesKey = aesKey;
    }

    public void setActiveUser(UserIdentity user) {
        if (!subscribedUsers.containsKey(user.getUserId())) {
            throw new IllegalArgumentException("User not subscribed to this session.");
        }
        this.activeUser = user;
    }

    public UserIdentity getActiveUser() {
        return this.activeUser;
    }
}
