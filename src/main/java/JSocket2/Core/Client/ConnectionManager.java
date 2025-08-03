package JSocket2.Core.Client;

import JSocket2.Protocol.IConnectionEventListener;
import javafx.beans.value.ChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ConnectionManager implements IConnectionEventListener {

    private String host;
    private int port;

    private ClientApplication app;
    private ConnectionManagerOptions options = new ConnectionManagerOptions();
    private ClientApplicationBuilder builder = new ClientApplicationBuilder();

    private final List<ChangeListener<Boolean>> externalListeners = new ArrayList<>();
    private final List<Consumer<ClientApplication>> reconnectListeners = new ArrayList<>();
    Random random = new Random();

    private int tryCount = 0;
    private int currentRetryDelay = 0;
    public ConnectionManager(Consumer<ConnectionManagerOptions> optionsConsumer,ClientApplicationBuilder clientApplicationBuilder) {
        optionsConsumer.accept(options);
        builder = clientApplicationBuilder;
        createAndStartClient();
    }
    public ConnectionManager(Consumer<ConnectionManagerOptions> optionsConsumer,Consumer<ClientApplicationBuilder> clientApplicationBuilder) {
        optionsConsumer.accept(options);
        clientApplicationBuilder.accept(builder);
        createAndStartClient();
    }
    public void addReconnectListener(Consumer<ClientApplication> listener) {
        reconnectListeners.add(listener);
    }
    private void createAndStartClient() {
        builder.setConnectionEventListener(this);
        app = builder.Build();

        for (ChangeListener<Boolean> extListener : externalListeners) {
            app.connectedProperty().addListener(extListener);
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException ignored) {
            }
            while (!app.connect()) {
                try {
                    Thread.sleep(currentRetryDelay);
                    tryCount++;
                    if(tryCount >= options.getMaxTryCount_for_changeRetryDelay()) {
                        var delay = Math.min(options.getMaxRetryDelay(), (int) (currentRetryDelay * 1.5));
                        currentRetryDelay = random.nextInt((int) (delay * options.getCoefficient_jitter()),delay);
                        tryCount=0;
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
            tryCount = 0;
            currentRetryDelay = options.getMinRetryDelay();
            executor.shutdown();
        });
    }

    private void recreateClient() {
        shutdown();
        createAndStartClient();
        reconnectListeners.forEach(listener -> listener.accept(app));
    }

    public  void addConnectedListener(ChangeListener<Boolean> listener) {
        externalListeners.add(listener);
        if (app != null) {
            app.connectedProperty().addListener(listener);
        }
    }
    public  void removeConnectedListener(ChangeListener<Boolean> listener) {
        externalListeners.remove(listener);
        if (app != null) {
            app.connectedProperty().removeListener(listener);
        }
    }

    public ClientApplication getClient() {
        return app;
    }

    public void shutdown() {
        if (app != null) {
            app.shutdown();
            app = null;
        }
    }

    @Override
    public void onConnectionLost() {
        recreateClient();
    }
}

