package Client.Controllers;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import static Shared.Utils.SceneUtil.changeSceneWithSameSize;

public class ResetPasswordController {

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
        // Apply entrance animations similar to other controllers
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

        // Add listeners to clear error state on text change
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
        // Navigate back to the appropriate previous screen, e.g., login or phone number
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

        clearErrorState();
        statusLabel.setText("Password reset successfully!");
        statusLabel.pseudoClassStateChanged(errorPseudoClass, false); // In case you want a success style later

        System.out.println("Password reset successfully. New Password: " + newPassword);

        // TODO: Call an RPC method to update the password on the server.
        // For example:
        // rpcCaller.resetPassword(newPassword);
        // Then navigate to the main application window upon success.
    }

    private void setErrorState(String message) {
        newPasswordField.clear();
        confirmPasswordField.clear();
        newPasswordField.requestFocus();
        // Apply error pseudo-class for CSS styling
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