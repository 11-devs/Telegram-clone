package Shared.Api.Models.ContactController;

public class RemoveContactOutputModel {
    private String status;

    public RemoveContactOutputModel(String status) {
        this.status = status;
    }
    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}