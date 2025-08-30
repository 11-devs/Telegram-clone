package Server.Controllers;

import Server.DaoManager;
import JSocket2.Protocol.Rpc.*;
public class MessageController extends RpcControllerBase{
    private final DaoManager daoManager;
    public MessageController(DaoManager daoManager){
        this.daoManager = daoManager;
    }
//
//    public RpcResponse<> getMessages(){
//
//    }
}

