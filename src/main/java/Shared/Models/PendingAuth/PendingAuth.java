package Shared.Models.PendingAuth;

import java.time.Instant;
import java.util.UUID;

public class PendingAuth {
    private UUID id;
    private String phoneNumber;
    private String hashedOtp;
    private Instant otpExpiresAt;
    private int attempts;
    private int maxAttempts = 5;
    private String stage;
    private String tempTokenId;
    private String lastIp;
    private String deviceInfo;
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getHashedOtp() { return hashedOtp; }
    public void setHashedOtp(String hashedOtp) { this.hashedOtp = hashedOtp; }

    public Instant getOtpExpiresAt() { return otpExpiresAt; }
    public void setOtpExpiresAt(Instant otpExpiresAt) { this.otpExpiresAt = otpExpiresAt; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }

    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public String getTempTokenId() { return tempTokenId; }
    public void setTempTokenId(String tempTokenId) { this.tempTokenId = tempTokenId; }

    public String getLastIp() { return lastIp; }
    public void setLastIp(String lastIp) { this.lastIp = lastIp; }

    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
