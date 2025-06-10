package JSocket.Protocol.Authentication;

public interface IAccessKeyManager {
    AuthModel getKeys();
    void setAuthProcessState(AuthProcessState state);
    AuthProcessState getAuthProcessState();
}
