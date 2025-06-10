package JSocket.Protocol.Authentication;

public interface IAccessKeyService {
    boolean isValidKey(String key);
    String useKey(String key);
    String generateKey();
    void saveKey(String userId,String key);
}
