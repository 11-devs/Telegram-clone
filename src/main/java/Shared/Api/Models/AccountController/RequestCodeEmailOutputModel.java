package Shared.Api.Models.AccountController;

public class RequestCodeEmailOutputModel {
    private String pendingId;
    private String email;
    private String Status;
    public RequestCodeEmailOutputModel() {
    }
    public RequestCodeEmailOutputModel(String pendingId, String content, String status) {
        this.pendingId = pendingId;
        this.email = content;
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
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
