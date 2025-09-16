package Shared.Api.Models.AccountController;

public class VerifyCodeEmailInputModel {
    private String otp;
    private String email;
    private String pendingId;

    public VerifyCodeEmailInputModel(String pendingId, String email, String otp) {
        this.otp = otp;
        this.email = email;
        this.pendingId = pendingId;
    }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }

    public String getPendingId() { return pendingId; }
    public void setPendingId(String pendingId) { this.pendingId = pendingId; }

    public String getEmail(){
        return email;
    }
    public void setEmail(String email){
        this.email = email;
    }
}
