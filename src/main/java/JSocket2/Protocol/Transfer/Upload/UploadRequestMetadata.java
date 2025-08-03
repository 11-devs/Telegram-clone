package JSocket2.Protocol.Transfer.Upload;

public class UploadRequestMetadata {
    private String userId;
    private String fileName;
    private String fileExtension;
    private long fileLength;

    public UploadRequestMetadata(String userId, String fileName, String fileExtension, long file_length) {
        this.userId = userId;
        this.fileName = fileName;
        this.fileLength = file_length;
        this.fileExtension = fileExtension;
    }

    public String getFileExtension(){
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String user_id) {
        this.userId = user_id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String file_name) {
        this.fileName = file_name;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long file_length) {
        this.fileLength = file_length;
    }
}

