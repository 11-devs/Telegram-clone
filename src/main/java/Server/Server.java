package Server;

import JSocket2.Core.Server.ServerApplication;
import JSocket2.Core.Server.ServerApplicationBuilder;
import Shared.Database.Database;
import jakarta.persistence.EntityManager;

import java.io.IOException;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerApplicationBuilder builder = new ServerApplicationBuilder();
        builder.setPort(8585).setAuthService(AuthService.class);
        Database database = new Database();
        builder.getServices().AddSingletonWithInstance(EntityManager.class,database.getEntityManager());
        builder.getServices().AddSingleton(DaoManager.class);
        ServerApplication app = builder.build();
        app.Run();
    }
}