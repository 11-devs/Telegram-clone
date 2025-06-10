package JSocket.Protocol.Authentication;

public class AuthService {
    IAccessKeyService accessKeyService;
    IUserManager userManager;
    public AuthService(IAccessKeyService accessKeyService, IUserManager userManager){
        this.accessKeyService = accessKeyService;
        this.userManager = userManager;
    }
    public IUser Login(String key) throws AuthException
    {
        if(!accessKeyService.isValidKey(key)){throw new InvalidAccessKeyException();}
           var userId = accessKeyService.useKey(key);
         return userManager.getUser(userId);
    }
}