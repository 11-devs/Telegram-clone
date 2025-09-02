package Shared.Models.PendingAuth;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pending_auth")
public class PendingAuth {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "hashed_otp")
    private String hashedOtp;

    @Column(name = "otp_expires_at")
    private Instant otpExpiresAt;

    @Column(name = "attempts")
    private int attempts;

    @Column(name = "max_attempts")
    private int maxAttempts = 5;

    @Column(name = "stage")
    private String stage;

    @Column(name = "temp_token_id")
    private String tempTokenId;

    @Column(name = "last_ip")
    private String lastIp;

    @Column(name = "device_info")
    private String deviceInfo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    // Hibernate requires a no-arg constructor
    public PendingAuth() {
    }

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
