package Client.Services;

import Client.RpcCaller;
import JSocket2.Protocol.Rpc.RpcResponse;
import Shared.Api.Models.ChatController.GetChatInfoOutputModel;
import Shared.Api.Models.MessageController.*;
import Shared.Models.Message.MessageType;
import javafx.concurrent.Task;

import java.util.UUID;

/**
 * A service class to handle all chat and message-related server communications.
 */
public class ChatService {

    private final RpcCaller rpcCaller;

    public ChatService(RpcCaller rpcCaller) {
        this.rpcCaller = rpcCaller;
    }

    /**
     * Creates a background task to fetch all chats for the current user.
     * @return A Task that, when run, returns the RPC response with chat information.
     */
    public Task<RpcResponse<GetChatInfoOutputModel[]>> fetchUserChats() {
        return new Task<>() {
            @Override
            protected RpcResponse<GetChatInfoOutputModel[]> call() throws Exception {
                return rpcCaller.getChatsByUser();
            }
        };
    }

    /**
     * Creates a background task to fetch all messages for a specific chat.
     * @param chatId The ID of the chat to fetch messages for.
     * @return A Task that, when run, returns the RPC response with message data.
     */
    public Task<RpcResponse<GetMessageOutputModel[]>> fetchMessagesForChat(UUID chatId) {
        return new Task<>() {
            @Override
            protected RpcResponse<GetMessageOutputModel[]> call() throws Exception {
                GetMessageByChatInputModel input = new GetMessageByChatInputModel();
                input.setChatId(chatId);
                return rpcCaller.getMessagesByChat(input);
            }
        };
    }

    /**
     * @return A Task that, when run, sends the message and returns the server's response.
     */
    public Task<RpcResponse<SendMessageOutputModel>> sendMessage(SendMessageInputModel model) {
        return new Task<>() {
            @Override
            protected RpcResponse<SendMessageOutputModel> call() throws Exception {
                return rpcCaller.sendMessage(model);
            }
        };
    }
    // ... existing service methods
    public Task<Void> sendTypingStatus(UUID chatId, boolean isTyping) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                TypingNotificationInputModel input = new TypingNotificationInputModel();
                input.setChatId(chatId);
                input.setTyping(isTyping);
                rpcCaller.sendTypingStatus(input);
                return null;
            }
        };
    }

    public Task<RpcResponse<Object>> editMessage(UUID messageId, String newContent) {
        return new Task<>() {
            @Override
            protected RpcResponse<Object> call() throws Exception {
                EditMessageInputModel input = new EditMessageInputModel();
                input.setMessageId(messageId);
                input.setNewContent(newContent);
                return rpcCaller.editMessage(input);
            }
        };
    }

    public Task<RpcResponse<Object>> deleteMessage(UUID messageId) {
        return new Task<>() {
            @Override
            protected RpcResponse<Object> call() throws Exception {
                DeleteMessageInputModel input = new DeleteMessageInputModel();
                input.setMessageId(messageId);
                return rpcCaller.deleteMessage(input);
            }
        };
    }

    public Task<Void> markChatAsRead(UUID chatId) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                MarkChatAsReadInputModel input = new MarkChatAsReadInputModel();
                input.setChatId(chatId);
                 rpcCaller.markChatAsRead(input);
                 return null;
            }
        };
    }
}