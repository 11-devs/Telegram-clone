package Shared.Api.Models.MembershipController;

public class LeaveChatOutputModel {
    private String message;

    public LeaveChatOutputModel(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}