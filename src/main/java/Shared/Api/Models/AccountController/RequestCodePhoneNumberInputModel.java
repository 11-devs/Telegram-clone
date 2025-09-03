package Shared.Api.Models.AccountController;

public class RequestCodePhoneNumberInputModel {
    private String phoneNumber;
    private String via;
    private String deviceInfo;

    public RequestCodePhoneNumberInputModel(String phoneNumber, String via, String deviceInfo) {
        this.phoneNumber = phoneNumber;
        this.via = via;
        this.deviceInfo = deviceInfo;
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
}
