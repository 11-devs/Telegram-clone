package Client.Controllers;

import Client.AppConnectionManager;
import Client.RpcCaller;
import JSocket2.Protocol.Rpc.RpcResponse;
import JSocket2.Protocol.StatusCode;
import Shared.Api.Models.AccountController.ResetPasswordInputModel;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import static Shared.Utils.SceneUtil.changeSceneWithSameSize;

public class ResetPasswordController {

    private RpcCaller rpcCaller;
    private String phoneNumber;
    private String pendingId;

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPendingId(String pendingId) {
        this.pendingId = pendingId;
    }

    @FXML
    private VBox root;
    @FXML
    private VBox infoBox;
    @FXML
    private Button submitButton;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label newPasswordLabel;
    @FXML
    private Label confirmPasswordLabel;
    @FXML
    private Label statusLabel;

    private final PseudoClass errorPseudoClass = PseudoClass.getPseudoClass("error");

    @FXML
    private void initialize() {
        rpcCaller = AppConnectionManager.getInstance().getRpcCaller();

        if (infoBox != null) {
            infoBox.setTranslateX(75);
            var transition = new javafx.animation.TranslateTransition(Duration.seconds(0.5), infoBox);
            transition.setToX(0);
            transition.play();
        }
        if (submitButton != null) {
            submitButton.setTranslateY(50);
            var transition = new javafx.animation.TranslateTransition(Duration.seconds(0.5), submitButton);
            transition.setToY(0);
            transition.play();
        }

        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> clearErrorState());
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> clearErrorState());

        newPasswordField.requestFocus();
    }

    @FXML
    private void handleSettings() {
        // TODO: Implement settings dialog logic
        System.out.println("Settings button clicked.");
    }

    @FXML
    private void handleBack() {
        changeSceneWithSameSize(root, "/Client/fxml/CloudPasswordCheck.fxml");
    }

    @FXML
    private void handleSubmit() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            setErrorState("Password fields cannot be empty.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            setErrorState("Passwords do not match.");
            return;
        }

        if (phoneNumber == null || pendingId == null) {
            setErrorState("Error: Missing phone number or pending ID for password reset.");
            return;
        }

        clearErrorState();
        statusLabel.setText("Resetting password...");
        submitButton.setDisable(true);

        Task<RpcResponse<Object>> resetPasswordTask = new Task<>() {
            @Override
            protected RpcResponse<Object> call() throws Exception {
                return rpcCaller.resetPassword(new ResetPasswordInputModel(phoneNumber, pendingId, newPassword));
            }
        };

        resetPasswordTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                var response = resetPasswordTask.getValue();
                if (response.getStatusCode() == StatusCode.OK) {
                    statusLabel.setText("Password reset successfully!");
                    statusLabel.pseudoClassStateChanged(errorPseudoClass, false);
                    System.out.println("Password reset successfully.");
                    // Navigate to CloudPasswordCheck.fxml after a short delay or immediately
                    // For now, immediately:
                    changeSceneWithSameSize(root, "/Client/fxml/CloudPasswordCheck.fxml", (CloudPasswordCheckController controller) -> {
                        controller.setPhoneNumber(phoneNumber);
                    });
                } else {
                    setErrorState("Failed to reset password: " + response.getMessage());
                }
                submitButton.setDisable(false);
            });
        });

        resetPasswordTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                statusLabel.setText("Error resetting password: " + resetPasswordTask.getException().getMessage());
                statusLabel.pseudoClassStateChanged(errorPseudoClass, true);
                resetPasswordTask.getException().printStackTrace();
                submitButton.setDisable(false);
            });
        });

        new Thread(resetPasswordTask).start();
    }

    private void setErrorState(String message) {
        newPasswordField.clear();
        confirmPasswordField.clear();
        newPasswordField.requestFocus();
        statusLabel.setText(message);
        newPasswordField.pseudoClassStateChanged(errorPseudoClass, true);
        confirmPasswordField.pseudoClassStateChanged(errorPseudoClass, true);
        newPasswordLabel.pseudoClassStateChanged(errorPseudoClass, true);
        confirmPasswordLabel.pseudoClassStateChanged(errorPseudoClass, true);
        statusLabel.pseudoClassStateChanged(errorPseudoClass, true);
        shakeField(newPasswordField);
        shakeField(confirmPasswordField);
    }

    private void clearErrorState() {
        statusLabel.setText("");
        newPasswordField.pseudoClassStateChanged(errorPseudoClass, false);
        confirmPasswordField.pseudoClassStateChanged(errorPseudoClass, false);
        newPasswordLabel.pseudoClassStateChanged(errorPseudoClass, false);
        confirmPasswordLabel.pseudoClassStateChanged(errorPseudoClass, false);
        statusLabel.pseudoClassStateChanged(errorPseudoClass, false);
    }

    private void shakeField(PasswordField field) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(0), e -> field.setTranslateX(0)),
                new KeyFrame(Duration.millis(50), e -> field.setTranslateX(-5)),
                new KeyFrame(Duration.millis(100), e -> field.setTranslateX(5)),
                new KeyFrame(Duration.millis(150), e -> field.setTranslateX(-5)),
                new KeyFrame(Duration.millis(200), e -> field.setTranslateX(5)),
                new KeyFrame(Duration.millis(250), e -> field.setTranslateX(0))
        );
        timeline.play();
    }
}
