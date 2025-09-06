package Shared.Api.Models.AccountController;

public class SetProfilePictureInputModel {
    private String profilePictureId;

    public SetProfilePictureInputModel() {}

    public SetProfilePictureInputModel(String profilePictureId) {
        this.profilePictureId = profilePictureId;
    }

    public String getProfilePictureId() {
        return profilePictureId;
    }

    public void setProfilePictureId(String profilePictureId) {
        this.profilePictureId = profilePictureId;
    }
}