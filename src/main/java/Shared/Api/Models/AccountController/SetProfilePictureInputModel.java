package Shared.Api.Models.AccountController;

public class SetProfilePictureInputModel {
    private String profilePictureMediaId;

    public SetProfilePictureInputModel() {}

    public SetProfilePictureInputModel(String profilePictureMediaId,String profilePictureFileId) {
        this.profilePictureMediaId = profilePictureMediaId;
        this.profilePictureFileId = profilePictureFileId;
    }

    public String getProfilePictureMediaId() {
        return profilePictureMediaId;
    }

    public void setProfilePictureMediaId(String profilePictureMediaId) {
        this.profilePictureMediaId = profilePictureMediaId;
    }
    private String profilePictureFileId;


    public String getProfilePictureFileId() {
        return profilePictureFileId;
    }

    public void setProfilePictureFileId(String profilePictureFileId) {
        this.profilePictureFileId = profilePictureFileId;
    }
}