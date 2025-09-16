package Shared.Api.Models.AccountController;

public class VerifyCodeInputModel {
    private String otp;
    private String phoneNumber;
    private String pendingId;
    private String deviceInfo;
    private String purpose; // Added for different OTP purposes

    public VerifyCodeInputModel(String pendingId, String phoneNumber, String otp, String deviceInfo) {
        this.otp = otp;
        this.phoneNumber = phoneNumber;
        this.pendingId = pendingId;
        this.deviceInfo = deviceInfo;
        this.purpose = "login"; // Default purpose
    }

    public VerifyCodeInputModel(String pendingId, String phoneNumber, String otp, String deviceInfo, String purpose) {
        this.otp = otp;
        this.phoneNumber = phoneNumber;
        this.pendingId = pendingId;
        this.deviceInfo = deviceInfo;
        this.purpose = purpose;
    }

    public String getDeviceInfo() {
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

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
