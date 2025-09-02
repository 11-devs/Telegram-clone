package Shared.Api.Models.AccountController;

public class VerifyCodeInputModel {
    private String otp;
    private String phoneNumber;
    private String pendingId;

    public VerifyCodeInputModel(String pendingId, String phoneNumber,String otp) {
        this.otp = otp;
        this.phoneNumber = phoneNumber;
        this.pendingId = pendingId;
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
