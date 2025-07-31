package JSocket2.Core.Server;

import JSocket2.DI.ServiceCollection;
import JSocket2.Protocol.Authentication.AuthService;
import JSocket2.Protocol.Rpc.RpcControllerCollection;

import java.io.IOException;

public class ServerApplicationBuilder {
    private int port = 8080;
    private final RpcControllerCollection rpcControllerCollection = new RpcControllerCollection();
    private ServiceCollection services;
    private AuthService authService = null;
    public ServerApplicationBuilder setPort(int port) {
        this.port = port;
        services = new ServiceCollection();
        return this;
    }

    public ServiceCollection getServices(){
        return services;
    }
    public ServerApplicationBuilder setAuthService(AuthService authService) {
        this.authService = authService;
        return this;
    }
    public <T> ServerApplicationBuilder addController(Class<T> controllerType) {
        this.services.AddSingleton(controllerType);
        this.rpcControllerCollection.registerController(controllerType);
        this.rpcControllerCollection.registerActions(controllerType);
        return this;
    }
    public ServerApplication build() throws IOException {
        if(!canBuild()){
            throw new RuntimeException("Can't build ServerApplication");
        }
        return new ServerApplication(port, rpcControllerCollection,authService,services);
    }
    private boolean canBuild(){
        return authService != null && port > 1023 && port < 49151;
    }
}
