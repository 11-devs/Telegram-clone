package JSocket2.Core.Server;

import JSocket2.Protocol.Message;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerSessionManager {
    private final Map<Socket, ServerSession> sessions = new ConcurrentHashMap<>();

    public ServerSession createSession(Socket socket) {
        ServerSession serverSession = new ServerSession(socket,this);
        sessions.put(socket, serverSession);
        return serverSession;
    }

    public ServerSession getSession(Socket socket) {
        return sessions.get(socket);
    }

    public void removeSession(Socket socket) {
        sessions.remove(socket);
    }
    public void closeAll() throws IOException {
        for(var socket: sessions.keySet()){
            socket.close();
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

    public void pushToUser(String userId, Message msg) {
        var list = userSessions.get(userId);
        if (list != null) {
            for (var sess : list) {
                //sess.send(msg);
            }
        }
    }
}
