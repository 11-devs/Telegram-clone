package Client.Controllers;

import Client.AccessKeyManager;
import Client.AppConnectionManager;
import Client.RpcCaller;
import JSocket2.Core.Client.ConnectionManager;
import JSocket2.Protocol.Rpc.RpcResponse;
import JSocket2.Protocol.StatusCode;
import Shared.Api.Models.AccountController.LoginInputModel;
import Shared.Api.Models.AccountController.LoginOutputModel;
import Shared.Api.Models.AccountController.RequestCodeEmailOutputModel;
import Shared.Api.Models.AccountController.RequestCodePhoneNumberInputModel;
import Shared.Utils.DeviceUtil;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import Shared.Utils.AnimationUtil;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static Shared.Utils.SceneUtil.changeSceneWithSameSize;

public class CloudPasswordCheckController {
    private ConnectionManager connectionManager;
    private RpcCaller rpcCaller;

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    private String phoneNumber;

    @FXML
    private VBox root;
    @FXML
    private VBox infoBox;
    @FXML
    private Button submitButton;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label passwordLabel;

    private final PseudoClass errorPseudoClass = PseudoClass.getPseudoClass("error");
    @FXML
    private void initialize() {
        connectionManager = AppConnectionManager.getInstance().getConnectionManager();
        rpcCaller = AppConnectionManager.getInstance().getRpcCaller();
        if (infoBox != null) {
            infoBox.setTranslateX(75);
            var transition = new javafx.animation.TranslateTransition(javafx.util.Duration.seconds(0.5), infoBox);
            transition.setToX(0);
            transition.setAutoReverse(false);
            transition.setCycleCount(1);
            transition.play();
        }
        if (submitButton != null) {
            submitButton.setTranslateY(50);
            var transition = new javafx.animation.TranslateTransition(javafx.util.Duration.seconds(0.5), submitButton);
            transition.setToY(0);
            transition.setAutoReverse(false);
            transition.setCycleCount(1);
            transition.play();
        }
        passwordField.requestFocus();
    }

    @FXML
    private void handleSettings() {

    }

    @FXML
    private void handleBack() {
        changeSceneWithSameSize(root, "/Client/fxml/phoneNumber.fxml");
    }

    @FXML
    private void handleSubmit() {
        String password = passwordField.getText();
        if (password != null && !password.isEmpty()) {
            System.out.println("Cloud password entered: " + password);

            Task<RpcResponse<LoginOutputModel>> loginTask = new Task<RpcResponse<LoginOutputModel>>() {
                @Override
                protected RpcResponse<LoginOutputModel> call() throws Exception {
                    return rpcCaller.login(new LoginInputModel(password,phoneNumber, DeviceUtil.getDeviceInfo()));
                }
            };
            loginTask.setOnSucceeded(event -> {
                try {
                    var response = loginTask.getValue();
                    if(response.getStatusCode() == StatusCode.OK){
                        switch (response.getPayload().getStatus()){
                            case "logged_in":
                                var resultCode = AccessKeyManager.LoginWithAccessKey(response.getPayload().getAccessKey(),connectionManager.getClient());
                                if (resultCode == StatusCode.OK) {
                                    System.out.println("Successful login");
                                    changeSceneWithSameSize(root, "/Client/fxml/mainChat.fxml", (MainChatController controller) -> {

                                    });
                                }
                                break;
                        }
                    }else{
                        switch (response.getMessage()) {
                            case "invalid_password":
                                Platform.runLater(() -> {
                                    passwordField.clear();
                                    AnimationUtil.showErrorAnimation(passwordField,errorPseudoClass);
                                    AnimationUtil.showErrorAnimation(passwordLabel,errorPseudoClass);
                                    System.out.println("Invalid password. Field cleared and bordered in red.");
                                });
                                break;
                        }
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                }
            });
            loginTask.setOnFailed(event -> {
                System.out.println("Task failed.");
                loginTask.getException().printStackTrace();
            });

            new Thread(loginTask).start();
        } else {
            System.out.println("Please enter a password.");
        }
    }


    @FXML
    private void handleForgotPassword() {
        System.out.println("Forgot password link clicked.");
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            changeSceneWithSameSize(root, "/Client/fxml/phoneNumber.fxml");
            return;
        }

        Task<RpcResponse<Object>> resetTask = new Task<>() {
            @Override
            protected RpcResponse<Object> call() throws Exception {
                return rpcCaller.requestPasswordReset(new RequestCodePhoneNumberInputModel(phoneNumber, "email", DeviceUtil.getDeviceInfo(), "password_reset"));
            }
        };

        resetTask.setOnSucceeded(event -> {
            var response = resetTask.getValue();
            if (response.getStatusCode() == StatusCode.OK) {
                Map<String, String> payload = (Map<String, String>) response.getPayload();
                String status = payload.get("status");

                Platform.runLater(() -> {
                    if ("email_code_sent".equals(status)) {
                        RequestCodeEmailOutputModel emailOutputModel = new RequestCodeEmailOutputModel();
                        emailOutputModel.setStatus(status);
                        emailOutputModel.setPendingId(payload.get("pendingId"));
                        emailOutputModel.setEmail(payload.get("email"));

                        changeSceneWithSameSize(root, "/Client/fxml/verificationViaEmail.fxml", (VerificationViaEmailController controller) -> {
                            controller.setRequestCodeEmailOutputModel(emailOutputModel);
                            controller.setPasswordResetMode(true);
                            controller.setPhoneNumber(this.phoneNumber);
                        });
                    } else if ("no_email_setup".equals(status)) {
                        // Directly navigate to ResetAccount.fxml without an intermediate alert
                        Platform.runLater(() -> {
                            changeSceneWithSameSize(root, "/Client/fxml/resetAccount.fxml", (ResetAccountController controller) -> {
                                controller.setPhoneNumber(this.phoneNumber);
                            });
                        });
                    }
                });
            } else {
                System.err.println("Request password reset failed: " + response.getMessage());
                Platform.runLater(() -> {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Password Reset Failed");
                    errorAlert.setContentText("Could not initiate password reset. Please try again later. Reason: " + response.getMessage());
                    errorAlert.showAndWait();
                });
            }
        });

        resetTask.setOnFailed(event -> {
            event.getSource().getException().printStackTrace();
            Platform.runLater(() -> {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Password Reset Failed");
                errorAlert.setContentText("An unexpected error occurred. Please check your connection and try again.");
                errorAlert.showAndWait();
            });
        });

        new Thread(resetTask).start();
    }
}