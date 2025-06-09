package JSocket2.Example.Controllers;

import JSocket2.Example.Models.SendMessageInputModel;
import JSocket2.Example.Models.SendMessageOutputModel;
import JSocket2.Mvc.ControllerBase;
import JSocket2.Protocol.Rpc.RpcResponse;

public class MessageController extends ControllerBase {
    public RpcResponse<SendMessageOutputModel> SendMessage(SendMessageInputModel input){
        var output = new SendMessageOutputModel();

        return Ok(output);
    }
}
