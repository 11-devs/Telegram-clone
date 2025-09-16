package Shared.Api.Models.ViewController;

public class SetViewOnMessageOutputModel {
    private String status;

    public SetViewOnMessageOutputModel(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}