package Shared.Api.Models.AccountController;

public class RequestCodePhoneNumberInputModel {
    private String phoneNumber;
    private String via;
    private String deviceInfo;
    private String purpose; // Added for different OTP purposes (login, password_reset)

    public RequestCodePhoneNumberInputModel(String phoneNumber, String via, String deviceInfo) {
        this.phoneNumber = phoneNumber;
        this.via = via;
        this.deviceInfo = deviceInfo;
        this.purpose = "login"; // Default purpose
    }

    public RequestCodePhoneNumberInputModel(String phoneNumber, String via, String deviceInfo, String purpose) {
        this.phoneNumber = phoneNumber;
        this.via = via;
        this.deviceInfo = deviceInfo;
        this.purpose = purpose;
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

    public String getVia() {
        return via;
    }

    public void setVia(String via) {
        this.via = via;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
