package JSocket.Utils;

import JSocket.Cryptography.EncryptionUtil;
import JSocket.Protocol.Message;

import javax.crypto.SecretKey;

public class MessageUtil {
    public static void EncryptMessage(Message message, SecretKey aesKey){
        byte[] IvBytes = EncryptionUtil.generateIV();
        message.setIvBytes(IvBytes);
        if (message.header.metadata_length > 0) {
            byte[] encrypted_metadata = EncryptionUtil.encryptDataAES(message.getMetadata(),aesKey,IvBytes);
            message.setMetadata(encrypted_metadata);
        }
        if (message.header.payload_length > 0) {
            byte[] encrypted_payload = EncryptionUtil.encryptDataAES(message.getPayload(),aesKey,IvBytes);
            message.setPayload(encrypted_payload);
        }
    }
    public static void DecryptMessage(Message message, SecretKey aesKey){
        byte[] IvBytes = message.getIvBytes();
        if (message.header.metadata_length > 0) {
            byte[] decrypted_metadata = EncryptionUtil.decryptDataAES(message.getMetadata(),aesKey,IvBytes);
            message.setMetadata(decrypted_metadata);
        }
        if (message.header.payload_length > 0) {
            byte[] decrypted_payload = EncryptionUtil.decryptDataAES(message.getPayload(),aesKey,IvBytes);
            message.setPayload(decrypted_payload);
        }
    }
}
