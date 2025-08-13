package JSocket2.Protocol.Transfer.Download;

public class DownloadRequestMetadata {
    private String fileId;
    public DownloadRequestMetadata( String fileId){
        this.fileId = fileId;

    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        fileId = fileId;
    }
}
