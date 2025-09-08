package Server.Controllers;

import JSocket2.Protocol.Rpc.RpcControllerBase;
import JSocket2.Protocol.Rpc.RpcResponse;
import JSocket2.Protocol.StatusCode;
import Server.DaoManager;
import Server.Events.ChatInfoChangedEvent;
import Shared.Api.Models.AccountController.*;
import Shared.Events.Models.ChatInfoChangedEventModel;
import Shared.Models.Account.Account;
import Shared.Models.Chat.SavedMessages;
import Shared.Models.Contact.Contact;
import Shared.Models.Media.Media;
import Shared.Models.Membership.Membership;
import Shared.Models.PendingAuth.PendingAuth;
import Shared.Models.Session.Session;
import Shared.Utils.PasswordUtil;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class AccountRpcController extends RpcControllerBase {
    private final DaoManager daoManager;
    private final Duration OTP_EXPIRY_DURATION = Duration.ofMinutes(2);
    private final Duration OTP_RESEND_COOLDOWN = Duration.ofSeconds(60);
    private final int MAX_OTP_ATTEMPTS = 5;
    private final Duration LOCKOUT_DURATION = Duration.ofMinutes(5);
    private final ChatInfoChangedEvent chatInfoChangedEvent;
    public AccountRpcController(DaoManager daoManager, ChatInfoChangedEvent chatInfoChangedEvent) {
        this.daoManager = daoManager;
        this.chatInfoChangedEvent = chatInfoChangedEvent;
    }
public RpcResponse<Boolean> isPhoneNumberRegistered(String phoneNumber){
   return Ok(daoManager.getAccountDAO().findByField("phoneNumber",phoneNumber) != null);
}
    public RpcResponse<Object> requestOTP(RequestCodePhoneNumberInputModel model) {
        boolean cooldown = false;
        var pendings = daoManager.getPendingAuthDAO().findAllByField("phoneNumber", model.getPhoneNumber());
        Optional<PendingAuth> optionalPending = pendings.stream()
                .filter(p -> p.getDeviceInfo() != null && p.getDeviceInfo().equals(model.getDeviceInfo()))
                .findFirst();
        PendingAuth pending = optionalPending.orElse(null);

        // Check if account exists for password reset purpose
        Account account = daoManager.getAccountDAO().findByField("phoneNumber", model.getPhoneNumber());
        if ("password_reset".equals(model.getPurpose()) && account == null) {
            return BadRequest("account_not_found");
        }

        if (pending != null) {
            if (Instant.now().isBefore(pending.getLastRequestAt().plus(OTP_RESEND_COOLDOWN))) {
                cooldown = true;
            }
            if (pending.getAttempts() >= MAX_OTP_ATTEMPTS && Instant.now().isBefore(pending.getLockoutUntil())) {
                return BadRequest("too_many_attempts_try_later");
            }
        } else {
            pending = new PendingAuth();
            pending.setPhoneNumber(model.getPhoneNumber());
            pending.setStage("initial");
            pending.setDeviceInfo(model.getDeviceInfo());
            pending.setPurpose(model.getPurpose()); // Set the purpose
        }

        if (!cooldown) {
            var otp = generateOTP();
            pending.setOtp(otp);
            pending.setOtpExpiresAt(Instant.now().plus(OTP_EXPIRY_DURATION));
            pending.setLastRequestAt(Instant.now());
            pending.setAttempts(0);
            pending.setLockoutUntil(null);
            pending.setPurpose(model.getPurpose()); // Ensure purpose is updated/set

            if (pending.getId() == null) {
                daoManager.getPendingAuthDAO().insert(pending);
            } else {
                daoManager.getPendingAuthDAO().update(pending);
            }

            // Send OTP via the specified channel (telegram/sms)
            switch (model.getVia()) {
                case "telegram":
                    System.out.println("Telegram message sent to phoneNumber:" + model.getPhoneNumber() + " OTP:" + pending.getOtp() + " for purpose: " + model.getPurpose());
                    break;
                case "sms":
                    System.out.println("SMS sent to phoneNumber:" + model.getPhoneNumber() + " OTP:" + pending.getOtp() + " for purpose: " + model.getPurpose());
                    break;
                default:
                    return BadRequest("invalid_via_channel");
            }
        }

        RequestCodePhoneNumberOutputModel outputModel = new RequestCodePhoneNumberOutputModel();
        outputModel.setStatus(!cooldown ? "code_sent" : "cooldown");
        outputModel.setPendingId(pending.getId().toString());
        outputModel.setPhoneNumber(model.getPhoneNumber());
        return Ok(outputModel);
    }

    public RpcResponse<Object> requestPasswordReset(RequestCodePhoneNumberInputModel model) {
        Account account = daoManager.getAccountDAO().findByField("phoneNumber", model.getPhoneNumber());
        if (account == null) {
            return BadRequest("account_not_found");
        }

        if (account.getEmail() != null && !account.getEmail().isEmpty()) {
            PendingAuth pending = new PendingAuth();
            pending.setEmail(account.getEmail());
            pending.setPhoneNumber(model.getPhoneNumber());
            pending.setStage("password_reset_email_verification");
            pending.setDeviceInfo(model.getDeviceInfo());
            pending.setPurpose("password_reset");

            var otp = generateOTP();
            pending.setOtp(otp);
            pending.setOtpExpiresAt(Instant.now().plus(OTP_EXPIRY_DURATION));
            pending.setLastRequestAt(Instant.now());
            pending.setAttempts(0);
            daoManager.getPendingAuthDAO().insert(pending);

            System.out.println("Email sent to: " + account.getEmail() + " OTP: " + pending.getOtp() + " for purpose: password_reset");

            Map<String, String> payload = new HashMap<>();
            payload.put("status", "email_code_sent");
            payload.put("pendingId", pending.getId().toString());
            payload.put("email", account.getEmail());
            return Ok(payload);
        } else {
            Map<String, String> payload = new HashMap<>();
            payload.put("status", "no_email_setup");
            return Ok(payload);
        }
    }

    public RpcResponse<VerifyCodeOutputModel> verifyPasswordResetEmailOtp(VerifyCodeEmailInputModel model) {
        PendingAuth pending = daoManager.getPendingAuthDAO().findById(UUID.fromString(model.getPendingId()));
        if (pending == null) return response(StatusCode.BAD_REQUEST, "invalid_pending_id", null);
        if (!"password_reset_email_verification".equals(pending.getStage())) return response(StatusCode.BAD_REQUEST, "invalid_stage", null);

        daoManager.getPendingAuthDAO().delete(pending);

        PendingAuth resetPending = new PendingAuth();
        resetPending.setPhoneNumber(pending.getPhoneNumber());
        resetPending.setStage("password_reset_allowed");
        resetPending.setPurpose("password_reset");
        resetPending.setOtpExpiresAt(Instant.now().plus(Duration.ofMinutes(10)));
        daoManager.getPendingAuthDAO().insert(resetPending);

        VerifyCodeOutputModel output = new VerifyCodeOutputModel();
        output.setStatus("password_reset_required");
        output.setPendingId(resetPending.getId().toString());
        output.setPhoneNumber(resetPending.getPhoneNumber());
        return Ok(output);
    }

    public RpcResponse<Object> resetAccount(String phoneNumber, String deviceInfo) {
        Account account = daoManager.getAccountDAO().findByField("phoneNumber", phoneNumber);
        if (account == null) {
            return BadRequest("account_not_found");
        }

        account.setHashedPassword(null);
        account.setBio("");
        account.setUsername(null);
        account.setEmail(null);
        daoManager.getAccountDAO().update(account);

        List<Session> sessions = daoManager.getSessionDAO().findAllByField("account.id", account.getId());
        for (Session session : sessions) {
            daoManager.getSessionDAO().delete(session);
        }

        String accessKey = generateAccessKey(account, deviceInfo);

        Map<String, String> payload = new HashMap<>();
        payload.put("status", "account_reset_success");
        payload.put("accessKey", accessKey);

        return Ok(payload);
    }
    public RpcResponse<Object> verifyOTP(VerifyCodeInputModel model) {
        PendingAuth pending = daoManager.getPendingAuthDAO().findById(UUID.fromString(model.getPendingId()));

        if (pending == null) {
            return BadRequest("invalid_pending_id");
        }

//        if (!pending.getPhoneNumber().equals(model.getPhoneNumber())) {
//            return BadRequest("phone_number_mismatch");
//        }
//
//        if (Instant.now().isAfter(pending.getOtpExpiresAt())) {
//            daoManager.getPendingAuthDAO().delete(pending);
//            return BadRequest("otp_expired");
//        }
//        if (pending.getLockoutUntil() != null && Instant.now().isBefore(pending.getLockoutUntil())) {
//            return BadRequest("too_many_attempts_try_later");
//        }
//
//        if (!model.getOtp().equals(pending.getOtp())) {
//            pending.setAttempts(pending.getAttempts() + 1);
//            if (pending.getAttempts() >= MAX_OTP_ATTEMPTS) {
//                pending.setLockoutUntil(Instant.now().plus(LOCKOUT_DURATION));
//                daoManager.getPendingAuthDAO().update(pending);
//                return BadRequest("too_many_attempts_try_later");
//            }
//            daoManager.getPendingAuthDAO().update(pending);
//            return BadRequest("invalid_otp");
//        }

        // OTP is valid
        daoManager.getPendingAuthDAO().delete(pending); // OTP consumed

        VerifyCodeOutputModel output = new VerifyCodeOutputModel();
        output.setPhoneNumber(model.getPhoneNumber());
        output.setPendingId(model.getPendingId()); // Keep pendingId for next step if needed

        if ("password_reset".equals(pending.getPurpose())) {
            // For password reset, just indicate that it's verified and allow password reset
            output.setStatus("password_reset_required");
        } else {
            // Existing login/registration flow
            var account = daoManager.getAccountDAO().findByField("phoneNumber", model.getPhoneNumber());

            if (account == null) {
                output.setStatus("need_register");
            }
//            else if (account.getHashedPassword() != null && !account.getHashedPassword().isEmpty()) {
//                output.setStatus("need_password");
//            }
            else {
                var accessKey = generateAccessKey(account, model.getDeviceInfo());
                output.setStatus("logged_in");
                output.setAccessKey(accessKey);
            }
        }
        return Ok(output);
    }

    public RpcResponse<Object> login(LoginInputModel model) {

        var account = daoManager.getAccountDAO().findByField("phoneNumber",model.getPhoneNumber());
        if (account == null) return BadRequest("account_not_found");

        if (!PasswordUtil.verify(model.getPassword(), account.getHashedPassword())) {
            return BadRequest("invalid_password");
        }

        var accessToken = generateAccessKey(account,model.getDeviceInfo());

        LoginOutputModel output = new LoginOutputModel();
        output.setStatus("logged_in");
        output.setAccessKey(accessToken);

        return Ok(output);
    }
    public String generateAccessKey(Account account,String deviceInfo) {
        try {
            String salt = UUID.randomUUID().toString();
            String accessKey = PasswordUtil.hash(salt+deviceInfo+account.getId());
            Session newSession = new Session();
            newSession.setAccount(account);
            newSession.setAccessKey(accessKey);
            newSession.setSalt(salt);
            newSession.setDeviceInfo(deviceInfo);
            newSession.setActive(true);

            daoManager.getSessionDAO().insert(newSession);
            return accessKey;


        } catch (Exception e) {
            System.err.println("Error generating access key: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
    public RpcResponse<Object> basicRegister(BasicRegisterInputModel model){
        var account = new Account();
        account.setFirstName(model.getFirstName());
        account.setLastName(model.getLastName());
        account.setProfilePictureId(model.getProfilePictureId());
        account.setPhoneNumber(model.getPhoneNumber());
        account.setHashedPassword(PasswordUtil.hash("12345678"));
        daoManager.getAccountDAO().insert(account);
        var savedMessage = new SavedMessages(account);
        daoManager.getSavedMessagesDAO().insert(savedMessage);
        String accessKey = generateAccessKey(account,model.getDeviceInfo());
        var output = new BasicRegisterOutputModel(accessKey);
        return Ok(output);
    }
    public RpcResponse<Object> setEmail(String email){
        var account = daoManager.getAccountDAO().findById(getCurrentUser().getUserId());
        if (account == null) {
            return BadRequest("account_not_found");
        }
        var existingAccountWithEmail = daoManager.getAccountDAO().findByField("email", email);
        if (existingAccountWithEmail != null) {
            return BadRequest("email_already_in_use");
        }
        PendingAuth pending = daoManager.getPendingAuthDAO().findByField("email", email);
        if (pending == null) {
            pending = new PendingAuth();
            pending.setEmail(email);
            pending.setStage("email_verification");
        }

        var otp = generateOTP();
        pending.setOtp(otp);
        pending.setOtpExpiresAt(Instant.now().plus(OTP_EXPIRY_DURATION));
        pending.setAttempts(0);

        if (pending.getId() == null) {
            daoManager.getPendingAuthDAO().insert(pending);
        } else {
            daoManager.getPendingAuthDAO().update(pending);
        }

        // Send OTP to email
        System.out.println("email:" + email + " OTP:" + otp);
        RequestCodeEmailOutputModel model = new RequestCodeEmailOutputModel();
        model.setStatus("code_sent");
        model.setPendingId(pending.getId().toString());
        model.setEmail(email);
        return Ok(model);
    }
    public RpcResponse<Object> verifyEmailOtp(VerifyCodeEmailInputModel model){
        var account = daoManager.getAccountDAO().findById(getCurrentUser().getUserId());
        if (account == null) {
            return BadRequest("account_not_found");
        }
        PendingAuth pending = daoManager.getPendingAuthDAO().findById(UUID.fromString(model.getPendingId()));
        if (pending == null) {
            return BadRequest("invalid_pending_id");
        }
        if (Instant.now().isAfter(pending.getOtpExpiresAt())) {
            daoManager.getPendingAuthDAO().delete(pending);
            return BadRequest("otp_expired");
        }
        if (!model.getOtp().equals(pending.getOtp())) {
            pending.setAttempts(pending.getAttempts() + 1);
            if (pending.getAttempts() >= MAX_OTP_ATTEMPTS) {
                daoManager.getPendingAuthDAO().delete(pending);
                return BadRequest("too_many_attempts");
            }
            daoManager.getPendingAuthDAO().update(pending);
            return BadRequest("invalid_otp");
        }
        daoManager.getPendingAuthDAO().delete(pending);
        account.setEmail(model.getEmail());
        daoManager.getAccountDAO().update(account);
        return Ok();
    }

    public RpcResponse<Object> setPassword(String password){
        var account = daoManager.getAccountDAO().findById(getCurrentUser().getUserId());
        account.setHashedPassword(PasswordUtil.hash(password));
        daoManager.getAccountDAO().update(account);
        return Ok();
    }

    public RpcResponse<Object> resetPassword(ResetPasswordInputModel model) {
        PendingAuth pending = daoManager.getPendingAuthDAO().findById(UUID.fromString(model.getPendingId()));

        if (pending == null || !"password_reset_allowed".equals(pending.getStage()) || !pending.getPhoneNumber().equals(model.getPhoneNumber())) {
            return BadRequest("invalid_reset_request");
        }

        if(Instant.now().isAfter(pending.getOtpExpiresAt())){
            daoManager.getPendingAuthDAO().delete(pending);
            return BadRequest("reset_token_expired");
        }

        Account account = daoManager.getAccountDAO().findByField("phoneNumber", model.getPhoneNumber());
        if (account == null) {
            return BadRequest("account_not_found");
        }

        // Hash the new password and update the account
        account.setHashedPassword(PasswordUtil.hash(model.getNewPassword()));
        daoManager.getAccountDAO().update(account);

        daoManager.getPendingAuthDAO().delete(pending); // Consume the pending auth entry

        return Ok("password_reset_successful");
    }

    public RpcResponse<Object> setUsername(String username){
        UUID currentUserId = UUID.fromString(getCurrentUser().getUserId());
        Account account = daoManager.getAccountDAO().findById(currentUserId);
        if (account == null) {
            return NotFound();
        }

        // Check for username uniqueness
        if (username != null && !username.trim().isEmpty()) {
            Account existingAccount = daoManager.getAccountDAO().findByField("username", username);
            if (existingAccount != null && !existingAccount.getId().equals(currentUserId)) {
                return BadRequest("username_taken");
            }
        }

        account.setUsername(username);
        daoManager.getAccountDAO().update(account);
        return Ok();
    }
    public RpcResponse<Object> setBio(String bio){
        UUID currentUserId = UUID.fromString(getCurrentUser().getUserId());
        Account account = daoManager.getAccountDAO().findById(currentUserId);
        if (account == null) {
            return NotFound();
        }
        account.setBio(bio);
        daoManager.getAccountDAO().update(account);
        return Ok();
    }

    public RpcResponse<Object> updateName(UpdateNameInputModel model) {
        UUID currentUserId = UUID.fromString(getCurrentUser().getUserId());
        Account account = daoManager.getAccountDAO().findById(currentUserId);
        if (account == null) {
            return NotFound();
        }

        account.setFirstName(model.getFirstName());
        account.setLastName(model.getLastName());
        daoManager.getAccountDAO().update(account);
        ChatInfoChangedEventModel eventModel = new ChatInfoChangedEventModel(null, account.getFirstName() + " " + account.getLastName(),null);
        broadcastChatInfoUpdate(currentUserId,eventModel);
        return Ok();
    }
    private List<Contact> findContacts(UUID ownerId) {
        String jpql = "SELECT c FROM Contact c WHERE c.owner.id = :ownerId";
        var contacts = daoManager.getContactDAO().findByJpql(jpql, query -> {
            query.setParameter("ownerId", ownerId);
        });
        return contacts;
    }
    // This method should be placed in your MembershipDAO or a suitable generic repository.
// It assumes your base findByJpql method returns a List<?> which can be cas

    private void broadcastChatInfoUpdate(UUID currentUserId,ChatInfoChangedEventModel eventModel) {
        List<Membership> userMemberships = daoManager.getMembershipDAO().findAllByField("account.id", currentUserId);
        for (Membership userMembership : userMemberships) {
            List<Membership> chatPeers = daoManager.getMembershipDAO().findAllByField("chat.id", userMembership.getChat().getId());
            for (Membership peer : chatPeers) {
                if (!peer.getAccount().getId().equals(currentUserId)) {

                    eventModel.setChatId(userMembership.getChat().getId());
                    try {
                        chatInfoChangedEvent.Invoke(
                                getServerSessionManager(),
                                peer.getAccount().getId().toString(),
                                eventModel
                        );
                    } catch (IOException ex) {
                        System.err.println("Failed to send status event to " + peer.getAccount().getId() + ": " + ex.getMessage());
                    }
                }
            }
        }
    }

    private String generateOTP() {
        int otp = 10000 + (int)(Math.random() * 90000);
        return String.valueOf(otp);
    }
    public RpcResponse<Object> getAccountInfo() {
        UUID currentUserId = UUID.fromString(getCurrentUser().getUserId());
        Account account = daoManager.getAccountDAO().findById(currentUserId);
        if (account == null) {
            return NotFound();
        }

        GetAccountInfoOutputModel output = new GetAccountInfoOutputModel();
        output.setId(account.getId());
        output.setFirstName(account.getFirstName());
        output.setLastName(account.getLastName());
        output.setUsername(account.getUsername());
        output.setBio(account.getBio());
        output.setPhoneNumber(account.getPhoneNumber());
        if(account.getProfilePictureId() != null && !account.getProfilePictureId().trim().isEmpty()) {
            Media media = daoManager.getEntityManager().find(Media.class, UUID.fromString(account.getProfilePictureId()));
            if (media != null) {
                output.setProfilePictureMediaId(media.getId().toString());
                output.setProfilePictureFileId(media.getFileId());
            }
        }
        output.setStatus(account.getStatus());

        return Ok(output);
    }

    public RpcResponse<Object> setProfilePicture(SetProfilePictureInputModel model) {
        UUID currentUserId = UUID.fromString(getCurrentUser().getUserId());
        Account account = daoManager.getAccountDAO().findById(currentUserId);
        if (account == null) {
            return NotFound();
        }

        account.setProfilePictureId(model.getProfilePictureMediaId());
        daoManager.getAccountDAO().update(account);
        ChatInfoChangedEventModel eventModel = new ChatInfoChangedEventModel(null, null,model.getProfilePictureFileId());
        broadcastChatInfoUpdate(currentUserId,eventModel);
        return Ok();
    }
    //private String generateAccessToken(account account) {
    //    return UUID.randomUUID().toString();
    //}
}