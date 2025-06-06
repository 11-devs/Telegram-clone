package Client.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class PhoneNumberController {
    @FXML
    private VBox root;

    @FXML
    private ComboBox<String> countryCode;

    @FXML
    private TextField phoneNumberField;

    // List of country codes (as an example)
    public ObservableList<String> countryCodes = FXCollections.observableArrayList(
            "+98 (Iran)", "+1 (USA)", "+44 (UK)", "+91 (India)"
    );

    public void initialize() {
        System.out.println("Initializing PhoneNumberController...");
        if (countryCode != null) {
            countryCode.setItems(countryCodes);
            countryCode.setValue("+98 (Iran)"); // Default code
            System.out.println("Country codes set successfully.");
        } else {
            System.out.println("countryCode is null! Check FXML binding.");
        }
    }

    @FXML
    private void continueAction() {
        String phoneNumber = phoneNumberField.getText();
        String selectedCountryCode = countryCode.getValue();

        if (phoneNumber.isEmpty()) {
            System.out.println("Phone number is empty!");
            return;
        }

        System.out.println("Phone Number: " + selectedCountryCode + " " + phoneNumber);
        // go to the next scene
    }
}