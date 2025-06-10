package JSocket.Protocol.Transfer;

public class TransferInfo {
    private String fileId;
    private String fileExtension;
    private String fileName;
    private String destinationPath;
    private TransferState transferState;
    private long lastWrittenOffset;
    private int lastChunkIndex;
    private int totalChunksCount;
    private long fileSize;

    public TransferInfo(String fileId,String fileName,String fileExtension,String destinationPath, long lastWrittenOffset, int lastChunkIndex,int totalChunksCount,long fileSize) {
        this.fileId = fileId;
        this.fileExtension = fileExtension;
        this.fileName = fileName;
        this.lastWrittenOffset = lastWrittenOffset;
        this.lastChunkIndex = lastChunkIndex;
        this.destinationPath = destinationPath;
        this.totalChunksCount = totalChunksCount;
        this.transferState = TransferState.InProcess;
        this.fileSize = fileSize;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public TransferState getTransferState() {
        return transferState;
    }

    public int getTotalChunksCount() {
        return totalChunksCount;
    }

    public void setTotalChunksCount(int totalChunksCount) {
        this.totalChunksCount = totalChunksCount;
    }

    public void setTransferState(TransferState transferState) {
        this.transferState = transferState;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }

    public String getFileId() {
        return fileId;
    }
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileExtension() {
        return fileExtension;
    }
    public String getFileName(){
        return fileName;
    }

    public long getLastWrittenOffset() {
        return lastWrittenOffset;
    }
    public void setLastWrittenOffset(long lastWrittenOffset) {
        this.lastWrittenOffset = lastWrittenOffset;
    }

    public int getLastChunkIndex() {
        return lastChunkIndex;
    }
    public void setLastChunkIndex(int lastChunkIndex) {
        this.lastChunkIndex = lastChunkIndex;
    }
}
