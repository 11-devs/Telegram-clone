package Client.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditNameDialogController {

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;
    @FXML
    private Label firstNameLabel;
    @FXML
    private Label lastNameLabel;

    private Stage dialogStage;
    private MyAccountSettingsController parentController;

    @FXML
    private void initialize() {
        System.out.println("EditNameDialogController initialized.");
        firstNameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            updateLabelFocus(firstNameLabel, newVal);
        });
        lastNameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            updateLabelFocus(lastNameLabel, newVal);
        });
        firstNameField.requestFocus(); // Request focus for the first name field when the dialog is shown.
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
        if (data instanceof String[] names && names.length == 2) {
            firstNameField.setText(names[0]);
            lastNameField.setText(names[1]);
            firstNameField.requestFocus(); // Ensure focus after setting data
        }
    }

    @FXML
    private void handleSave() {
        String newFirstName = firstNameField.getText();
        String newLastName = lastNameField.getText();
        if (parentController != null) {
            parentController.updateName(newFirstName, newLastName);
        }
        dialogStage.close();
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
}