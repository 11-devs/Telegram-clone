package JSocket2.Protocol.Rpc;

public class RpcResponseMetadata {
    private int statusCode;
    private String message;
    public RpcResponseMetadata(int statusCode,String message){
        this.statusCode = statusCode;
        this.message = message;
    }
    public int getStatusCode() {
        return statusCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }

    public void setStatusCode(byte statusCode) {
        this.statusCode = statusCode;
    }

}
