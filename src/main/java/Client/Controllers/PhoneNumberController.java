package Client.Controllers;

import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.converter.NumberStringConverter;

import static Shared.Utils.SceneUtil.changeSceneWithSameSize;

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

        // Animation of moving from a little further right to the main target
        if (root != null) {
            root.setTranslateX(75);
            TranslateTransition transition = new TranslateTransition(Duration.seconds(0.5), root);
            transition.setToX(0);
            transition.setAutoReverse(false);
            transition.setCycleCount(1);
            transition.play();
        }

        System.out.println("Initializing PhoneNumberController...");
        if (countryCode != null) {
            countryCode.setItems(countryCodes);
            countryCode.setValue("+98 (Iran)"); // Default code
            System.out.println("Country codes set successfully.");
        } else {
            System.out.println("countryCode is null! Check FXML binding.");
        }
        // Limit input to numbers and a maximum of 12 digits (for local numbers)
        phoneNumberField.setTextFormatter(new TextFormatter<>(new NumberStringConverter(), null, change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*") && newText.length() <= 12) { // Maximum 12 digits
                return change;
            }
            return null; // Reject non-numeric changes or more than 12 digits
        }));
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
        // TODO:
        // go to the next scene
        // Set condition
        changeSceneWithSameSize(root, "/Client/fxml/verificationViaTelegram.fxml");
        // else (for new account)
        // changeSceneWithSameSize(root, "/Client/fxml/verificationViaSms.fxml");
    }
}