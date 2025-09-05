package JSocket2.Protocol.Rpc;

import JSocket2.Core.Server.ServerSession;
import JSocket2.Core.Server.ServerSessionManager;
import JSocket2.Protocol.Authentication.UserIdentity;
import JSocket2.Protocol.StatusCode;

public abstract class RpcControllerBase {
    private UserIdentity currentUser = null;
    protected UserIdentity getCurrentUser(){
        return currentUser;
    }
    protected void setCurrentUser(UserIdentity currentUser){
        if(this.currentUser == null){
            this.currentUser = currentUser;
        }
    }
    private ServerSessionManager serverSessionManager = null;

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

    protected ServerSessionManager getServerSessionManager() {
        return serverSessionManager;
    }

    protected void setServerSessionManager(ServerSessionManager serverSessionManager) {
        this.serverSessionManager = serverSessionManager;
    }
}
