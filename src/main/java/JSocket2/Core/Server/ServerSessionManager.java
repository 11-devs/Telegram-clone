package JSocket2.Core.Server;

import JSocket2.Protocol.Message;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerSessionManager {
    private final Map<ClientHandler, ServerSession> sessions = new ConcurrentHashMap<>();

    public ServerSession createSession(ClientHandler clientHandler) {
        ServerSession serverSession = new ServerSession(clientHandler,this);
        sessions.put(clientHandler, serverSession);
        return serverSession;
    }

    public ServerSession getSession(ClientHandler clientHandler) {
        return sessions.get(clientHandler);
    }

    public void removeSession(ClientHandler clientHandler) {
        sessions.remove(clientHandler);
    }
    public void closeAll() throws IOException {
        for(var socket: sessions.keySet()){
            removeSession(socket);
        }
    }
    private final ConcurrentMap<String, CopyOnWriteArrayList<ServerSession>> userSessions = new ConcurrentHashMap<>();

    void indexSessionForUser(ServerSession sess, String userId) {
        userSessions
                .computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>())
                .add(sess);
    }

    void deindexSessionForUser(ServerSession sess, String userId) {
        var list = userSessions.get(userId);
        if (list != null) {
            list.remove(sess);
            if (list.isEmpty()) {
                userSessions.remove(userId);
            }
        }
    }

    public void publishMessage(String userId, Message msg) throws IOException {
        var list = userSessions.get(userId);
        if (list != null) {
            for (var sess : list) {
                sess.getClientHandler().send(msg);
            }
        }
    }
}
