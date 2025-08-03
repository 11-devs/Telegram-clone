package JSocket2.Protocol.Rpc;

import JSocket2.Protocol.StatusCode;

public class RpcResponse<T> {
    private StatusCode statusCode;
    private String message;
    private T payload;
    public RpcResponse(StatusCode statusCode,String message,T payload){
        this.statusCode = statusCode;
        this.payload = payload;
        this.message = message;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public T getPayload() {
        return payload;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public String getMessage() {
        return message;
    }
}
