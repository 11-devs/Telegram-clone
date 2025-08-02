package JSocket2.Example.Controllers;

import JSocket2.Example.Models.LoginInputModel;
import JSocket2.Protocol.Rpc.RpcControllerBase;
import JSocket2.Protocol.Rpc.RpcAction;
import JSocket2.Protocol.Rpc.RpcResponse;


public class LoginRpcController extends RpcControllerBase {
    @RpcAction(Name="login")
    public RpcResponse<Object> Login(LoginInputModel model){
        if(model.Username.equals("admin") && model.Password.equals("12345678")) {
            return Ok();
        }else{
            return BadRequest("username or password is not correct");
        }
    }
}
