package Client.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;

import static Shared.Utils.SceneUtil.changeSceneWithSameSize;

public class CloudPasswordCheckController {
    @FXML
    private VBox root;
    @FXML
    private VBox infoBox;
    @FXML
    private Button submitButton;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void initialize() {
        // Animation of moving from a little further right to the main target
        if (infoBox != null) {
            infoBox.setTranslateX(75);
            var transition = new javafx.animation.TranslateTransition(javafx.util.Duration.seconds(0.5), infoBox);
            transition.setToX(0);
            transition.setAutoReverse(false);
            transition.setCycleCount(1);
            transition.play();
        }
        // Animation of moving from a little further lower to the main target
        if (submitButton != null) {
            submitButton.setTranslateY(75);
            var transition = new javafx.animation.TranslateTransition(javafx.util.Duration.seconds(0.5), submitButton);
            transition.setToY(0);
            transition.setAutoReverse(false);
            transition.setCycleCount(1);
            transition.play();
        }
        passwordField.requestFocus(); // Initial focus on password field
    }

    @FXML
    private void handleSubmit() {
        String password = passwordField.getText();
        if (password != null && !password.isEmpty()) {
            System.out.println("Cloud password entered: " + password);
            // TODO: Add password validation logic
            // Example: changeSceneWithSameSize(root, "Client/fxml/nextScene.fxml");
        } else {
            System.out.println("Please enter a password.");
        }
    }

    @FXML
    private void handleForgotPassword() {
        // TODO: Implement forgot password logic
        System.out.println("Forgot password link clicked.");
        // Example: changeSceneWithSameSize(root, "Client/fxml/forgotPassword.fxml");
    }
}