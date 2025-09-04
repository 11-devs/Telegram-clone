package Client;

import JSocket2.Core.Client.ConnectionManager;
import JSocket2.Protocol.Rpc.RpcCallerBase;
import JSocket2.Protocol.Rpc.RpcResponse;
import Shared.Api.Models.ChatController.GetChatInfoOutputModel;
import Shared.Api.Models.ChatController.getChatsByUserInputModel;
import Shared.Api.Models.MediaController.CreateMediaInputModel;
import Shared.Api.Models.MessageController.GetMessageByChatInputModel;
import Shared.Api.Models.MessageController.GetMessageOutputModel;
import Shared.Api.Models.MessageController.SendMessageInputModel;
import Shared.Api.Models.MessageController.SendMessageOutputModel;
import Shared.Models.Chat.Chat;
import com.google.gson.Gson;
import Shared.Api.Models.AccountController.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

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
    public RpcResponse<GetChatInfoOutputModel[]> getChatsByUser() throws IOException {
        return callRpcAndGetResponse("ChatRpcController", "getChatsByUser", GetChatInfoOutputModel[].class);
    }

    public RpcResponse<GetMessageOutputModel[]> getMessagesByChat(GetMessageByChatInputModel model) throws IOException {
        // The server returns a list of its own inner class model.
        // We will get it as a generic list of objects (likely LinkedTreeMaps from Gson)
        // and parse it manually in the controller.
        return callRpcAndGetResponse("MessageRpcController", "getMessagesByChat", GetMessageOutputModel[].class,model);
    }

    public RpcResponse<SendMessageOutputModel> sendMessage(SendMessageInputModel model) throws IOException {
        return callRpcAndGetResponse("MessageRpcController", "sendMessage", SendMessageOutputModel.class, model);
    }
    public RpcResponse<UUID> createMediaEntry(CreateMediaInputModel model) throws IOException {
        return callRpcAndGetResponse("MediaRpcController", "createMediaEntry", UUID.class, model);
    }
}