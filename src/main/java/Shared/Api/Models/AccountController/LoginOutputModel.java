package Shared.Api.Models.AccountController;


public class LoginOutputModel {
    private String status;
    private String accessKey;

    public LoginOutputModel() {}
    public LoginOutputModel(String status) { this.status = status; this.status = status; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }

}