package Client.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SpeakersCameraSettingsController {

    private SettingsController parentController;
    private Stage dialogStage; // Reference to the actual dialog stage
    @FXML
    private Label sectionTitleLabel;
    @FXML
    private VBox root;

    public void setParentController(SettingsController parentController) {
        this.parentController = parentController;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setSectionTitle(String title) {
        if (sectionTitleLabel != null) {
            sectionTitleLabel.setText(title);
        }
    }

    @FXML
    private void handleBack() {
        if (parentController != null) {
            parentController.goBackToMainSettings();
        }
    }
    // Other specific handlers for Speakers and Camera
}