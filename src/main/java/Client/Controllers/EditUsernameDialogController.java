package Client.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditUsernameDialogController {

    @FXML
    private TextField usernameField;
    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;

    private Stage dialogStage;
    private MyAccountSettingsController parentController;

    @FXML
    private void initialize() {
        System.out.println("EditUsernameDialogController initialized.");
        usernameField.requestFocus(); // Request focus for the username field when the dialog is shown.
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setParentController(Object parentController) {
        if (parentController instanceof MyAccountSettingsController) {
            this.parentController = (MyAccountSettingsController) parentController;
        }
    }

    public void setData(Object data) {
        if (data instanceof String username) {
            usernameField.setText(username);
            usernameField.requestFocus(); // Request focus for immediate input
        }
    }

    @FXML
    private void handleSave() {
        String newUsername = usernameField.getText();
        // Basic validation
        if (newUsername != null && newUsername.length() >= 5 && newUsername.matches("^[a-zA-Z0-9_]+$")) {
            if (parentController != null) {
                parentController.updateUsername(newUsername);
            }
            dialogStage.close();
        } else {
            // TODO: Show validation error in UI
            System.err.println("Invalid username. Must be at least 5 characters and contain only a-z, 0-9, and underscores.");
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
}