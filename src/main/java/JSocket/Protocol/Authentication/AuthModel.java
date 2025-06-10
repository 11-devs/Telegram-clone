package JSocket.Protocol.Authentication;

public class AuthModel {
    private int accessKeyCount;
    private String[] accessKeys;
    public AuthModel(String[] accessKeys,int accessKeyCount){
        this.accessKeys = accessKeys;
        this.accessKeyCount = accessKeyCount;
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
