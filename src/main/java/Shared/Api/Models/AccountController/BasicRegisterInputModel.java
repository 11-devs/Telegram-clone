package Shared.Api.Models.AccountController;

public class BasicRegisterInputModel {
    private String firstName;
    private String lastName;
    private String profilePictureId;
    public BasicRegisterInputModel(){

    }
    public BasicRegisterInputModel(String firstName,String lastName,String profilePictureId){
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePictureId = profilePictureId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getProfilePictureId() {
        return profilePictureId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setProfilePictureId(String profilePictureId) {
        this.profilePictureId = profilePictureId;
    }
}
