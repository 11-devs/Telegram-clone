package JSocket.Core;

import JSocket.Cryptography.EncryptionUtil;

import javax.crypto.SecretKey;

public abstract class Session {
    protected SecretKey aesKey;
    public Session(){
        aesKey = EncryptionUtil.generateAESsecretKey();
    }
    public Session(SecretKey aesKey){
        this.aesKey =aesKey;
    }
    public SecretKey getAESKey() {
        return aesKey;
    }
}
