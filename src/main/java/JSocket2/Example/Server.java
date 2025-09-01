package JSocket2.Example;

import Server.AuthService;
import JSocket2.Core.Server.ServerApplication;
import JSocket2.Core.Server.ServerApplicationBuilder;
import JSocket2.Example.Controllers.LoginRpcController;

import java.io.IOException;

public class Server {

    public static void main(String[] args) throws IOException {
        ServerApplicationBuilder builder = new ServerApplicationBuilder();
        builder.setPort(8585)
                .addController(LoginRpcController.class);
        builder.setAuthService(AuthService.class);
        ServerApplication app = builder.build();
        app.Run();

    }
}
