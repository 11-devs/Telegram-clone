package JSocket.Example.Controllers;

import JSocket.Example.Models.SendMessageInputModel;
import JSocket.Example.Models.SendMessageOutputModel;
import JSocket.Mvc.ControllerBase;
import JSocket.Protocol.Rpc.RpcResponse;

public class MessageController extends ControllerBase {
    public RpcResponse<SendMessageOutputModel> SendMessage(SendMessageInputModel input){
        var output = new SendMessageOutputModel();

        return Ok(output);
    }
}
