package Client.Controllers;

import Shared.Utils.SceneUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import static Shared.Utils.SceneUtil.changeSceneWithSameSize;

public class VerificationViaEmailController {
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
    private void initialize() {

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
    private void handleNext() {
        String code = code1.getText() + code2.getText() + code3.getText() + code4.getText() + code5.getText();
        if (code.length() == 5 && code.matches("[0-9]{5}")) {
            System.out.println("Verification code entered: " + code);
            // TODO:
            // Check conditions...
            // Next scene
            // SceneUtil.changeSceneWithSameSize(code1, "Client/fxml/---.fxml");
        } else {
            System.out.println("Please enter a 5-digit code.");
        }
    }

//    @FXML
//    private void handleSmsLinkClick() {
//        // Switch to SMS login page
//        SceneUtil.changeSceneWithSameSize(code1, "/Client/fxml/VerificationViaSms.fxml");
//    }

}