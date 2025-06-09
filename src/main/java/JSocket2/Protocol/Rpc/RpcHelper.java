package JSocket2.Protocol.Rpc;

import JSocket2.Protocol.Message;
import JSocket2.Protocol.MessageType;
import JSocket2.Protocol.StatusCode;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

public class RpcHelper {
    private static Gson gson = new Gson();
    public static  <T> RpcResponse<T> convertMessageToRpcResponse(Message message,Class<T> responseClass){
        if(message.header.type != MessageType.RPC_RESPONSE)
            throw new RuntimeException();
        RpcResponseMetadata metaObj = gson.fromJson(
                new String(message.getMetadata(), StandardCharsets.UTF_8),
                RpcResponseMetadata.class
        );
        var result = gson.fromJson(
                new String(message.getPayload(), StandardCharsets.UTF_8),
                responseClass
        );
        var response = new RpcResponse<T>(StatusCode.fromCode(metaObj.getStatusCode()),metaObj.getMessage(),result);
        return response;
    }
}
