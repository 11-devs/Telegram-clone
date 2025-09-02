package Server;

import JSocket2.Protocol.Authentication.InvalidAccessKeyException;
import JSocket2.Protocol.Authentication.UserIdentity;
import JSocket2.Protocol.Authentication.AuthException;
import JSocket2.Protocol.Authentication.IAuthService;

public class AuthService implements IAuthService {
    DaoManager daoManager;
    public AuthService(DaoManager daoManager){
        this.daoManager = daoManager;
    }
    public boolean IsKeyValid(String key) throws AuthException
    {
        var session = daoManager.getSessionDAO().findByField("accessKey",key);
        return session != null;
    }
    public UserIdentity Login(String key) throws AuthException
    {
        var session = daoManager.getSessionDAO().findByField("accessKey",key);
        if(session != null){
            var account = session.getAccount();
            return new UserIdentity(account.getId().toString(),account.getFirstName(),account.getLastName());
        }else{
            throw new InvalidAccessKeyException();
        }
    }

}
