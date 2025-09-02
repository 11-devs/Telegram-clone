package Client;

import JSocket2.Core.Client.ClientApplicationBuilder;
import JSocket2.Core.Client.ConnectionManager;

public class AppConnectionManager {

    private static AppConnectionManager instance;
    
    private ConnectionManager connectionManager;

    public RpcCaller getRpcCaller() {
        return rpcCaller;
    }

    private RpcCaller rpcCaller;
    private AppConnectionManager() {
        ClientApplicationBuilder builder = new ClientApplicationBuilder();
        builder.setEndpoint("localhost",8586);
        this.connectionManager = new ConnectionManager(options -> {},builder);
        this.rpcCaller = new RpcCaller(this.connectionManager);
    }

    public static AppConnectionManager getInstance() {
        if (instance == null) {
            instance = new AppConnectionManager();
        }
        return instance;
    }

    public ConnectionManager getConnectionManager() {
        return this.connectionManager;
    }
}