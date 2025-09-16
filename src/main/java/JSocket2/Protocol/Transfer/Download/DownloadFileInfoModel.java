package JSocket2.Protocol.Transfer.Download;

public class DownloadFileInfoModel {
    private String fileId;
    private String fileName;
    private String fileExtension;
    private long fileLength;

    public DownloadFileInfoModel(String fileId,String fileName,String fileExtension,long fileLength){
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.fileLength = fileLength;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileLength() {
        return fileLength;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }
}
