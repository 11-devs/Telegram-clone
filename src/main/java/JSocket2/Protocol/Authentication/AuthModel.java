package JSocket2.Protocol.Authentication;

public class AuthModel {
    private int accessKeyCount = 0;
    private String[] accessKeys;
    private String fileHash;

    public AuthModel(String[] accessKeys, int accessKeyCount) {
        this.accessKeys = accessKeys;
        this.accessKeyCount = accessKeyCount;
    }

    public AuthModel(String[] accessKeys, int accessKeyCount, String fileHash) {
        this.accessKeys = accessKeys;
        this.accessKeyCount = accessKeyCount;
        this.fileHash = fileHash;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String[] getAccessKeys() {
        return accessKeys;
    }

    public void setAccessKeys(String[] accessKeys) {
        this.accessKeys = accessKeys;
    }

    public int getAccessKeyCount() {
        return accessKeyCount;
    }

    public void setAccessKeyCount(int accessKeyCount) {
        this.accessKeyCount = accessKeyCount;
    }
}