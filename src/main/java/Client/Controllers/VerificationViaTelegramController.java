package Client.Controllers;

import Client.AccessKeyManager;
import Client.AppConnectionManager;
import Client.RpcCaller;
import JSocket2.Core.Client.ConnectionManager;
import JSocket2.Protocol.Rpc.RpcResponse;
import JSocket2.Protocol.StatusCode;
import Shared.Api.Models.AccountController.RequestCodePhoneNumberInputModel;
import Shared.Api.Models.AccountController.RequestCodePhoneNumberOutputModel;
import Shared.Api.Models.AccountController.VerifyCodeInputModel;
import Shared.Api.Models.AccountController.VerifyCodeOutputModel;
import Shared.Utils.DeviceUtil;
import Shared.Utils.SceneUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

import static Shared.Utils.SceneUtil.changeSceneWithSameSize;

public class VerificationViaTelegramController {
    private RequestCodePhoneNumberOutputModel requestCodePhoneNumberOutputModel;
    private ConnectionManager connectionManager;
    private RpcCaller rpcCaller;
    private TextField[] textFields;

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
    public Button nextButton;
    @FXML
    public Label TelegramValidationMessage;
    @FXML
    public Label phoneLabel;

    public void setRequestCodeOutputModel(RequestCodePhoneNumberOutputModel requestCodePhoneNumberOutputModel) {
        this.requestCodePhoneNumberOutputModel = requestCodePhoneNumberOutputModel;
        phoneLabel.setText(requestCodePhoneNumberOutputModel.getPhoneNumber());
    }

    @FXML
    private void initialize() {
        connectionManager = AppConnectionManager.getInstance().getConnectionManager();
        rpcCaller = AppConnectionManager.getInstance().getRpcCaller();
        textFields = new TextField[]{code1, code2, code3, code4, code5};

        playIntroAnimations();
        setupInputListeners();

        code1.requestFocus();
    }

    private void playIntroAnimations() {
        if (infoBox != null) {
            infoBox.setTranslateX(75);
            var transition = new TranslateTransition(Duration.seconds(0.5), infoBox);
            transition.setToX(0);
            transition.play();
        }
        if (nextButton != null) {
            nextButton.setTranslateY(50);
            var transition = new TranslateTransition(Duration.seconds(0.5), nextButton);
            transition.setToY(0);
            transition.play();
        }
    }

    private void setupInputListeners() {
        for (int i = 0; i < textFields.length; i++) {
            final int index = i;
            TextField currentField = textFields[index];

            currentField.setOnKeyTyped(event -> {
                TelegramValidationMessage.setText("");
                String text = currentField.getText();

                if (text.length() == 1 && text.matches("\\d")) {
                    animateTextRise(currentField);
                    if (index < textFields.length - 1) {
                        textFields[index + 1].requestFocus();
                    } else {
                        handleNext();
                    }
                } else if (!text.isEmpty()){
                    shakeField(currentField);
                    javafx.application.Platform.runLater(currentField::clear);
                }
            });

            currentField.setOnKeyPressed(event -> {
                if (event.isControlDown() && event.getCode() == KeyCode.V) {
                    String pastedText = Clipboard.getSystemClipboard().getString();
                    if (pastedText != null) {
                        String digits = pastedText.replaceAll("[^\\d]", "");
                        if (digits.length() >= 5) {
                            digits = digits.substring(0, 5);
                            for (int j = 0; j < textFields.length; j++) {
                                textFields[j].setText(String.valueOf(digits.charAt(j)));
                            }
                            textFields[4].requestFocus();
                            handleNext();
                            event.consume();
                        }
                    }
                    return;
                }

                switch (event.getCode()) {
                    case LEFT:
                        if (index > 0) {
                            textFields[index - 1].requestFocus();
                        }
                        break;
                    case RIGHT:
                        if (index < textFields.length - 1) {
                            textFields[index + 1].requestFocus();
                        }
                        break;
                    case BACK_SPACE:
                        if (currentField.getText().isEmpty() && index > 0) {
                            textFields[index - 1].requestFocus();
                            textFields[index - 1].clear();
                        }
                        break;
                    default:
                        break;
                }
            });
        }
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
        field.setTranslateY(5);
        TranslateTransition transition = new TranslateTransition(Duration.millis(150), field);
        transition.setToY(0);
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
    private void handleNext() {
        if (nextButton.isDisabled()) return;

        String code = code1.getText() + code2.getText() + code3.getText() + code4.getText() + code5.getText();
        if (code.length() == 5 && code.matches("\\d{5}")) {
            setLoadingState(true);

            Task<RpcResponse<VerifyCodeOutputModel>> otpTask = new Task<>() {
                @Override
                protected RpcResponse<VerifyCodeOutputModel> call() throws IOException {
                    var deviceInfo = DeviceUtil.getDeviceInfo();
                    return rpcCaller.verifyOTP(new VerifyCodeInputModel(requestCodePhoneNumberOutputModel.getPendingId(), requestCodePhoneNumberOutputModel.getPhoneNumber(), code, deviceInfo));
                }
            };

            otpTask.setOnSucceeded(event -> {
                var response = otpTask.getValue();
                if (response.getStatusCode() == StatusCode.OK) {
                    try {
                        handleSuccessfulVerification(response.getPayload());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    handleFailedVerification(response.getMessage());
                }
                setLoadingState(false);
            });

            otpTask.setOnFailed(event -> {
                otpTask.getException().printStackTrace();
                TelegramValidationMessage.setText("An error occurred.");
                setLoadingState(false);
            });

            new Thread(otpTask).start();
        } else {
            TelegramValidationMessage.setText("Please enter a valid 5-digit code.");
        }
    }

    private void handleSuccessfulVerification(VerifyCodeOutputModel payload) throws IOException {
        switch (payload.getStatus()) {
            case "need_register":
                changeSceneWithSameSize(root, "/Client/fxml/UserInfo.fxml", (UserInfoController controller) -> {
                    controller.setPhoneNumber(requestCodePhoneNumberOutputModel.getPhoneNumber());
                });
                break;
            case "need_password":
                changeSceneWithSameSize(root, "/Client/fxml/CloudPasswordCheck.fxml", (CloudPasswordCheckController controller) -> {
                    controller.setPhoneNumber(requestCodePhoneNumberOutputModel.getPhoneNumber());
                });
                break;
            case "logged_in":
                var resultCode = AccessKeyManager.LoginWithAccessKey(payload.getAccessKey(), connectionManager.getClient());
                if (resultCode == StatusCode.OK) {
                    System.out.println("Successful login");
                    changeSceneWithSameSize(root, "/Client/fxml/mainChat.fxml", (MainChatController controller) -> {

                    });
                }
                break;
        }
    }

    private void handleFailedVerification(String message) {
        switch (message) {
            case "otp_expired":
                TelegramValidationMessage.setText("OTP expired. Please request a new one.");
                break;
            case "too_many_attempts_try_later":
                TelegramValidationMessage.setText("Too many attempts. Please try again later.");
                break;
            case "invalid_otp":
                TelegramValidationMessage.setText("Invalid OTP. Please check the code and try again.");
                break;
            default:
                TelegramValidationMessage.setText("Something went wrong. Please try again.");
                break;
        }
        turnFieldsBlank();
    }


    private void turnFieldsBlank() {
        for (TextField field : textFields) {
            field.setText("");
            shakeField(field);
        }
        textFields[0].requestFocus();
    }

    private void setLoadingState(boolean isLoading) {
        nextButton.setDisable(isLoading);
        for(TextField field : textFields){
            field.setDisable(isLoading);
        }
        if (isLoading) {
            nextButton.setText("Verifying...");
        } else {
            nextButton.setText("Next");
        }
    }


    @FXML
    private void handleSmsLinkClick() {
        setFieldsDisabled(true);
        Task<RpcResponse<RequestCodePhoneNumberOutputModel>> smsTask = new Task<>() {
            @Override
            protected RpcResponse<RequestCodePhoneNumberOutputModel> call() throws IOException {
                return rpcCaller.requestOTP(new RequestCodePhoneNumberInputModel(requestCodePhoneNumberOutputModel.getPhoneNumber(), "sms", DeviceUtil.getDeviceInfo()));
            }
        };

        smsTask.setOnSucceeded(event -> {
            var response = smsTask.getValue();
            if (response.getStatusCode() == StatusCode.OK) {
                SceneUtil.changeSceneWithSameSize(code1, "/Client/fxml/VerificationViaSms.fxml", (VerificationViaSmsController controller) -> {
                    controller.setRequestCodeOutputModel(response.getPayload());
                });
            } else {
                TelegramValidationMessage.setText("Failed to send SMS. Please try again.");
                setFieldsDisabled(false);
            }
        });

        smsTask.setOnFailed(event -> {
            smsTask.getException().printStackTrace();
            TelegramValidationMessage.setText("An error occurred while requesting SMS.");
            setFieldsDisabled(false);
        });

        new Thread(smsTask).start();
    }

    private void setFieldsDisabled(boolean disabled) {
        for (TextField field : textFields) {
            field.setDisable(disabled);
        }
    }
}