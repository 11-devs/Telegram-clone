package Client;

import Client.Services.UI.ChatUIService;
import Client.Subscribers.*;
import JSocket2.Core.Client.ClientApplicationBuilder;
import JSocket2.Core.Client.ConnectionManager;

public class AppConnectionManager {

    private static AppConnectionManager instance;

    private final ConnectionManager connectionManager;
    private final RpcCaller rpcCaller;

    public RpcCaller getRpcCaller() {
        return rpcCaller;
    }

    private AppConnectionManager() {
        // 1. Setup DI container


        // 2. Configure the client builder
        ClientApplicationBuilder builder = new ClientApplicationBuilder();
        builder.setEndpoint("localhost", 8586);
        builder.addEventSubscriber(NewMessageSubscriber.class);
        builder.addEventSubscriber(MessageEditedSubscriber.class);
        builder.addEventSubscriber(MessageDeletedSubscriber.class);
        builder.addEventSubscriber(MessageReadSubscriber.class);
        builder.addEventSubscriber(UserTypingSubscriber.class);
        builder.addEventSubscriber(UserStatusChangedSubscriber.class);
        builder.getServices().AddSingleton(ChatUIService.class);

        // 3. Conceptually, the builder is enhanced to use the DI and event system.
        //    The framework would use this provider to create subscriber instances
        //    and an EventBroker to route incoming events.
        //
        //    builder.useServiceProvider(this.serviceProvider);
        //    builder.addSubscriber(NewMessageSubscriber.class);

        this.connectionManager = new ConnectionManager(options -> {}, builder);
        this.rpcCaller = new RpcCaller(this.connectionManager);
    }

    public static synchronized AppConnectionManager getInstance() {
        if (instance == null) {
            instance = new AppConnectionManager();
        }
        return instance;
    }

    public ConnectionManager getConnectionManager() {
        return this.connectionManager;
    }

}