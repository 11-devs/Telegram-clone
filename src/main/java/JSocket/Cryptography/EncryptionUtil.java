package JSocket.Cryptography;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class EncryptionUtil {

    private static final int AES_KEY_SIZE = 128;
    private static final int RSA_KEY_SIZE = 2048;
    private static final int IV_SIZE = 16;

    public static PublicKey decodeRsaPublicKey(byte[] rsaPublicKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec spec = new X509EncodedKeySpec(rsaPublicKeyBytes);
        return keyFactory.generatePublic(spec);
    }
    public static SecretKey decodeAesKey(byte[] aesKeyBytes){
       return new SecretKeySpec(aesKeyBytes, 0, aesKeyBytes.length, "AES");
    }
    public static KeyPair generateRSAkeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(RSA_KEY_SIZE);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            System.err.println("ERROR: while generating RSA KeyPair:");
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static byte[] encryptDataRSA(byte[] jsonData, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(jsonData);
        } catch (Exception e) {
            System.err.println("Error: while encrypting data with RSA:");
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static byte[] decryptDataRSA(byte[] encryptedBytes, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(encryptedBytes);
        } catch (Exception e) {
            System.err.println("Error: while decrypting data with RSA:");
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static byte[] encryptDataAES(byte[] rawBytes, SecretKey aesKey, byte[] ivBytes) {
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
            return cipher.doFinal(rawBytes);
        } catch (Exception e) {
            System.err.println("Error: while encrypting data with AES:");
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static byte[] decryptDataAES(byte[] cipherBytes, SecretKey aesKey, byte[] ivBytes) {
        try {
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);
            return cipher.doFinal(cipherBytes);
        } catch (Exception e) {
            System.err.println("Error: while decrypting data with AES:");
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static byte[] generateIV() {
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    public static SecretKey generateAESsecretKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(AES_KEY_SIZE);
            return keyGenerator.generateKey();
        } catch (Exception e) {
            System.err.println("Error: while generating AES key:");
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
