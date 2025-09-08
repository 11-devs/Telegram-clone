package Client;

import JSocket2.Core.Client.ConnectionManager;
import JSocket2.Protocol.Rpc.RpcCallerBase;
import JSocket2.Protocol.Rpc.RpcResponse;
import Shared.Api.Models.ChatController.*;
import Shared.Api.Models.ContactController.*;
import Shared.Api.Models.ContactController.GetContactsOutputModel;
import Shared.Api.Models.MediaController.CreateMediaInputModel;
import Shared.Api.Models.MembershipController.GetChatMembersOutputModel;
import Shared.Api.Models.MembershipController.UpdateMemberRoleInputModel;
import Shared.Api.Models.MessageController.*;
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
    public RpcResponse<Object> resetPassword(ResetPasswordInputModel model) throws IOException {
        return callRpcAndGetResponse("AccountRpcController", "resetPassword", Object.class, model);
    }

    public RpcResponse<Object> requestPasswordReset(RequestCodePhoneNumberInputModel model) throws IOException {
        return callRpcAndGetResponse("AccountRpcController", "requestPasswordReset", Object.class, model);
    }

    public RpcResponse<VerifyCodeOutputModel> verifyPasswordResetEmailOtp(VerifyCodeEmailInputModel model) throws IOException {
        return callRpcAndGetResponse("AccountRpcController", "verifyPasswordResetEmailOtp", VerifyCodeOutputModel.class, model);
    }

    public RpcResponse<Object> resetAccount(String phoneNumber, String deviceInfo) throws IOException {
        return callRpcAndGetResponse("AccountRpcController", "resetAccount", Object.class, phoneNumber, deviceInfo);
    }
    public RpcResponse<GetChatInfoOutputModel[]> getChatsByUser() throws IOException {
        return callRpcAndGetResponse("ChatRpcController", "getChatsByUser", GetChatInfoOutputModel[].class);
    }

    public RpcResponse<CreateGroupOutputModel> createGroup(CreateGroupInputModel model) throws IOException {
        return callRpcAndGetResponse("ChatRpcController", "createGroup", CreateGroupOutputModel.class, model);
    }

    public RpcResponse<CreateChannelOutputModel> createChannel(CreateChannelInputModel model) throws IOException {
        return callRpcAndGetResponse("ChatRpcController", "createChannel", CreateChannelOutputModel.class, model);
    }

    public RpcResponse<UpdateChatInfoOutputModel> updateChatInfo(UpdateChatInfoInputModel model) throws IOException {
        return callRpcAndGetResponse("ChatRpcController", "updateChatInfo", UpdateChatInfoOutputModel.class, model);
    }

    public RpcResponse<Object> deleteChat(UUID chatId) throws IOException {
        return callRpcAndGetResponse("ChatRpcController", "deleteChat", Object.class, chatId);
    }
    public RpcResponse<GetChatInfoOutputModel> getChatByUserId(UUID chatId) throws IOException {
        return callRpcAndGetResponse("ChatRpcController", "getChatByUserId", GetChatInfoOutputModel.class, chatId);
    }

    public RpcResponse<GetChatMembersOutputModel> getChatMembers(UUID chatId) throws IOException {
        return callRpcAndGetResponse("MembershipRpcController", "getChatMembers", GetChatMembersOutputModel.class, chatId);
    }

    public RpcResponse<Object> updateMemberRole(UpdateMemberRoleInputModel model) throws IOException {
        return callRpcAndGetResponse("MembershipRpcController", "updateMemberRole", Object.class, model);
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

    public void sendTypingStatus(TypingNotificationInputModel model) throws IOException {
        callRpc("MessageRpcController", "sendTypingStatus", model);
    }

    public RpcResponse<Object> editMessage(EditMessageInputModel model) throws IOException {
        return callRpcAndGetResponse("MessageRpcController", "editMessage", Object.class, model);
    }

    public RpcResponse<Object> deleteMessage(DeleteMessageInputModel model) throws IOException {
        return callRpcAndGetResponse("MessageRpcController", "deleteMessage", Object.class, model);
    }

    public void markChatAsRead(MarkChatAsReadInputModel model) throws IOException {
        callRpc("MessageRpcController", "markChatAsRead", model);
    }
    public RpcResponse<GetChatInfoOutputModel> getChatByUsername(String username) throws IOException {
        return callRpcAndGetResponse("ChatRpcController", "getChatByUsername", GetChatInfoOutputModel.class, username);
    }
    public RpcResponse<Object> toggleChatMute(ToggleChatMuteInputModel model) throws IOException {
        return callRpcAndGetResponse("ChatRpcController", "toggleChatMute", Object.class, model);
    }


    public RpcResponse<GetAccountInfoOutputModel> getAccountInfo() throws IOException {
        return callRpcAndGetResponse("AccountRpcController", "getAccountInfo", GetAccountInfoOutputModel.class);
    }

    public RpcResponse<Object> setProfilePicture(SetProfilePictureInputModel model) throws IOException {
        return callRpcAndGetResponse("AccountRpcController", "setProfilePicture", Object.class, model);

    }
    public RpcResponse<Object> updateName(UpdateNameInputModel model) throws IOException {
        return callRpcAndGetResponse("AccountRpcController", "updateName", Object.class, model);
    }
    public RpcResponse<Object> forwardMessage(ForwardMessageInputModel model) throws IOException {
        return callRpcAndGetResponse("MessageRpcController", "forwardMessage", Object.class, model);
    }

    // Add these methods inside the RpcCaller class

    public RpcResponse<GetContactsOutputModel> getContacts() throws IOException {
        return callRpcAndGetResponse("ContactRpcController", "getContacts", GetContactsOutputModel.class);
    }

    public RpcResponse<AddContactOutputModel> addContact(AddContactInputModel model) throws IOException {
        return callRpcAndGetResponse("ContactRpcController", "addContact", AddContactOutputModel.class, model);
    }

    public RpcResponse<RemoveContactOutputModel> removeContact(RemoveContactInputModel model) throws IOException {
        return callRpcAndGetResponse("ContactRpcController", "removeContact", RemoveContactOutputModel.class, model);
    }
    public RpcResponse<GetChatInfoOutputModel> getSavedMessage() throws IOException {
        return callRpcAndGetResponse("ChatRpcController", "getSavedMessage", GetChatInfoOutputModel.class);

    }
    public RpcResponse<GetChatInfoOutputModel[]> searchPublic(String query) throws IOException {
        return callRpcAndGetResponse("ChatRpcController", "searchPublic", GetChatInfoOutputModel[].class,query);

    }


}