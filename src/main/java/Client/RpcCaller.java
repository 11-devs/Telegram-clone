package Client;

import JSocket2.Core.Client.ConnectionManager;
import JSocket2.Protocol.Rpc.RpcCallerBase;
import JSocket2.Protocol.Rpc.RpcResponse;
import com.google.gson.Gson;
import Shared.Api.Models.AccountController.*;

import java.io.IOException;

public class RpcCaller extends RpcCallerBase {
    private Gson gson = new Gson();

    public RpcCaller(ConnectionManager connectionManager) {
        super(connectionManager);
    }
    public RpcResponse<Boolean> isPhoneNumberRegistered(String phoneNumber) throws IOException {
        return callRpcAndGetResponse("AccountRpcController", "isPhoneNumberRegistered", Boolean.class, phoneNumber);
    }
    public RpcResponse<Object> verifyEmailOtp(VerifyCodeEmailInputModel model) throws IOException {
        return callRpcAndGetResponse("AccountRpcController", "verifyEmailOtp", Object.class, model);
    }
    public RpcResponse<RequestCodeEmailOutputModel> setEmail(String email) throws IOException {
        return callRpcAndGetResponse("AccountRpcController", "setEmail", RequestCodeEmailOutputModel.class, email);
    }

    public RpcResponse<RequestCodePhoneNumberOutputModel> requestOTP(RequestCodePhoneNumberInputModel model) throws IOException {
        return callRpcAndGetResponse("AccountRpcController", "requestOTP", RequestCodePhoneNumberOutputModel.class, model);
    }

    public RpcResponse<VerifyCodeOutputModel> verifyOTP(VerifyCodeInputModel model) throws IOException {
        return callRpcAndGetResponse("AccountRpcController", "verifyOTP", VerifyCodeOutputModel.class,model);
    }

    public RpcResponse<LoginOutputModel> login(LoginInputModel model) throws IOException {
        return callRpcAndGetResponse("AccountRpcController", "login", LoginOutputModel.class,model);
    }

    public RpcResponse<BasicRegisterOutputModel> basicRegister(BasicRegisterInputModel model) throws IOException {
        return callRpcAndGetResponse("AccountRpcController", "basicRegister",BasicRegisterOutputModel.class, model);
    }

    public RpcResponse<Object> setPassword(String password) throws IOException {
        return callRpcAndGetResponse("AccountRpcController", "setPassword",Object.class,password);
    }

    public RpcResponse<Object> setUsername(String username) throws IOException {
        return callRpcAndGetResponse("AccountRpcController", "setUsername",Object.class, username);
    }

    public RpcResponse<Object> setBio(String bio) throws IOException {
        return callRpcAndGetResponse("AccountRpcController", "setBio",Object.class, bio);
    }
}