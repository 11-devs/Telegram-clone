package JSocket2.Protocol.Transfer;

import JSocket2.Protocol.StatusCode;
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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ServerFileTransferManager extends FileTransferManager {
    private final String SAVE_PATH = "src/files";
    public ServerFileTransferManager(MessageHandler handler, Map<UUID, CompletableFuture<Message>> pendingRequests) {
        super(handler, pendingRequests);
    }
    public void ProcessUploadRequest(Message message) throws IOException {
        var metadata = gson.fromJson(new String(message.getMetadata(), StandardCharsets.UTF_8), UploadRequestMetadata.class);

        var responseMetadata = gson.toJson(new RpcResponseMetadata(StatusCode.OK.code, ""));
        var fileId = UUID.randomUUID().toString();
        FileInfoModel fileInfoModel = new FileInfoModel(fileId);
        var responsePayload = gson.toJson(fileInfoModel);
        var msg = new Message(
                MessageHeader.BuildRpcResponseHeader(message.header.uuid, false, responseMetadata.length(), responsePayload.length()),
                responseMetadata.getBytes(StandardCharsets.UTF_8),
                responsePayload.getBytes(StandardCharsets.UTF_8)
        );
        int totalChunksCount = (int) Math.ceil((double) metadata.getFileLength() / fileInfoModel.ChunkSize);
        createTransfer(fileId,metadata.getFileName(),metadata.getFileExtension(),SAVE_PATH,totalChunksCount,metadata.getFileLength());
        handler.write(msg);
    }
    private File LoadFile(String fileId) throws IOException {
        String systemTempDir = System.getProperty("java.io.tmpdir");
        String basePath = Paths.get(systemTempDir, "JTelegram").toString();
        File infoFile = new File(basePath, fileId + ".info");
        Gson gson = new Gson();
        TransferInfo info;
        try (FileReader reader = new FileReader(infoFile)) {
            info = gson.fromJson(reader, TransferInfo.class);
        }
        File finalFile = new File(SAVE_PATH, info.getFileName() + "." + info.getFileExtension());
        return finalFile;
    }

    public void ProcessDownloadRequest(Message message) throws IOException {
        var metadata = gson.fromJson(new String(message.getMetadata(), StandardCharsets.UTF_8), DownloadRequestMetadata.class);
        File file = LoadFile(metadata.getFileId());
        var responseMetadata = gson.toJson(new RpcResponseMetadata(StatusCode.OK.code, ""));
        DownloadFileInfoModel info = new DownloadFileInfoModel(metadata.getFileId(), FileUtil.getFileNameWithoutExtension(file),FileUtil.getFileExtension(file),file.length());
        var responsePayload = gson.toJson(info);
        var msg = new Message(
                MessageHeader.BuildRpcResponseHeader(message.header.uuid, false, responseMetadata.length(), responsePayload.length()),
                responseMetadata.getBytes(StandardCharsets.UTF_8),
                responsePayload.getBytes(StandardCharsets.UTF_8)
        );
        handler.write(msg);
    }
    public void ProcessDownloadChunkRequest(Message message) throws IOException {
        var metadata = gson.fromJson(new String(message.getMetadata(), StandardCharsets.UTF_8), DownloadChunkRequestMetadata.class);
        File file = LoadFile(metadata.getFileId());
        try (InputStream input = new FileInputStream(file)) {
            sendSpecificChunk(message.header.uuid, input, metadata.getFileId(), file.length(), 65536, metadata.getStartChunkIndex());
        }
    }
    public void ProcessUploadResumeRequest(UUID requestId, UploadResumeRequestMetadata metadata) throws IOException {
        String fileId = metadata.FileId;
        var transferFiles = LoadTransfer(fileId);
        UploadResumeResultModel result = new UploadResumeResultModel(fileId, transferFiles.getinfo().getLastChunkIndex() +1, transferFiles.getinfo().getLastWrittenOffset(),65536,transferFiles.getinfo().getFileSize());
        var responseMetadata = gson.toJson(new RpcResponseMetadata(StatusCode.OK.code, ""));
        var responsePayload = gson.toJson(result);

        var resultMessage = new Message(
                MessageHeader.BuildRpcResponseHeader(
                        requestId,true,responseMetadata.getBytes(StandardCharsets.UTF_8).length, responsePayload.getBytes(StandardCharsets.UTF_8).length
                ),
                responseMetadata.getBytes(StandardCharsets.UTF_8),
                responsePayload.getBytes(StandardCharsets.UTF_8)
        );
        handler.write(resultMessage);
    }


}
