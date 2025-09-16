package Client.Controllers;

import Client.RpcCaller;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EditUsernameDialogController {

    @FXML
    public VBox dialogRoot;
    @FXML
    private TextField usernameField;
    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;
    @FXML
    private Label usernameLabel; // New label for "Username" title
    @FXML
    private Label errorLabel; // New label for displaying validation errors

    private Stage dialogStage;
    private MyAccountSettingsController parentController;
    private RpcCaller rpcCaller;
    @FXML
    private void initialize() {
        System.out.println("EditUsernameDialogController initialized.");
        // Add focus listener to the username field to highlight the label
        usernameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            updateLabelFocus(usernameLabel, newVal);
        });

        // Add a listener to clear the error message when the user starts typing
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearError();
        });

        usernameField.requestFocus(); // Request focus for the username field when the dialog is shown.
    }

    private void updateLabelFocus(Label label, boolean focused) {
        if (focused) {
            label.getStyleClass().remove("field-label-dialog");
            label.getStyleClass().add("field-label-dialog-active");
        } else {
            label.getStyleClass().remove("field-label-dialog-active");
            label.getStyleClass().add("field-label-dialog");
        }
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
            // Move cursor to the end of the text
            usernameField.positionCaret(username.length());
            usernameField.requestFocus();
        }
    }

    @FXML
    private void handleSave() {
        String newUsername = usernameField.getText().trim();

        if (validateUsername(newUsername)) {
            if (parentController != null) {
                parentController.updateUsername(newUsername);
            }
            dialogStage.close();
        }
    }

    // TODO: check for duplication
    private boolean validateUsername(String username) {
        if (username.isEmpty()) {
            showError("Username cannot be empty.");
            return false;
        }
        if (username.length() < 5) {
            showError("Username must be at least 5 characters long.");
            return false;
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            showError("Only letters (a-z), numbers (0-9), and underscores (_) are allowed.");
            return false;
        }
        if (Character.isDigit(username.charAt(0))) {
            showError("Username cannot start with a number.");
            return false;
        }
        clearError();
        return true;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        // Add error style to the text field's underline
        if (!usernameField.getStyleClass().contains("error")) {
            usernameField.getStyleClass().add("error");
        }
    }

    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        // Remove error style from the text field's underline
        usernameField.getStyleClass().remove("error");
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
}