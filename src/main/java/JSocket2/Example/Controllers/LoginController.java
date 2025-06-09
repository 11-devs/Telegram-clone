package JSocket2.Example.Controllers;

import JSocket2.Example.Models.LoginInputModel;
import JSocket2.Mvc.ControllerBase;
import JSocket2.Protocol.Rpc.RpcResponse;


public class LoginController extends ControllerBase {
    public RpcResponse<Object> Login(LoginInputModel model){
        if(model.Username.equals("admin") && model.Password.equals("12345678")) {
            return Ok();
        }else{
            return BadRequest("username or password is not correct");
        }
    }
}
