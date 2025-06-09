package JSocket2.Protocol;

import JSocket2.Protocol.Authentication.AuthProcessState;
import JSocket2.Protocol.Authentication.IAccessKeyManager;
import JSocket2.Core.Client.ClientSession;
import JSocket2.Cryptography.EncryptionUtil;
import JSocket2.Protocol.Rpc.RpcResponseMetadata;
import JSocket2.Protocol.Transfer.ClientFileTransferManager;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ClientMessageProcessor implements IMessageProcessor {
    private final Gson gson;
    private final MessageHandler messageHandler;
    private final ClientSession clientSession;
    private final ClientFileTransferManager fileTransferManager;
    private final Map<UUID, CompletableFuture<Message>> pendingRequests;
    private IAccessKeyManager accessKeyManager;
    public ClientMessageProcessor(MessageHandler handler, ClientSession clientSession,Map<UUID, CompletableFuture<Message>> pendingRequests,ClientFileTransferManager fileTransferManager){
        this.gson = new Gson();
        this.messageHandler = handler;
        this.clientSession = clientSession;
        this.pendingRequests = pendingRequests;
        this.fileTransferManager = fileTransferManager;
    }

    public void Invoke(Message message) throws IOException {
        switch (message.header.type) {
            case RSA_PUBLIC_KEY -> handleRsaPublicKey(message);
            case SEND_CHUNK -> handleDownloadChunk(message);
            default -> throw new UnsupportedOperationException("Unknown message type: " + message.header.type);
        }
    }
    private void handleDownloadChunk(Message message) throws IOException {
        fileTransferManager.ProcessSendChunk(message);
    }
    private void handleRsaPublicKey(Message message) throws IOException{
        try {
            var publicKey = EncryptionUtil.decodeRsaPublicKey(message.getPayload());
            clientSession.setServerPublicKey(publicKey);
            sendAesKey();
            //sendAuthModel();
            //Login
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendAuthModel() throws IOException {
        var authModel = accessKeyManager.getKeys();
        var payloadJson = gson.toJson(authModel);
        UUID requestId = UUID.randomUUID();
        MessageHeader header = MessageHeader.BuildAesKeyHeader(requestId,payloadJson.length());
        Message message = new Message(header);
        message.setPayload(payloadJson.getBytes(StandardCharsets.UTF_8));
        messageHandler.write(message);
        CompletableFuture<Message> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);
        var response = future.join();
        var metadata = gson.fromJson(new String(response.getMetadata(),StandardCharsets.UTF_8), RpcResponseMetadata.class);
        if(StatusCode.fromCode(metadata.getStatusCode()) == StatusCode.OK){
            accessKeyManager.setAuthProcessState(AuthProcessState.SUCCESS);
        }else{
            accessKeyManager.setAuthProcessState(AuthProcessState.FAILED);
        }

    }

    private void sendAesKey() throws IOException {
        UUID requestId = UUID.randomUUID();
        byte[] aes_key = clientSession.getAESKey().getEncoded();
        byte[] encrypted_aes_key = EncryptionUtil.encryptDataRSA(aes_key,clientSession.getServerPublicKey());
        MessageHeader header = MessageHeader.BuildAesKeyHeader(requestId,encrypted_aes_key.length);
        Message message = new Message(header);
        message.setPayload(encrypted_aes_key);
        messageHandler.write(message);
    }


}