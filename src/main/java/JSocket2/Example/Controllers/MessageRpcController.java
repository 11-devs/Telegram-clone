package JSocket2.Example.Controllers;

import JSocket2.Example.Models.SendMessageInputModel;
import JSocket2.Example.Models.SendMessageOutputModel;
import JSocket2.Protocol.Rpc.RpcControllerBase;
import JSocket2.Protocol.Rpc.RpcResponse;

public class MessageRpcController extends RpcControllerBase {
    public RpcResponse<SendMessageOutputModel> SendMessage(SendMessageInputModel input){
        var output = new SendMessageOutputModel();
        return Ok(output);
    }
}
