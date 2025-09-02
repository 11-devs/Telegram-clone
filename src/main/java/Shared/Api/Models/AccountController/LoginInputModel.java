package Shared.Api.Models.AccountController;

public class LoginInputModel {
    private String password;
    private String phoneNumber;
    private String deviceInfo;
    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
    public String getPhoneNumber(){
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}