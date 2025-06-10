package JSocket.Mvc;

import JSocket.Protocol.StatusCode;
import JSocket.Protocol.Rpc.RpcResponse;

public abstract class ControllerBase {

    protected final <T> RpcResponse<T> response(StatusCode statusCode,String message, T content) {
        return new RpcResponse<>(statusCode,message, content);
    }
    protected final <T> RpcResponse<T> response(StatusCode statusCode, T content) {
        return new RpcResponse<>(statusCode,null, content);
    }

    protected final RpcResponse<Object> NotFound() {
        return response(StatusCode.NOT_FOUND, null);
    }

    protected final RpcResponse<Object> BadRequest() {
        return response(StatusCode.BAD_REQUEST, null);
    }
    protected final RpcResponse<Object> BadRequest(String message) {
        return response(StatusCode.BAD_REQUEST, message,null);
    }
    protected final RpcResponse<Object> Ok() {
        return response(StatusCode.OK, null);
    }

    protected final <T> RpcResponse<T> Ok(T content) {
        return response(StatusCode.OK, content);
    }
}
