package Server.Controllers;

import Server.DaoManager;
import JSocket2.Protocol.Rpc.*;
public class MessageRpcController extends RpcControllerBase{
    private final DaoManager daoManager;
    public MessageRpcController(DaoManager daoManager){
        this.daoManager = daoManager;
    }

}

