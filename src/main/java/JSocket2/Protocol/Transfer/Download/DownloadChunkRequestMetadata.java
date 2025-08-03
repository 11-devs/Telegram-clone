package JSocket2.Protocol.Transfer.Download;

public class DownloadChunkRequestMetadata {
    private String fileId;
    private int startChunkIndex;
    private long StartOffset;
    public DownloadChunkRequestMetadata(String fileId, int startChunkIndex, long StartOffset){
        this.fileId = fileId;
        this.startChunkIndex = startChunkIndex;
        this.StartOffset = StartOffset;
    }
    public DownloadChunkRequestMetadata(String fileId){
        this.fileId = fileId;
        this.startChunkIndex = 0;
        this.StartOffset = 0;
    }

    public long getStartOffset() {
        return StartOffset;
    }

    public void setStartOffset(int startOffset) {
        StartOffset = startOffset;
    }

    public int getStartChunkIndex() {
        return startChunkIndex;
    }

    public void setStartChunkIndex(int startChunkIndex) {
        this.startChunkIndex = startChunkIndex;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        fileId = fileId;
    }
}
