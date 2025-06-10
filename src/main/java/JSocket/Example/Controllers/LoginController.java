package JSocket.Example.Controllers;

import JSocket.Example.Models.LoginInputModel;
import JSocket.Mvc.ControllerBase;
import JSocket.Protocol.Rpc.RpcResponse;


public class LoginController extends ControllerBase {
    public RpcResponse<Object> Login(LoginInputModel model){
        if(model.Username.equals("admin") && model.Password.equals("12345678")) {
            return Ok();
        }else{
            return BadRequest("username or password is not correct");
        }
    }
}
