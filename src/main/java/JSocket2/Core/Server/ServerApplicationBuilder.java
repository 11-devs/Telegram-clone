package JSocket2.Core.Server;

import JSocket2.Protocol.Authentication.AuthService;
import JSocket2.Protocol.Rpc.RpcDispatcher;

import java.io.IOException;

public class ServerApplicationBuilder {
    private int port = 8080;
    private final RpcDispatcher rpcDispatcher = new RpcDispatcher();
    private AuthService authService = null;
    public ServerApplicationBuilder setPort(int port) {
        this.port = port;
        return this;
    }
    public ServerApplicationBuilder setAuthService(AuthService authService) {
        this.authService = authService;
        return this;
    }
    public <T> ServerApplicationBuilder addController(String controller, Object controllerInstance) {
        this.rpcDispatcher.registerController(controller,controllerInstance);
        return this;
    }
    public <T> ServerApplicationBuilder addAction(String controller, String action, Class<T> modelType) {
        this.rpcDispatcher.registerAction(controller,action,modelType);
        return this;
    }
    public <T> ServerApplicationBuilder addAction(String controller, String action) {
        this.rpcDispatcher.registerAction(controller,action,null);
        return this;
    }
    public ServerApplication build() throws IOException {
        if(!canBuild()){
            throw new RuntimeException("Can't build ServerApplication");
        }
        return new ServerApplication(port, rpcDispatcher,authService);
    }
    private boolean canBuild(){
        return authService != null && port > 1023 && port < 49151;
    }
}
