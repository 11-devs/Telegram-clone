package JSocket2.Example;

import JSocket2.Protocol.Authentication.AuthService;
import JSocket2.Core.Server.ServerApplication;
import JSocket2.Core.Server.ServerApplicationBuilder;
import JSocket2.Example.Controllers.LoginRpcController;

import java.io.IOException;

public class Server {

    public static void main(String[] args) throws IOException {
        ServerApplicationBuilder builder = new ServerApplicationBuilder();
        AuthService authService = new AuthService(null,null);
        builder.setPort(8585).setAuthService(authService)
                .addController(LoginRpcController.class);
        ServerApplication app = builder.build();
        app.Run();

    }
}
