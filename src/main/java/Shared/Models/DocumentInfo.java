package Shared.Models;
import JSocket2.Protocol.Transfer.TransferInfo;
import java.io.File;

public class DocumentInfo {
    private String fileId;

    private String fileName;
    private long fileSize;
    private String fileExtension;
    private String originalPath;
    private String storedPath;
    private String senderName;
    private boolean isUploaded;

    public DocumentInfo(String fileName, long fileSize, String fileExtension, String originalPath) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileExtension = fileExtension;
        this.originalPath = originalPath;
        this.isUploaded = false;
    }

    public DocumentInfo(TransferInfo transferInfo) {
        this.fileId = transferInfo.getFileId();
        this.fileName = transferInfo.getFileName() + "." + transferInfo.getFileExtension();
        this.fileSize = transferInfo.getFileSize();
        this.fileExtension = transferInfo.getFileExtension();
        this.storedPath = new File(transferInfo.getDestinationPath(), this.fileName).getPath();
        this.isUploaded = true;
    }

    // Getters and setters
    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getFileExtension() { return fileExtension; }
    public void setFileExtension(String fileExtension) { this.fileExtension = fileExtension; }

    public String getOriginalPath() { return originalPath; }
    public void setOriginalPath(String originalPath) { this.originalPath = originalPath; }

    public String getStoredPath() { return storedPath; }
    public void setStoredPath(String storedPath) { this.storedPath = storedPath; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public boolean isUploaded() { return isUploaded; }
    public void setUploaded(boolean uploaded) { isUploaded = uploaded; }
}
