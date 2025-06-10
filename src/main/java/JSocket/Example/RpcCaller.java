package JSocket.Example;

import JSocket.Core.Client.ConnectionManager;
import JSocket.Example.Models.LoginInputModel;
import JSocket.Protocol.Rpc.RpcCallerBase;
import com.google.gson.Gson;

import java.io.IOException;

public class RpcCaller extends RpcCallerBase {

    private Gson gson = new Gson();

    public RpcCaller(ConnectionManager connectionManager){
        super(connectionManager);
    }


    public void login(LoginInputModel login) throws IOException {
        callRpc("LoginController","Login",login);
    }
}
