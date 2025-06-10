package JSocket.Core.Client;

import JSocket.Protocol.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import JSocket.Protocol.Transfer.ClientFileTransferManager;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;

public class ClientApplication {
    private final String host;
    private final int port;
    private final BooleanProperty connectedProperty = new SimpleBooleanProperty(false);
    AtomicBoolean running = new AtomicBoolean(true);

    private Socket socket;
    private MessageHandler messageHandler;
    private ClientMessageProcessor messageProcessor;
    private MessageListener messageListener;
    private Thread listenerThread;
    private ClientSession clientSession;
    private ClientFileTransferManager fileTransferManager;
    private final ConcurrentMap<UUID, CompletableFuture<Message>> pendingRequests = new ConcurrentHashMap<>();
    private final UUID sessionId = UUID.randomUUID();
    private final ExecutorService backgroundExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });
    private final ConcurrentMap<String, Task<?>> activeTasks = new ConcurrentHashMap<>();
    private final IConnectionEventListener connectionEventListener;
    public ClientApplication(String host, int port,IConnectionEventListener connectionEventListener) {
        this.host = host;
        this.port = port;
        this.connectionEventListener = connectionEventListener;
    }
    public void registerTask(String Id, Task<?> task) {
        activeTasks.put(Id, task);
    }

    public void unregisterTask(String Id) {
        activeTasks.remove(Id);
    }
    public ExecutorService getBackgroundExecutor() {
        return backgroundExecutor;
    }

    public ConcurrentMap<String, Task<?>> getActiveTasks() {
        return activeTasks;
    }

    public BooleanProperty connectedProperty() {
        return connectedProperty;
    }

    public boolean isConnected() {
        return connectedProperty.get();
    }

    public boolean connect() {
        try {
            socket = new Socket(host, port);
            clientSession = new ClientSession();
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            messageHandler = new MessageHandler(in, out, clientSession);
            messageProcessor = new ClientMessageProcessor(messageHandler, clientSession, pendingRequests, getFileTransferManager());
            messageListener = new MessageListener(messageHandler, pendingRequests, messageProcessor, clientSession, connectionEventListener);

            listenerThread = new Thread(messageListener);
            listenerThread.setDaemon(true);
            listenerThread.start();

            running.set(true);
            Platform.runLater(() -> connectedProperty.set(true));

            doHandshake();
            return true;
        } catch (IOException e) {
            running.set(false);
            Platform.runLater(() -> connectedProperty.set(false));
            return false;
        }
    }

    private void doHandshake() {
        // TODO: Implement if needed
    }

    public void shutdown() {
        try {
            if (listenerThread != null && listenerThread.isAlive()) {
                listenerThread.interrupt();
            }
            for(var activeTask:activeTasks.values()){
                activeTask.cancel();
            }
            backgroundExecutor.shutdown();
            activeTasks.clear();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        } finally {
            Platform.runLater(() -> connectedProperty.set(false));
        }
    }

    private void notifyConnectionLost() {
        running.set(false);
        Platform.runLater(() -> connectedProperty.set(false));
    }


    public ConcurrentMap<UUID, CompletableFuture<Message>> getPendingRequests() {
        return pendingRequests;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public ClientFileTransferManager getFileTransferManager() {
        if (fileTransferManager == null && messageHandler != null) {
            fileTransferManager = new ClientFileTransferManager(
                    messageHandler,
                    pendingRequests
            );
        }
        return fileTransferManager;
    }

    public void onConnectionLost() {
        notifyConnectionLost();
        shutdown();
    }
}
