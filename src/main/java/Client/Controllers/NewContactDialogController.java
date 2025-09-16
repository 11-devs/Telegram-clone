package Client.Controllers;

import Shared.Utils.DialogUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NewContactDialogController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField countryPreCode;
    @FXML private TextField phoneNumberField;
    @FXML private Label errorMessageLabel;
    @FXML private Button createButton;

    private Stage dialogStage;
    private ContactsSectionController parentController;
    private final BooleanProperty wasSuccessful = new SimpleBooleanProperty(false);

    @FXML
    private void initialize() {
        // Add listeners to clear error on typing
        firstNameField.textProperty().addListener((obs, old, aNew) -> clearError());
        phoneNumberField.textProperty().addListener((obs, old, aNew) -> clearError());
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setParentController(Object parentController) {
        if (parentController instanceof ContactsSectionController) {
            this.parentController = (ContactsSectionController) parentController;
        }
    }

    // This method is required by SceneUtil but not used for this dialog.
    public void setData(Object data) {}

    public boolean wasSuccessful() {
        return wasSuccessful.get();
    }

    public BooleanProperty wasSuccessfulProperty() {
        return wasSuccessful;
    }

    @FXML
    private void handleCreate() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phone = phoneNumberField.getText().replaceAll("[^\\d]", "");
        String preCode = countryPreCode.getText().replaceAll("[^\\d]", "");

        if (firstName.isEmpty()) {
            showError("First name cannot be empty.");
            return;
        }
        if (phone.isEmpty() || preCode.isEmpty()) {
            showError("Phone number cannot be empty.");
            return;
        }
        String fullPhoneNumber = "+" + preCode + phone;

        if (parentController != null) {
            parentController.addContact(firstName, lastName, fullPhoneNumber);
            wasSuccessful.set(true); // Assuming success for now. Real app would wait for server response.
        }
        dialogStage.close();
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
    
    // A simplified method to show the country selection dialog
    @FXML
    private void showCountrySelection() {
        System.out.println("Country selection clicked. Implement if needed.");
        // You can reuse the logic from PhoneNumberController to show the country selection dialog
    }

    private void showError(String message) {
        errorMessageLabel.setText(message);
        errorMessageLabel.setVisible(true);
        errorMessageLabel.setManaged(true);
    }

    private void clearError() {
        errorMessageLabel.setVisible(false);
        errorMessageLabel.setManaged(false);
    }
}