package JSocket.Core.Server;

import JSocket.Core.Session;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServerSession extends Session {
    private final ServerSessionManager serverSessionManager;
    private final Socket socket;
    private final Set<String> subscribedUserIds = ConcurrentHashMap.newKeySet();
    private boolean isAuthorized = false;

    public ServerSession(Socket socket, ServerSessionManager serverSessionManager) {
        super();
        this.socket = socket;
        this.serverSessionManager = serverSessionManager;
    }

    public void subscribeUser(String userId) {
        if (subscribedUserIds.add(userId)) {
            serverSessionManager.indexSessionForUser(this, userId);
        }
        isAuthorized = true;
    }


    public void unsubscribeUser(String userId) {
        if (subscribedUserIds.remove(userId)) {
            serverSessionManager.deindexSessionForUser(this, userId);
        }
        if(subscribedUserIds.isEmpty()){
            isAuthorized = false;
        }
    }


    public void close() throws IOException {

        for (var userId : subscribedUserIds) {
            serverSessionManager.deindexSessionForUser(this, userId);
        }
        socket.close();
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public void setAESKey(SecretKey aesKey) {
        super.aesKey =aesKey;
    }
}

