package Client.Controllers;

import Client.AccessKeyManager;
import Client.AppConnectionManager;
import Client.RpcCaller;
import JSocket2.Core.Client.ConnectionManager;
import JSocket2.Protocol.Rpc.RpcResponse;
import JSocket2.Protocol.StatusCode;
import Shared.Api.Models.AccountController.RequestCodeEmailOutputModel;
import Shared.Api.Models.AccountController.VerifyCodeEmailInputModel;
import Shared.Api.Models.AccountController.VerifyCodeInputModel;
import Shared.Api.Models.AccountController.VerifyCodeOutputModel;
import Shared.Utils.DeviceUtil;
import Shared.Utils.SceneUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static Shared.Utils.SceneUtil.changeSceneWithSameSize;

public class VerificationViaEmailController {
    private ConnectionManager connectionManager;
    private RpcCaller rpcCaller;
    private RequestCodeEmailOutputModel requestCodeEmailOutputModel;
    private boolean isPasswordResetMode = false;
    private String phoneNumber;

    @FXML
    private VBox root;
    @FXML
    private VBox infoBox;

    @FXML
    private TextField code1;
    @FXML
    private TextField code2;
    @FXML
    private TextField code3;
    @FXML
    private TextField code4;
    @FXML
    private TextField code5;

    @FXML
    private Label emailLabel;

    @FXML
    public Button nextButton;

    public void setPasswordResetMode(boolean passwordResetMode) {
        isPasswordResetMode = passwordResetMode;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @FXML
    private void initialize() {

        connectionManager = AppConnectionManager.getInstance().getConnectionManager();
        rpcCaller = AppConnectionManager.getInstance().getRpcCaller();
        // Animation of moving from a little further right to the main target
        if (infoBox != null) {
            infoBox.setTranslateX(75);
            TranslateTransition transition = new TranslateTransition(Duration.seconds(0.5), infoBox);
            transition.setToX(0);
            transition.setAutoReverse(false);
            transition.setCycleCount(1);
            transition.play();
        }
        // Animation of moving from a little further lower to the main target
        if (nextButton != null) {
            nextButton.setTranslateY(50);
            TranslateTransition transition = new TranslateTransition(Duration.seconds(0.5), nextButton);
            transition.setToY(0);
            transition.setAutoReverse(false);
            transition.setCycleCount(1);
            transition.play();
        }

        // Automatic switching between boxes with the press of a key
        TextField[] fields = {code1, code2, code3, code4, code5};
        for (int i = 0; i < fields.length; i++) {
            final int index = i;
            fields[i].setOnKeyTyped(event -> {
                String input = fields[index].getText();
                if (input.length() == 1) {
                    // Only one numeric character is allowed.
                    if (!input.matches("[0-9]")) {
                        // Left and right scrolling animation to indicate error (only on TextField)
                        shakeField(fields[index]);
                        // Clears non-numeric input.
                        fields[index].setText("");
                    } else {
                        // Animation of moving the number from bottom to center
                        animateTextRise(fields[index]);
                        if (index < 4) {
                            fields[index + 1].requestFocus();
                        }
                    }
                } else if (input.length() > 1) {
                    // If more than one character is entered, keep only the first character.
                    fields[index].setText(input.substring(0, 1));
                    if (index < 4 && fields[index].getText().matches("[0-9]")) {
                        animateTextRise(fields[index]);
                        fields[index + 1].requestFocus();
                    }
                }
            });
            // Add navigation with keyboard arrow keys
            fields[i].setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.RIGHT) {
                    if (index == 4) {
                        fields[0].requestFocus(); // From last to first
                    } else {
                        fields[index + 1].requestFocus();
                    }
                    simulateDownKey(index == 4 ? 0 : index + 1); // Sending the correct index
                } else if (event.getCode() == KeyCode.LEFT) {
                    if (index == 0) {
                        fields[4].requestFocus(); // From first to last
                    } else {
                        fields[index - 1].requestFocus();
                    }
                    simulateDownKey(index == 0 ? 4 : index - 1); // Sending the correct index
                }
            });
        }
        code1.requestFocus(); // Initial focus on the first box
    }

    // Method to simulate pressing the down arrow key to correct the position of the caret (typing bar)
    private void simulateDownKey(int index) {
        TextField[] fields = {code1, code2, code3, code4, code5};
        KeyEvent keyDownPressed = new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "",
                "",
                KeyCode.DOWN,
                false,
                false,
                false,
                false
        );
        fields[index].fireEvent(keyDownPressed);
    }

    private void shakeField(TextField field) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(0), event -> field.setTranslateX(0)),
                new KeyFrame(Duration.millis(40), event -> field.setTranslateX(-3)),
                new KeyFrame(Duration.millis(80), event -> field.setTranslateX(3)),
                new KeyFrame(Duration.millis(120), event -> field.setTranslateX(-2)),
                new KeyFrame(Duration.millis(160), event -> field.setTranslateX(2)),
                new KeyFrame(Duration.millis(200), event -> field.setTranslateX(0))
        );
        timeline.setCycleCount(1);
        timeline.play();
    }

    private void animateTextRise(TextField field) {
        // Adjust the initial text position (a little lower, e.g. 5 pixels for smoother movement)
        field.setTranslateY(5);
        TranslateTransition transition = new TranslateTransition(Duration.millis(150), field);
        transition.setToY(0);
        transition.setCycleCount(1);
        transition.setAutoReverse(false);
        transition.play();
    }

    @FXML
    private void handleSettings() {
        // TODO: develop setting dialog
    }

    @FXML
    private void handleBack() {
        changeSceneWithSameSize(root, "/Client/fxml/phoneNumber.fxml");
    }

    @FXML
    private void handleNoEmailAccess() {
        System.out.println("No email access link clicked.");
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            // This should not happen if the phone number is passed correctly
            System.err.println("Phone number is not available for account reset.");
            changeSceneWithSameSize(root, "/Client/fxml/phoneNumber.fxml");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("No Email Access");
        alert.setHeaderText("You are about to reset your account.");
        alert.setContentText("Resetting your account will delete all your cloud data. This action is irreversible. Do you want to proceed?");

        ButtonType buttonTypeReset = new ButtonType("Reset Account");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeReset, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonTypeReset) {
            // Navigate to ResetAccount.fxml instead of performing the reset directly
            Platform.runLater(() -> {
                changeSceneWithSameSize(root, "/Client/fxml/resetAccount.fxml", (ResetAccountController controller) -> {
                    controller.setPhoneNumber(this.phoneNumber);
                });
            });
        }
    }

    @FXML
    private void handleNext() {
        String code = code1.getText() + code2.getText() + code3.getText() + code4.getText() + code5.getText();
        if (code.length() == 5 && code.matches("[0-9]{5}")) {
            System.out.println("Verification code entered: " + code);
            if (isPasswordResetMode) {
                Task<RpcResponse<VerifyCodeOutputModel>> passwordResetOtpTask = new Task<>() {
                    @Override
                    protected RpcResponse<VerifyCodeOutputModel> call() throws Exception {
                        return rpcCaller.verifyPasswordResetEmailOtp(new VerifyCodeEmailInputModel(requestCodeEmailOutputModel.getPendingId(), requestCodeEmailOutputModel.getEmail(), code));
                    }
                };
                passwordResetOtpTask.setOnSucceeded(event -> {
                    var response = passwordResetOtpTask.getValue();
                    if (response.getStatusCode() == StatusCode.OK) {
                        var payload = response.getPayload();
                        if ("password_reset_required".equals(payload.getStatus())) {
                            Platform.runLater(() -> {
                                changeSceneWithSameSize(root, "/Client/fxml/resetPassword.fxml", (ResetPasswordController controller) -> {
                                    controller.setPhoneNumber(payload.getPhoneNumber());
                                    controller.setPendingId(payload.getPendingId());
                                });
                            });
                        }
                    } else {
                        Platform.runLater(this::turnFieldsBlank);
                    }
                });
                passwordResetOtpTask.setOnFailed(event -> {
                    passwordResetOtpTask.getException().printStackTrace();
                    Platform.runLater(this::turnFieldsBlank);
                });
                new Thread(passwordResetOtpTask).start();
            } else {
                Task<RpcResponse<Object>> otpTask = new Task<>() {
                    @Override
                    protected RpcResponse<Object> call() throws Exception {
                        return rpcCaller.verifyEmailOtp(new VerifyCodeEmailInputModel(requestCodeEmailOutputModel.getPendingId(), requestCodeEmailOutputModel.getEmail(), code));
                    }
                };
                otpTask.setOnSucceeded(event -> {
                    try {
                        var response = otpTask.getValue();
                        if (response.getStatusCode() == StatusCode.OK) {
                            System.out.println("Email Confirmed");
                        } else if (response.getStatusCode() == StatusCode.BAD_REQUEST) {
                            turnFieldsBlank();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                otpTask.setOnFailed(event -> {
                    System.out.println("Task failed.");
                    otpTask.getException().printStackTrace();
                });
                new Thread(otpTask).start();
            }
        } else {
            System.out.println("Please enter a 5-digit code.");
        }
    }
    private void turnFieldsBlank() {
        TextField[] fields = {code1, code2, code3, code4, code5};
        for (TextField field : fields) {
            field.setText("");
            shakeField(field);
        }
    }

    public void setRequestCodeEmailOutputModel(RequestCodeEmailOutputModel requestCodeEmailOutputModel) {
        this.requestCodeEmailOutputModel = requestCodeEmailOutputModel;
        if(requestCodeEmailOutputModel != null && requestCodeEmailOutputModel.getEmail() != null) {
            Platform.runLater(() -> emailLabel.setText(maskEmail(requestCodeEmailOutputModel.getEmail())));
        }
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "your recovery email";
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 2) { // e.g., a@b.c
            return email.charAt(0) + "***" + email.substring(atIndex);
        }
        return email.substring(0, 2) + "***" + email.substring(atIndex - 2);
    }

//    @FXML
//    private void handleSmsLinkClick() {
//        // Switch to SMS login page
//        SceneUtil.changeSceneWithSameSize(code1, "/Client/fxml/VerificationViaSms.fxml");
//    }

}