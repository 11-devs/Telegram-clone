package Shared.Api.Models.AccountController;

public class RequestCodeOutputModel {
    private String pendingId;
    private String Status;
    public RequestCodeOutputModel() {
    }
    public RequestCodeOutputModel(String pendingId, String status) {
        this.pendingId = pendingId;
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
}
