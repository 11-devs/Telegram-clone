package Server;

import JSocket2.Core.Server.ServerApplication;
import JSocket2.Core.Server.ServerApplicationBuilder;
import JSocket2.Cryptography.RsaKeyManager;
import Server.Controllers.*;
import Server.Events.NewMessageEvent;
import Shared.Database.Database;
import Shared.Models.Account.Account;
import jakarta.persistence.EntityManager;

import java.io.IOException;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerApplicationBuilder builder = new ServerApplicationBuilder();
        builder.setPort(8586).setAuthService(AuthService.class);
        Database database = new Database();
        builder.getServices().AddSingletonWithInstance(EntityManager.class,database.getEntityManager());
        builder.getServices().AddSingleton(DaoManager.class);
        builder.getServices().AddSingleton(NewMessageEvent.class);
        builder.addController(AccountRpcController.class);
        builder.addController(MessageRpcController.class);
        builder.addController(ChatRpcController.class);
        builder.addController(ContactRpcController.class);
        builder.addController(ViewRpcController.class);
        builder.addController(MediaRpcController.class);
        ServerApplication app = builder.build();
        app.Run();
    }
}