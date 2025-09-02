package JSocket2.Protocol.Rpc;

import JSocket2.Core.Client.ConnectionManager;
import JSocket2.Protocol.*;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RpcCallerBase {
    private final ConnectionManager connectionManager;
    protected final Gson gson;
    public RpcCallerBase(ConnectionManager connectionManager) {
        this(connectionManager, new Gson());
    }

    public RpcCallerBase(ConnectionManager connectionManager, Gson gson) {
        this.connectionManager = connectionManager;
        this.gson = gson;
    }

    protected void callRpc(String controllerName, String actionName, Object... payloadObjects) throws IOException {
        UUID requestId = UUID.randomUUID();
        Message message = createRpcCallMessage(controllerName, actionName, payloadObjects, requestId);
        connectionManager.getClient().getMessageHandler().write(message);
    }

    private Message createRpcCallMessage(String controllerName, String actionName, Object[] payloadObjects, UUID requestId) throws IOException {
        RpcCallMetadata metadata = new RpcCallMetadata(controllerName, actionName);
        String metadataJson = gson.toJson(metadata);
        String payloadJson  = gson.toJson(payloadObjects);
        byte[] metadataBytes = metadataJson.getBytes(StandardCharsets.UTF_8);
        byte[] payloadBytes  = payloadJson.getBytes(StandardCharsets.UTF_8);
        MessageHeader header = MessageHeader.BuildRpcCallHeader(
                requestId, true, metadataBytes.length, payloadBytes.length
        );
        return new Message(header, metadataBytes, payloadBytes);
    }

    protected <T> RpcResponse<T> callRpcAndGetResponse(String controllerName, String actionName, Class<T> responseClass, Object... payloadObjects) throws IOException {
        UUID requestId = UUID.randomUUID();
        Message message = createRpcCallMessage(controllerName, actionName, payloadObjects, requestId);
        CompletableFuture<Message> future = new CompletableFuture<>();
        connectionManager.getClient().getPendingRequests().put(requestId, future);
        connectionManager.getClient().getMessageHandler().write(message);
        var responseMessage = future.join();
        var response = RpcHelper.convertMessageToRpcResponse(responseMessage,responseClass);
        if(response.getStatusCode() != StatusCode.OK){
            System.out.println("Error Code: "+response.getStatusCode() + " "+response.getMessage());
        }
        return response;
    }
    protected <T> RpcResponse<List<T>> callRpcAndGetListResponse(String controllerName, String actionName,Class<T> responseClass, Object... payloadObjects) throws IOException {
        UUID requestId = UUID.randomUUID();
        Message message = createRpcCallMessage(controllerName, actionName, payloadObjects, requestId);
        CompletableFuture<Message> future = new CompletableFuture<>();
        connectionManager.getClient().getPendingRequests().put(requestId, future);
        connectionManager.getClient().getMessageHandler().write(message);
        var responseMessage = future.join();
        var response = RpcHelper.convertMessageToRpcListResponse(responseMessage,responseClass);
        if(response.getStatusCode() != StatusCode.OK){
            System.out.println("Error Code: "+response.getStatusCode() + " "+response.getMessage());
        }
        return response;
    }
}
