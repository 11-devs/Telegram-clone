package Shared.Api.Models.AccountController;


public class VerifyCodeOutputModel {
    private String status; // logged_in / need_register / need_password
    private String tempToken; // optional
    private String accessKey;


    public VerifyCodeOutputModel() {}
    public VerifyCodeOutputModel(String status) { this.status = status; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTempToken() { return tempToken; }
    public void setTempToken(String tempToken) { this.tempToken = tempToken; }

    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }

}
