// CREATE NEW FILE: main/java/JSocket2/Protocol/IClientLifecycleListener.java
package JSocket2.Core.Server;

/**
 * An interface for the application layer to listen to client lifecycle events
 * from the core server, such as connection, authentication, and disconnection.
 */
public interface IClientLifecycleListener {
    /**
     * Called when a client's session is successfully authenticated.
     * @param session The session of the newly authenticated user.
     */
    void onClientAuthenticated(ServerSession session);

    /**
     * Called when a client's connection is lost or terminated.
     * @param session The session of the client that disconnected.
     */
    void onClientDisconnected(ServerSession session);
}