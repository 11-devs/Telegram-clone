package JSocket2.Protocol.EventHub;

import JSocket2.Core.Client.ConnectionManager;
import JSocket2.Protocol.Message;
import JSocket2.Protocol.MessageHeader;
import JSocket2.Protocol.Rpc.RpcCallMetadata;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public abstract class PushControllerBase {
    private final ConnectionManager connectionManager;
    protected final Gson gson;
    public PushControllerBase(ConnectionManager connectionManager) {
        this(connectionManager, new Gson());
    }

    public PushControllerBase(ConnectionManager connectionManager, Gson gson) {
        this.connectionManager = connectionManager;
        this.gson = gson;
    }

    protected void callRpc(String controllerName, String actionName, Object payloadObject) throws IOException {
        UUID requestId = UUID.randomUUID();
        Message message = createRpcCallMessage(controllerName, actionName, payloadObject, requestId);
        connectionManager.getClient().getMessageHandler().write(message);
    }

    private Message createRpcCallMessage(String controllerName, String actionName, Object payloadObject, UUID requestId) throws IOException {
        RpcCallMetadata metadata = new RpcCallMetadata(controllerName, actionName);
        String metadataJson = gson.toJson(metadata);
        String payloadJson  = gson.toJson(payloadObject);
        byte[] metadataBytes = metadataJson.getBytes(StandardCharsets.UTF_8);
        byte[] payloadBytes  = payloadJson.getBytes(StandardCharsets.UTF_8);
        MessageHeader header = MessageHeader.BuildRpcCallHeader(
                requestId, true, metadataBytes.length, payloadBytes.length
        );
        return new Message(header, metadataBytes, payloadBytes);
    }
}
