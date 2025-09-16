package Shared.Api.Models.AccountController;

import java.util.UUID;

public class VerifyCodeOutputModel {
    private String status; // logged_in / need_register / need_password / password_reset_required
    private String tempToken; // optional
    private String accessKey;
    private String phoneNumber; // Added for password reset flow
    private String pendingId;   // Added for password reset flow

    public VerifyCodeOutputModel() {}
    public VerifyCodeOutputModel(String status) { this.status = status; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTempToken() { return tempToken; }
    public void setTempToken(String tempToken) { this.tempToken = tempToken; }

    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPendingId() {
        return pendingId;
    }

    public void setPendingId(String pendingId) {
        this.pendingId = pendingId;
    }
}