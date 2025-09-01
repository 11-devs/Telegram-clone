package JSocket2.Core.Server;

import JSocket2.Cryptography.RsaKeyManager;
import JSocket2.DI.ServiceCollection;
import JSocket2.Protocol.Authentication.IAuthService;
import JSocket2.Protocol.Rpc.RpcControllerCollection;

import java.io.IOException;

public class ServerApplicationBuilder {
    private int port = 8080;
    private final RpcControllerCollection rpcControllerCollection;
    private final ServiceCollection services;
    private boolean setAuth = false;
    public ServerApplicationBuilder(){
        services = new ServiceCollection();
        rpcControllerCollection = new RpcControllerCollection();
        services.AddSingleton(ServerSessionManager.class);
        services.AddSingleton(RsaKeyManager.class);
    }
    public ServerApplicationBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public ServiceCollection getServices(){
        return services;
    }
    public ServerApplicationBuilder setAuthService(Class<?> authService) {
        services.AddScoped(IAuthService.class,authService);
        setAuth = true;
        return this;
    }
    public <T> ServerApplicationBuilder addController(Class<T> controllerType) {
        this.services.AddSingleton(controllerType);
        this.rpcControllerCollection.registerController(controllerType);
        return this;
    }

    public ServerApplication build() throws IOException {
        if(!canBuild()){
            throw new RuntimeException("Can't build ServerApplication");
        }
        return new ServerApplication(port, rpcControllerCollection,services);
    }
    private boolean canBuild(){
        return setAuth && port > 1023 && port < 49151;
    }
}
