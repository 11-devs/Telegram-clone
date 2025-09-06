package Client.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class NotificationDialogController {
    @FXML
    private VBox messageContainer;
    @FXML
    private Text messageText;
    @FXML
    private Button okButton;
    @FXML
    private VBox rootVBox; // Reference to the root VBox

    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setData(Object data) {
        if (data instanceof String message) {
            messageText.setText(message); // Use the input message directly

            // Adjust height dynamically after layout is fully computed
            Platform.runLater(() -> {
                messageText.applyCss(); // Apply styles to ensure layout is ready
                double textHeight = messageText.getLayoutBounds().getHeight(); // Get computed height
                if (textHeight <= 0) {
                    // Fallback: estimate height based on number of lines
                    String[] lines = message.split("\n");
                    textHeight = 20 * lines.length; // Approx 20px per line
                }
                double marginTotal = 20.0; // top (20) margin, bottom removed
                double hboxHeight = 41.0; // Button height (30) + margin (10)
                double newVBoxHeight = textHeight + marginTotal + hboxHeight;
                rootVBox.setPrefHeight(newVBoxHeight);
                if (dialogStage != null) {
                    dialogStage.sizeToScene(); // Resize the stage to fit the new content
                }

                // Debug: Print the displayed message to verify
                String debugMessage = message.length() >= 100 ? message.substring(0, 100) + "..." : message;
                System.out.println("Displayed message: " + debugMessage);
                System.out.println("Computed height: " + textHeight);
            });
        }
    }
    public void setData(Object data) {
        if (data instanceof String) {
            setMessage((String) data);
        } else if (data != null) {
            // Fallback for non-string data
            setMessage(data.toString());
        }
    }
    @FXML
    private void handleOk() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}