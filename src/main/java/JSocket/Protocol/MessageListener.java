package JSocket.Protocol;

import JSocket.Core.Session;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class MessageListener implements Runnable {

    private final MessageHandler messageHandler;
    private final Map<UUID, CompletableFuture<Message>> pendingRequests;
    private final IMessageProcessor messageProcessor;
    private final Session session;
    private final IConnectionEventListener connectionEventListener;
    private volatile boolean running = true;

    public MessageListener(
            MessageHandler messageHandler,
            Map<UUID, CompletableFuture<Message>> pendingRequests,
            IMessageProcessor messageProcessor,
            Session session,
            IConnectionEventListener connectionEventListener

    ) {
        this.messageHandler = messageHandler;
        this.pendingRequests = pendingRequests;
        this.messageProcessor = messageProcessor;
        this.session = session;
        this.connectionEventListener = connectionEventListener;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Message message = messageHandler.read();
                if (message.header.uuid != null &&
                        pendingRequests.containsKey(message.header.uuid)) {
                    CompletableFuture<Message> future = pendingRequests.remove(message.header.uuid);
                    future.complete(message);
                } else {
                    messageProcessor.Invoke(message);
                }

            } catch (IOException e) {
                if (connectionEventListener != null) {
                    connectionEventListener.onConnectionLost();
                }
                stop();
            }
            catch (Exception e) {
                e.printStackTrace();
                stop();
            }
        }
    }

    public void stop() {
        running = false;
    }
}
