package Shared.Api.Models.AccountController;

public class BasicRegisterInputModel {
    private String firstName;
    private String lastName;
    private String profilePictureId;
    private String phoneNumber;
    private String deviceInfo;
    public BasicRegisterInputModel(){

    }
    public BasicRegisterInputModel( String phoneNumber,String firstName,String lastName,String profilePictureId,String deviceInfo){
        this.phoneNumber = phoneNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePictureId = profilePictureId;
        this.deviceInfo = deviceInfo;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

}
