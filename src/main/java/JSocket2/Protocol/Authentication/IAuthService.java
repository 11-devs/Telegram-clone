package JSocket2.Protocol.Authentication;

public interface IAuthService {
    UserIdentity Login(String key);
    boolean IsKeyValid(String key);
}
