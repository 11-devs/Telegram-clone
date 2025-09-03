package JSocket2.Protocol;

import JSocket2.Protocol.Authentication.IAuthService;
import JSocket2.Protocol.Authentication.InvalidAccessKeyException;
import JSocket2.Core.Server.ServerSession;
import JSocket2.Cryptography.EncryptionUtil;
import JSocket2.Cryptography.RsaKeyManager;
import JSocket2.Protocol.Authentication.AuthModel;
import JSocket2.Protocol.Rpc.RpcCallMetadata;
import JSocket2.Protocol.Rpc.RpcDispatcher;
import JSocket2.Protocol.Rpc.RpcResponseMetadata;
import JSocket2.Protocol.Transfer.ServerFileTransferManager;
import JSocket2.Protocol.Transfer.Upload.UploadResumeRequestMetadata;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ServerMessageProcessor implements IMessageProcessor {
    private final Gson gson;
    private final RpcDispatcher rpcDispatcher;
    private final MessageHandler messageHandler;
    private final ServerFileTransferManager fileTransferManager;
    private final ServerSession serverSession;
    private final IAuthService authService;
    private RsaKeyManager rsaKeyManager;

    public ServerMessageProcessor(MessageHandler handler, RpcDispatcher rpcDispatcher, ServerFileTransferManager fileTransferManager, ServerSession serverSession, RsaKeyManager rsaKeyManager, IAuthService authService) {
        this.gson = new Gson();
        this.messageHandler = handler;
        this.rpcDispatcher = rpcDispatcher;
        this.fileTransferManager = fileTransferManager;
        this.serverSession = serverSession;
        this.rsaKeyManager = rsaKeyManager;
        this.authService = authService;
    }
    @Override
    public void Invoke(Message message) throws IOException {
        switch (message.header.type) {
            case RPC_CALL -> handleRpcCall(message);
            case UPLOAD_REQUEST -> handleUploadRequest(message);
            case UPLOAD_CHUNK -> handleUploadChunk(message);
            case SEND_CHUNK -> handleUploadChunk(message);
            case UPLOAD_RESUME_REQUEST -> handleUploadResumeRequest(message);
            case DOWNLOAD_REQUEST -> handleDownloadRequest(message);
            case DOWNLOAD_START -> handleStartDownload(message);
            case UPLOAD_ACK -> {
            }
            case AES_KEY -> handeAesKey(message);
            case AUTH -> handleAuth(message);
            default -> throw new UnsupportedOperationException("Unknown message type: " + message.header.type);
        }
    }

    private void handleStartDownload(Message message) throws IOException {
        fileTransferManager.ProcessDownloadChunkRequest(message);
    }

    private void handleDownloadRequest(Message message) throws IOException {
        fileTransferManager.ProcessDownloadRequest(message);
    }

    private void handleUploadResumeRequest(Message message) throws IOException {
        var metadata = gson.fromJson(new String(message.getMetadata(), StandardCharsets.UTF_8), UploadResumeRequestMetadata.class);
        fileTransferManager.ProcessUploadResumeRequest(message.header.uuid, metadata);
    }

    private void handeAesKey(Message message) {
        var encrypted_aes_key = message.getPayload();
        var decrypted_aes_key = EncryptionUtil.decryptDataRSA(encrypted_aes_key, rsaKeyManager.getRSAPrivateKey());
        var aesKey = EncryptionUtil.decodeAesKey(decrypted_aes_key);
        serverSession.setAESKey(aesKey);
        System.out.println("handshake is complete");
    }

    private void handleAuth(Message message) throws IOException {
        var payloadJson = new String(message.getPayload(), StandardCharsets.UTF_8);
        var response = gson.fromJson(payloadJson, AuthModel.class);
        String responseMetadata = null;
        try {
            for (var key : response.getAccessKeys()) {
                if(!authService.IsKeyValid(key))
                    continue;
                var user = authService.Login(key);
                serverSession.subscribeUser(user);
            }
                responseMetadata = gson.toJson(new RpcResponseMetadata(StatusCode.OK.code, "Auth was successful"));
        }
        catch (InvalidAccessKeyException e) {
            responseMetadata = gson.toJson(new RpcResponseMetadata(StatusCode.BAD_REQUEST.code, "Auth was failed"));
        }
        catch (Exception e){

            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally {
            var msg = new Message(
                    MessageHeader.BuildRpcResponseHeader(message.header.uuid, false, responseMetadata.length(), 0),
                    responseMetadata.getBytes(StandardCharsets.UTF_8),
                    new byte[0]
            );
            messageHandler.write(msg);
        }
    }

    private void handleRpcCall(Message message) throws IOException {
        var metadata = gson.fromJson(new String(message.getMetadata(), StandardCharsets.UTF_8), RpcCallMetadata.class);
        var payloadJson = new String(message.getPayload(), StandardCharsets.UTF_8);
        var response = rpcDispatcher.dispatch(metadata, payloadJson,serverSession.getActiveUser());

        var responseMetadata = gson.toJson(new RpcResponseMetadata(response.getStatusCode().code, response.getMessage()));
        var responsePayload = gson.toJson(response.getPayload());

        var msg = new Message(
                MessageHeader.BuildRpcResponseHeader(message.header.uuid, false, responseMetadata.length(), responsePayload.length()),
                responseMetadata.getBytes(StandardCharsets.UTF_8),
                responsePayload.getBytes(StandardCharsets.UTF_8)
        );

        messageHandler.write(msg);
    }

    private void handleUploadRequest(Message message) throws IOException {
        fileTransferManager.ProcessUploadRequest(message);
    }

    private void handleUploadChunk(Message message) throws IOException {
        fileTransferManager.ProcessSendChunk(message);
    }

}
