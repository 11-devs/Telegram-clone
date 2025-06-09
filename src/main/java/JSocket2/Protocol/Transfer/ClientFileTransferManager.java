package JSocket2.Protocol.Transfer;

import JSocket2.Protocol.Message;
import JSocket2.Protocol.MessageHandler;
import JSocket2.Protocol.MessageHeader;
import JSocket2.Protocol.Rpc.RpcResponseMetadata;
import JSocket2.Protocol.Transfer.Download.DownloadChunkRequestMetadata;
import JSocket2.Protocol.Transfer.Download.DownloadFileInfoModel;
import JSocket2.Protocol.Transfer.Download.DownloadRequestMetadata;
import JSocket2.Protocol.Transfer.Upload.UploadRequestMetadata;
import JSocket2.Protocol.Transfer.Upload.UploadResumeRequestMetadata;
import JSocket2.Protocol.Transfer.Upload.UploadResumeResultModel;
import JSocket2.Utils.FileUtil;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

public class ClientFileTransferManager extends FileTransferManager{
    private final String SAVE_PATH = "src/files/client";
    public ClientFileTransferManager(MessageHandler handler, Map<UUID, CompletableFuture<Message>> pendingRequests) {
        super(handler,pendingRequests);
    }
    public FileInfoModel initiateUpload(File file) throws IOException {
        String fileName = FileUtil.getFileNameWithoutExtension(file);
        String fileExtension = FileUtil.getFileExtension(file);
        long fileSize = file.length();

        UUID requestId = UUID.randomUUID();
        FileInfoModel info = sendUploadInitRequest(requestId, fileName, fileExtension, fileSize);
        return info;
    }
    public void StartUpload(FileInfoModel info,File file) throws IOException {
        long fileSize = file.length();
        UUID requestId = UUID.randomUUID();
        sendFileInternal(requestId,info.FileId,file,0,0,info.ChunkSize,fileSize);
    }
    public void ContinueUpload(String fileId, File file) throws IOException {
        UploadResumeResultModel resumeInfo = sendTransferResumeRequest(UUID.randomUUID(), fileId);
        sendFileInternal(
                UUID.randomUUID(),
                fileId,
                file,
                resumeInfo.StartIndex,
                resumeInfo.StartOffset,
                resumeInfo.ChunkSize,
                resumeInfo.FileSize
        );
    }
    private UploadResumeResultModel sendTransferResumeRequest(UUID requestId, String fileId) throws IOException {
        UploadResumeRequestMetadata metadata = new UploadResumeRequestMetadata(fileId);
        byte[] metadataBytes = gson.toJson(metadata).getBytes(StandardCharsets.UTF_8);
        Message request = new Message(
                MessageHeader.BuildResumeUploadRequestAckHeader(requestId, true, metadataBytes.length, 0),
                metadataBytes,
                new byte[0]
        );

        CompletableFuture<Message> futureResponse = new CompletableFuture<>();
        pendingRequests.put(requestId, futureResponse);
        handler.write(request);

        Message response;
        try {
            response = futureResponse.join();
        } finally {
            pendingRequests.remove(requestId);
        }

        RpcResponseMetadata metaObj = gson.fromJson(
                new String(response.getMetadata(), StandardCharsets.UTF_8),
                RpcResponseMetadata.class
        );
        UploadResumeResultModel info = gson.fromJson(
                new String(response.getPayload(), StandardCharsets.UTF_8),
                UploadResumeResultModel.class
        );
        return info;
    }

    private FileInfoModel sendUploadInitRequest(UUID requestId, String fileName,String fileExtension, long fileSize) throws IOException {
        UploadRequestMetadata metadata = new UploadRequestMetadata("1234", fileName,fileExtension, fileSize);
        byte[] metadataBytes = gson.toJson(metadata).getBytes(StandardCharsets.UTF_8);

        Message request = new Message(
                MessageHeader.BuildUploadRequestHeader(requestId, true, metadataBytes.length, 0),
                metadataBytes,
                new byte[0]
        );

        CompletableFuture<Message> futureResponse = new CompletableFuture<>();
        pendingRequests.put(requestId, futureResponse);
        handler.write(request);

        Message response;
        try {
            response = futureResponse.join();
        } finally {
            pendingRequests.remove(requestId);
        }

        RpcResponseMetadata metaObj = gson.fromJson(
                new String(response.getMetadata(), StandardCharsets.UTF_8),
                RpcResponseMetadata.class
        );
        FileInfoModel info = gson.fromJson(
                new String(response.getPayload(), StandardCharsets.UTF_8),
                FileInfoModel.class
        );
        return info;
    }
    public TransferInfo initiateDownload(String fileId) throws IOException {
        sendDownloadRequest(UUID.randomUUID(),fileId);
        String basePath = "src/files/temp";
        File infoFile = new File(basePath, fileId + ".info");
        File tmpFile = new File(basePath, fileId + ".tmp");
        int chunkIndex = 0;
        long offset = 0;
        if (tmpFile.exists() && infoFile.exists()) {
        try {
            Gson gson = new Gson();
            TransferInfo info;
            try (FileReader reader = new FileReader(infoFile)) {
                info = gson.fromJson(reader, TransferInfo.class);
                return info;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException(e);
        } catch (JsonIOException e) {
            throw new RuntimeException(e);
        }
        }
        return null;
    }
    private void sendDownloadRequest(UUID requestId, String fileId) throws IOException {
        DownloadRequestMetadata metadata = new DownloadRequestMetadata(fileId);
        byte[] metadataBytes = gson.toJson(metadata).getBytes(StandardCharsets.UTF_8);

        Message request = new Message(
                MessageHeader.BuildDownloadRequestHeader(requestId, true, metadataBytes.length, 0),
                metadataBytes,
                new byte[0]
        );

        CompletableFuture<Message> futureResponse = new CompletableFuture<>();
        pendingRequests.put(requestId, futureResponse);
        handler.write(request);

        Message response;
        try {
            response = futureResponse.join();
        } finally {
            pendingRequests.remove(requestId);
        }

        RpcResponseMetadata metaObj = gson.fromJson(
                new String(response.getMetadata(), StandardCharsets.UTF_8),
                RpcResponseMetadata.class
        );
        DownloadFileInfoModel info = gson.fromJson(
                new String(response.getPayload(), StandardCharsets.UTF_8),
                DownloadFileInfoModel.class
        );
        int totalChunksCount = (int) Math.ceil((double) info.getFileLength() / 65536);
        createTransfer(fileId,info.getFileName(),info.getFileExtension(),SAVE_PATH,"src/files/temp",totalChunksCount,info.getFileLength());
    }
    public void sendDownloadChunkRequest(String fileId,int chunkIndex,long offset) throws IOException {
        DownloadChunkRequestMetadata metadata = new DownloadChunkRequestMetadata(fileId,chunkIndex,offset);
        byte[] metadataBytes = gson.toJson(metadata).getBytes(StandardCharsets.UTF_8);
        Message request = new Message(
                MessageHeader.BuildStartDownloadRequestHeader(UUID.randomUUID(), true, metadataBytes.length, 0),
                metadataBytes,
                new byte[0]
        );
        handler.write(request);
    }
    private void sendDownloadChunkRequest(String fileId) throws IOException {
        sendDownloadChunkRequest(fileId,0,0);
    }
    @Override
    public void ProcessSendChunk(Message message) throws IOException {
        super.ProcessSendChunk(message);
        var metadata = gson.fromJson(
                new String(message.getMetadata(), StandardCharsets.UTF_8),
                SendChunkMetadata.class
        );
        IProgressListener progressListener = progressListeners.get(metadata.fileId);
        if (progressListener != null) {
            progressListener.onProgress(metadata.chunkIndex+1,metadata.totalChunks);
        }
        BooleanSupplier continueSuppliersOrDefault = continueSuppliers.getOrDefault(metadata.fileId, () -> true);
        if(continueSuppliersOrDefault.getAsBoolean() && (metadata.chunkIndex != metadata.totalChunks-1)){
            sendDownloadChunkRequest(metadata.fileId,metadata.chunkIndex+1,metadata.offset+65536);
        }
    }
}
