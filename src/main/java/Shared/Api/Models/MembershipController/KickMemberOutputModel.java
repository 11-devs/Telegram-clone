package Shared.Api.Models.MembershipController;

public class KickMemberOutputModel {
    private String message;

    public KickMemberOutputModel(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}