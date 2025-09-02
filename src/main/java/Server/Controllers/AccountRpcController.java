package Server.Controllers;

import JSocket2.Protocol.Rpc.RpcControllerBase;
import JSocket2.Protocol.Rpc.RpcResponse;
import Server.DaoManager;
import Shared.Api.Models.AccountController.*;
import Shared.Models.Account.Account;
import Shared.Models.PendingAuth.PendingAuth;
import Shared.Utils.PasswordUtil;

import java.time.Instant;
import java.util.UUID;

public class AccountRpcController extends RpcControllerBase {
    private final DaoManager daoManager;

    public AccountRpcController(DaoManager daoManager) {
        this.daoManager = daoManager;
    }

    public RpcResponse<RequestCodeOutputModel> requestOTP(String phoneNumber) {
        //var account = daoManager.getAccountDAO().findByField("phoneNumber",phoneNumber);

        PendingAuth pending = new PendingAuth();
        var otp = generateOTP();
        pending.setId(UUID.randomUUID());
        pending.setPhoneNumber(phoneNumber);
        pending.setHashedOtp(PasswordUtil.hash(otp));
        pending.setOtpExpiresAt(Instant.now().plusSeconds(120));
        pending.setAttempts(0);
        pending.setStage("initial");

        daoManager.getPendingAuthDAO().insert(pending);

        // Send OTP to phoneNumber via SMS
        System.out.println("phoneNumber:"+phoneNumber + " OTP:"+otp);

        RequestCodeOutputModel model = new RequestCodeOutputModel();
        model.setStatus("code_sent");
        model.setPendingId(pending.getId().toString());

        return Ok(model);
    }

    public RpcResponse<Object> verifyOTP(VerifyCodeInputModel model) {
        PendingAuth pending = daoManager.getPendingAuthDAO()
                .findById(UUID.fromString(model.getPendingId()));

        if (pending == null || Instant.now().isAfter(pending.getOtpExpiresAt())) {
            return BadRequest("otp_expired");
        }
        if (!PasswordUtil.verify(model.getOtp(),pending.getHashedOtp())) {
            pending.setAttempts(pending.getAttempts() + 1);
            daoManager.getPendingAuthDAO().update(pending);

            if (pending.getAttempts() >= 5) {
                return BadRequest("too_many_attempts");
            }

            return BadRequest("invalid_otp");
        }
        daoManager.getPendingAuthDAO().delete(pending);
        var account = daoManager.getAccountDAO().findByField("phoneNumber",model.getPhoneNumber());
        VerifyCodeOutputModel output = new VerifyCodeOutputModel();
        if (account == null) {
            output.setStatus("need_register");
        } else if (!account.getHashedPassword().isEmpty()) {
            output.setStatus("need_password");
        } else {
            var accessKey = generateAccessKey(account);
            output.setStatus("logged_in");
            output.setAccessKey(accessKey);
        }
        return Ok(output);
    }

    public RpcResponse<Object> login(LoginInputModel model) {

        var account = daoManager.getAccountDAO().findByField("phoneNumber",model.getPhoneNumber());
        if (account == null) return BadRequest("account_not_found");

        if (!PasswordUtil.verify(model.getPassword(), account.getHashedPassword())) {
            return BadRequest("invalid_password");
        }

        var accessToken = generateAccessKey(account);

        LoginOutputModel output = new LoginOutputModel();
        output.setStatus("logged_in");
        output.setAccessKey(accessToken);

        return Ok(output);
    }

    private String generateAccessKey(Account account) {
        String accessKey = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        // Save to Table
        return accessKey;
    }
    public RpcResponse<Object> basicRegister(BasicRegisterInputModel model){
        var account = new Account();
        account.setFirstName(model.getFirstName());
        account.setLastName(model.getLastName());
        account.setProfilePictureId(model.getProfilePictureId());
        daoManager.getAccountDAO().insert(account);
        String accessKey = generateAccessKey(account);
        var output = new BasicRegisterOutputModel(accessKey);
        return Ok(output);
    }
    public RpcResponse<Object> setPassword(String password){
        var account = daoManager.getAccountDAO().findById(getCurrentUser().getUserId());
        account.setHashedPassword(PasswordUtil.hash(password));
        daoManager.getAccountDAO().update(account);
        return Ok();
    }
    public RpcResponse<Object> setUsername(String username){
        var account = daoManager.getAccountDAO().findById(getCurrentUser().getUserId());
        account.setUsername(username);
        daoManager.getAccountDAO().update(account);
        return Ok();
    }
    public RpcResponse<Object> setBio(String bio){
        var account = daoManager.getAccountDAO().findById(getCurrentUser().getUserId());
        account.setBio(bio);
        daoManager.getAccountDAO().update(account);
        return Ok();
    }
    private String generateOTP() {
        int otp = 100000 + (int)(Math.random() * 900000);
        return String.valueOf(otp);
    }

    //private String generateAccessToken(account account) {
    //    return UUID.randomUUID().toString();
    //}
}
