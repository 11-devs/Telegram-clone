package JSocket2.Protocol.Transfer;

import JSocket2.Protocol.StatusCode;
import JSocket2.Protocol.Message;
import JSocket2.Protocol.MessageHandler;
import JSocket2.Protocol.MessageHeader;
import JSocket2.Protocol.Rpc.RpcResponseMetadata;
import com.google.gson.Gson;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

public abstract class FileTransferManager {

    protected static final Gson gson = new Gson();
    protected final Map<UUID, CompletableFuture<Message>> pendingRequests;
    protected final MessageHandler handler;

    protected final Map<String, IProgressListener> progressListeners = new ConcurrentHashMap<>();
    protected final Map<String, BooleanSupplier> continueSuppliers = new ConcurrentHashMap<>();
    protected final Map<String, TransferFiles> activeTransfers = new ConcurrentHashMap<>();
    public void registerTransferListener(String fileId,
                                         IProgressListener listener,
                                         BooleanSupplier shouldContinue) {
        if (listener != null) {
            progressListeners.put(fileId, listener);
        }
        if (shouldContinue != null) {
            continueSuppliers.put(fileId, shouldContinue);
        }
    }
    protected void unregisterTransfer(String fileId) {
        progressListeners.remove(fileId);
        continueSuppliers.remove(fileId);
    }
    public FileTransferManager(MessageHandler handler,Map<UUID, CompletableFuture<Message>> pendingRequests ) {
        this.handler = handler;
        this.pendingRequests = pendingRequests;
    }
        public Map<String, TransferFiles> getActiveTransfers(){
            return activeTransfers;
        }
    protected void sendFileInternal(UUID requestId, String fileId, File file, int startIndex, long startOffset, int chunkSize, long fileSize) throws IOException {

        System.out.println("Sending file started. file ID: " + fileId);

        try (InputStream inStream = new BufferedInputStream(new FileInputStream(file))) {
            sendChunks(requestId, inStream, fileId, fileSize, chunkSize, startIndex, startOffset);
        }
    }
    public void deactivateTransfers() throws IOException {
        for(var fileId : activeTransfers.keySet()){
            TransferFiles transferFiles = activeTransfers.remove(fileId);
            transferFiles.closeAndCleanup();
        }
    }
    protected void createTransfer(String fileId,String fileName,String fileExtension,String destinationPath,int totalChunksCount,long fileSize) throws IOException {
        activeTransfers.put(fileId, TransferFiles.CreateDefault(fileId,fileName,fileExtension,destinationPath,totalChunksCount,fileSize));
    }
    protected void createTransfer(String fileId,String fileName,String fileExtension,String destinationPath,String tempPath,int totalChunksCount,long fileSize) throws IOException {
        activeTransfers.put(fileId, TransferFiles.Create(fileId,fileName,fileExtension,destinationPath,tempPath,totalChunksCount,fileSize));
    }
    protected boolean CanLoadTransfer(String fileId){
        return TransferFiles.canLoad(fileId);
    }
    protected TransferFiles LoadTransfer(String fileId) throws IOException {
        var transferFiles = TransferFiles.Load(fileId);
        activeTransfers.put(fileId,transferFiles );
        return transferFiles;
    }

    protected void writeChunk(String fileId, long offset,int chunkIndex, byte[] data) throws IOException {
        if (!activeTransfers.containsKey(fileId)) {
            throw new IllegalStateException("Transfer with fileId \"" + fileId + "\" is not initialized.");
        }
        TransferFiles transferFiles = activeTransfers.get(fileId);

        transferFiles.writeChunk(offset, data, chunkIndex);
    }
    protected void closeTransfer(String fileId) throws IOException {
        if (!activeTransfers.containsKey(fileId)) {
            throw new IllegalStateException("Transfer with fileId \"" + fileId + "\" is not active.");
        }
        TransferFiles transferFiles = activeTransfers.remove(fileId);
        transferFiles.closeAndCleanup();
    }
    protected void finishTransfer(String fileId) throws IOException {
        if (!activeTransfers.containsKey(fileId)) {
            throw new IllegalStateException("Transfer with fileId \"" + fileId + "\" is not active.");
        }
        TransferFiles transferFiles = activeTransfers.remove(fileId);
        transferFiles.finalizeTransfer();
    }
    public void ProcessSendChunk(Message message) throws IOException {
        var metadata = gson.fromJson(
                new String(message.getMetadata(), StandardCharsets.UTF_8),
                SendChunkMetadata.class
        );
        byte[] chunkData = message.getPayload();
        receiveChunk(message.header.uuid,metadata,chunkData);

    }

    protected void receiveChunk(UUID requestId,
                                SendChunkMetadata metadata,byte[] chunkData) throws IOException {
        receiveChunk(requestId,metadata,chunkData,true);
    }
    protected void receiveChunk(UUID requestId,
                                  SendChunkMetadata metadata,byte[] chunkData,boolean sendAck) throws IOException {

        writeChunk(metadata.fileId, metadata.offset,metadata.chunkIndex, chunkData);
        if(metadata.chunkIndex == metadata.totalChunks-1){
            finishTransfer(metadata.fileId);
        }
        var responseMetadata = gson.toJson(new RpcResponseMetadata(StatusCode.OK.code, ""));
        var responsePayload = ByteBuffer.allocate(4).putInt(metadata.chunkIndex).array();;
    if(sendAck) {
    var ackMessage = new Message(
            MessageHeader.BuildUploadAckHeader(
                    requestId, responseMetadata.getBytes(StandardCharsets.UTF_8).length
            ),
            responseMetadata.getBytes(StandardCharsets.UTF_8),
            responsePayload
    );
    handler.write(ackMessage);

}
    }
    protected void sendChunk(UUID requestId,
                             byte[] chunkData,
                             String fileId,
                             int chunkIndex,
                             int totalChunks,
                             long offset) throws IOException {
        sendChunk(requestId,chunkData,fileId,chunkIndex,totalChunks,offset,true);
    }
    protected void sendChunk(UUID requestId,
                           byte[] chunkData,
                           String fileId,
                           int chunkIndex,
                           int totalChunks,
                           long offset,boolean hasAck) throws IOException {
        SendChunkMetadata chunkMetadata = new SendChunkMetadata();
        chunkMetadata.fileId = fileId;
        chunkMetadata.chunkIndex = chunkIndex;
        chunkMetadata.totalChunks = totalChunks;
        chunkMetadata.offset = offset;

        byte[] metaBytes = gson.toJson(chunkMetadata).getBytes(StandardCharsets.UTF_8);

        Message chunkMsg = new Message(
                MessageHeader.BuildSendChunkHeader(requestId, true, metaBytes.length, chunkData.length),
                metaBytes,
                chunkData
        );

        CompletableFuture<Message> chunkFuture = new CompletableFuture<>();
        pendingRequests.put(requestId, chunkFuture);

        handler.write(chunkMsg);
        if(hasAck){
            try {
                var message = chunkFuture.join();
                var chunkindex = message.getPayload();
            } finally {
                pendingRequests.remove(requestId);
            }
        }
    }
    protected void sendChunks(UUID requestId,
                            InputStream inStream,
                            String fileId,
                            long fileSize,
                            int chunkSize,
                            int startIndex,
                            long startOffset) throws IOException {

        if (startOffset > 0) {
            long skipped = 0;
            while (skipped < startOffset) {
                long actuallySkipped = inStream.skip(startOffset - skipped);
                if (actuallySkipped <= 0) {
                    throw new IOException("Unable to skip to resume offset: " + startOffset);
                }
                skipped += actuallySkipped;
            }
        }

        byte[] buffer = new byte[chunkSize];
        int bytesRead;
        int index = startIndex;
        long offset = startOffset;
        long uploaded = startOffset;
        int totalChunks = (int) ((fileSize + chunkSize - 1) / chunkSize);

        while ((bytesRead = inStream.read(buffer)) != -1) {
            BooleanSupplier continueSuppliersOrDefault = continueSuppliers.getOrDefault(fileId, () -> true);
            if (!continueSuppliersOrDefault.getAsBoolean()) {
                throw new IOException("Connection lost during upload of " + fileId);
            }
            byte[] chunkData = new byte[bytesRead];
            System.arraycopy(buffer, 0, chunkData, 0, bytesRead);
            sendChunk(requestId, chunkData, fileId, index, totalChunks, offset);
            uploaded += bytesRead;
            offset += bytesRead;
            index++;
            IProgressListener progressListener = progressListeners.get(fileId);
            if (progressListener != null) {
                progressListener.onProgress(uploaded, fileSize);
            }
        }
    }
        protected void sendSpecificChunk(UUID requestId,
                InputStream inStream,
                String fileId,
        long fileSize,
        int chunkSize,
        int chunkIndex) throws IOException {

            long offset = (long) chunkSize * chunkIndex;

            if (offset >= fileSize) {
                throw new IllegalArgumentException("Chunk index out of file size range.");
            }

            long skipped = 0;
            while (skipped < offset) {
                long actuallySkipped = inStream.skip(offset - skipped);
                if (actuallySkipped <= 0) {
                    throw new IOException("Unable to skip to chunk offset: " + offset);
                }
                skipped += actuallySkipped;
            }

            byte[] buffer = new byte[chunkSize];
            int bytesRead = inStream.read(buffer);

            if (bytesRead <= 0) {
                throw new IOException("Failed to read chunk at offset: " + offset);
            }

            byte[] chunkData = new byte[bytesRead];
            System.arraycopy(buffer, 0, chunkData, 0, bytesRead);

            int totalChunks = (int) ((fileSize + chunkSize - 1) / chunkSize);

            sendChunk(requestId, chunkData, fileId, chunkIndex, totalChunks, offset,false);
        }

    }
