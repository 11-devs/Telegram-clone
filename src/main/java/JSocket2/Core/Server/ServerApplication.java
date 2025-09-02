package JSocket2.Core.Server;

import JSocket2.DI.ServiceCollection;
import JSocket2.DI.ServiceProvider;
import JSocket2.Protocol.Authentication.IAuthService;
import JSocket2.Protocol.Rpc.RpcControllerCollection;
import JSocket2.Utils.Logger;
import JSocket2.Protocol.Rpc.RpcDispatcher;
import JSocket2.Protocol.Message;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ServerApplication {
    private final int PORT;
    private final ServerSocket serverSocket;
    private final RpcDispatcher rpcDispatcher;
    public final ServiceProvider serviceProvider;
    public ServerSessionManager getServerSessionManager() {
        return serverSessionManager;
    }

    final ServerSessionManager serverSessionManager;
    private final Map<UUID, CompletableFuture<Message>> pendingRequests;
    public ServerApplication(int port, RpcControllerCollection rpcControllerCollection, ServiceCollection services) throws IOException {
        this.PORT = port;
        this.serviceProvider = services.CreateServiceProvider();
        this.serverSocket = new ServerSocket(PORT);
        this.serverSessionManager =serviceProvider.GetService(ServerSessionManager.class);
        this.pendingRequests = new ConcurrentHashMap<>();

        this.rpcDispatcher = rpcControllerCollection.CreateRpcDispatcher(this.serviceProvider);

    }

    public void Run() {
        try {
            System.out.println("Server run in " + InetAddress.getLocalHost().getHostAddress() + ":"+PORT);
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                Logger.get().info("A new client has connected");
                ClientHandler clientHandler = new ClientHandler(serviceProvider,socket, rpcDispatcher, serverSessionManager,pendingRequests);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            Logger.get().error("Error while listening for clients");
            Close();
        }
        catch (Exception e) {
            Logger.get().error("Unknown Error while listening for clients");
            Close();
        }
    }

    public void Close() {
        try {
            Logger.get().info("Server has closed");
            serverSocket.close();
        } catch (IOException e) {
            Logger.get().error("Error while closing the server socket");
        }
    }
}
