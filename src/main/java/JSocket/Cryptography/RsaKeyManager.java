package JSocket.Cryptography;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class RsaKeyManager {

    private final KeyPair rsaKeyPair;
    public RsaKeyManager() {
        rsaKeyPair = EncryptionUtil.generateRSAkeyPair();
    }
    public RsaKeyManager(KeyPair rsaKeyPair) {
        this.rsaKeyPair = rsaKeyPair;
    }
    public PublicKey getRSAPublicKey() {
        return rsaKeyPair.getPublic();
    }
    public PrivateKey getRSAPrivateKey() {
        return rsaKeyPair.getPrivate();
    }

}
