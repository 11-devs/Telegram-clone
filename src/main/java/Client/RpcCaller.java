package Client;

import JSocket2.Core.Client.ConnectionManager;
import JSocket2.Example.Models.LoginInputModel;
import JSocket2.Protocol.Rpc.RpcCallerBase;
import com.google.gson.Gson;

import java.io.IOException;

public class RpcCaller extends RpcCallerBase {

    private Gson gson = new Gson();

    public RpcCaller(ConnectionManager connectionManager){
        super(connectionManager);
    }


    public void login(LoginInputModel login) throws IOException {
        callRpc("LoginController","Login",login);}
}
