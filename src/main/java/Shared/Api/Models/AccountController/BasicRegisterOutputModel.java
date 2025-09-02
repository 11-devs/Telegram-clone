package Shared.Api.Models.AccountController;

public class BasicRegisterOutputModel {
    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public BasicRegisterOutputModel(String accessKey) {
        this.accessKey = accessKey;
    }

    private String accessKey;

}
