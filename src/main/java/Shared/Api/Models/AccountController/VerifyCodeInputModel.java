package Shared.Api.Models.AccountController;

public class VerifyCodeInputModel {
    private String otp;
    private String phoneNumber;
    private String pendingId;
    private String deviceInfo;

    public VerifyCodeInputModel(String pendingId, String phoneNumber, String otp, String deviceInfo) {
        this.otp = otp;
        this.phoneNumber = phoneNumber;
        this.pendingId = pendingId;
        this.deviceInfo = deviceInfo;
    } public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }

    public String getPendingId() { return pendingId; }
    public void setPendingId(String pendingId) { this.pendingId = pendingId; }

    public String getPhoneNumber(){
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }
}
