package JSocket2.Core.Server;

import JSocket2.Protocol.Authentication.AuthService;
import JSocket2.Utils.Logger;
import JSocket2.Cryptography.RsaKeyManager;
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

    private final AuthService authService;
    public ServerSessionManager getServerSessionManager() {
        return serverSessionManager;
    }

    final ServerSessionManager serverSessionManager;
    private final RsaKeyManager rsaKeyManager;
    private final Map<UUID, CompletableFuture<Message>> pendingRequests;
    public ServerApplication(int port, RpcDispatcher rpcDispatcher, AuthService authService) throws IOException {
        this.PORT = port;
        this.rpcDispatcher = rpcDispatcher;
        this.serverSocket = new ServerSocket(PORT);
        this.serverSessionManager = new ServerSessionManager();
        this.rsaKeyManager = new RsaKeyManager();
        this.pendingRequests = new ConcurrentHashMap<>();
        this.authService =  authService;
    }

    public void Run() {
        try {
            System.out.println("Server run in "+InetAddress.getLocalHost().getHostAddress()+":"+PORT);
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                Logger.get().info("A new client has connected");
                ClientHandler clientHandler = new ClientHandler(socket, rpcDispatcher, serverSessionManager,rsaKeyManager,pendingRequests,authService);
                new Thread(clientHandler).start();
                Scanner scanner = new Scanner(System.in);
                    scanner.nextLine();
                    getServerSessionManager().closeAll();

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
