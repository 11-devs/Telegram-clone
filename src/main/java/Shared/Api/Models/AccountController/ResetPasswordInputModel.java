package Shared.Api.Models.AccountController;

public class ResetPasswordInputModel {
    private String phoneNumber;
    private String pendingId;
    private String newPassword;

    public ResetPasswordInputModel(String phoneNumber, String pendingId, String newPassword) {
        this.phoneNumber = phoneNumber;
        this.pendingId = pendingId;
        this.newPassword = newPassword;
    }

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

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
