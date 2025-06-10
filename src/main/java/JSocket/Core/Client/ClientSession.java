package JSocket.Core.Client;

import JSocket.Core.Session;

import javax.crypto.SecretKey;
import java.security.PublicKey;

public class ClientSession extends Session {
    private PublicKey serverPublicKey;
    private boolean isAuthorized = false;

    public ClientSession(){

    }

    public ClientSession(PublicKey serverPublicKey, SecretKey aesKey) {
        this.serverPublicKey = serverPublicKey;
        super.aesKey = aesKey;
    }
    public void setServerPublicKey(PublicKey serverPublicKey) {
        this.serverPublicKey = serverPublicKey;
    }
    public PublicKey getServerPublicKey() {
        return serverPublicKey;
    }
    public boolean isAuthorized() {
        return isAuthorized;
    }
    public void setAuthorized(boolean authorized) {
        isAuthorized = authorized;
    }
}
