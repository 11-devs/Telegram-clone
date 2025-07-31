package JSocket2.Core.Server;

import JSocket2.Core.Session;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServerSession extends Session {
    private final ServerSessionManager serverSessionManager;
    private final ClientHandler clientHandler;
    private final Set<String> subscribedUserIds = ConcurrentHashMap.newKeySet();
    private boolean isAuthorized = false;

    public ServerSession(ClientHandler clientHandler, ServerSessionManager serverSessionManager) {
        super();
        this.clientHandler = clientHandler;
        this.serverSessionManager = serverSessionManager;
    }
    public ClientHandler getClientHandler(){
        return clientHandler;
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
        //socket.close();
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public void setAESKey(SecretKey aesKey) {
        super.aesKey =aesKey;
    }
}

