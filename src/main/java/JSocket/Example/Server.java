package JSocket.Example;

import JSocket.Protocol.Authentication.AuthService;
import JSocket.Core.Server.ServerApplication;
import JSocket.Core.Server.ServerApplicationBuilder;
import JSocket.Example.Controllers.LoginController;
import JSocket.Example.Models.LoginInputModel;

import java.io.IOException;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerApplicationBuilder builder = new ServerApplicationBuilder();
        AuthService authService = new AuthService(null,null);
        builder.setPort(8585).setAuthService(authService)
                .addController("LoginController" , new LoginController())
                .addAction("LoginController","Login", LoginInputModel.class);
        ServerApplication app = builder.build();
        app.Run();
    }
}
