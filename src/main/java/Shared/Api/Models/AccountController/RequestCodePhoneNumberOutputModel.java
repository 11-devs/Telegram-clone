package Shared.Api.Models.AccountController;

public class RequestCodePhoneNumberOutputModel {
    private String pendingId;
    private String phoneNumber;
    private String Status;
    public RequestCodePhoneNumberOutputModel() {
    }
    public RequestCodePhoneNumberOutputModel(String pendingId, String content, String status) {
        this.pendingId = pendingId;
        this.phoneNumber = content;
        Status = status;
    }

    public String getPendingId() {
        return pendingId;
    }

    public void setPendingId(String pendingId) {
        this.pendingId = pendingId;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

}
