package JSocket2.Protocol.Rpc;

import JSocket2.Protocol.Message;
import JSocket2.Protocol.MessageType;
import JSocket2.Protocol.StatusCode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RpcHelper {
    private static Gson gson = new Gson();
    public static <T> RpcResponse<List<T>> convertMessageToRpcListResponse(
            Message message,
            Class<T> elementClass) {
        Type listType = TypeToken.getParameterized(List.class, elementClass).getType();
        return (RpcResponse<List<T>>) convertMessageToRpcResponse(message, listType);
    }
    public static  <T> RpcResponse<T> convertMessageToRpcResponse(Message message, Class<T> responseClass){
        return  (RpcResponse<T>) convertMessageToRpcResponse(message, responseClass);
    }
    private static RpcResponse<?> convertMessageToRpcResponse(Message message, Type responseClass){
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
        var response = new RpcResponse<>(StatusCode.fromCode(metaObj.getStatusCode()),metaObj.getMessage(),result);
        return response;
    }
}
