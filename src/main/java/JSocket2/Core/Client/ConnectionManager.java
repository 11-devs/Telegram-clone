package JSocket2.Core.Client;

import JSocket2.Core.Client.ClientApplication;
import JSocket2.Protocol.IConnectionEventListener;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionManager implements IConnectionEventListener {

    private final String host;
    private final int port;

    private ClientApplication app;


    private final List<ChangeListener<Boolean>> externalListeners = new ArrayList<>();

    private int minRetryDelay = 3000;
    private int maxRetryDelay = 15000;
    private int maxTryCount_for_changeRetryDeley = 5;
    private int tryCount = 0;
    private int currentRetryDelay = minRetryDelay;
    public ConnectionManager(String host, int port) {
        this.host = host;
        this.port = port;

        createAndStartClient();
    }

    private void createAndStartClient() {

        app = new ClientApplication(host, port,this);

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
                    if(tryCount >= maxTryCount_for_changeRetryDeley) {
                        currentRetryDelay = Math.min(maxRetryDelay, (int) (currentRetryDelay * 1.5));
                        tryCount=0;
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
            tryCount = 0;
            currentRetryDelay = minRetryDelay;
            executor.shutdown();
        });
    }

    private void recreateClient() {
        shutdown();
        createAndStartClient();
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

