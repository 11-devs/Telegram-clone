package Client.Controllers;

import Shared.Utils.DialogUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Objects;

public class SettingsController {

    @FXML
    private ImageView profilePictureImage;
    @FXML
    private Label nameLabel;
    @FXML
    private Label phoneNumberLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Button closeButton;

    private Stage dialogStage;
    private Object parentController;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setParentController(Object parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void initialize() {
        // Load user data and set it to the labels
        // This is placeholder data for now
        nameLabel.setText("My Account");
        phoneNumberLabel.setText("+98 912 345 6789");
        usernameLabel.setText("@me");
        try {
            Image profileImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Client/images/11Devs-white.png")));
            profilePictureImage.setImage(profileImage);
        } catch (Exception e) {
            System.out.println("Could not load profile picture: " + e.getMessage());
        }
    }

    @FXML
    private void handleClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    @FXML
    private void handleProfilePictureClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(dialogStage);
        if (selectedFile != null) {
            Image newImage = new Image(selectedFile.toURI().toString());
            profilePictureImage.setImage(newImage);
            // Here you would add logic to upload the image to the server
        }
    }

    @FXML
    private void handleMyAccount() {
        DialogUtil.showNotificationDialog(dialogStage, "My Account section will be implemented in a future version.");
    }

    // Add handlers for other buttons similarly...
    @FXML
    private void handleNotifications() {
        DialogUtil.showNotificationDialog(dialogStage, "Notifications and Sounds section will be implemented in a future version.");
    }

    @FXML
    private void handlePrivacySecurity() {
        DialogUtil.showNotificationDialog(dialogStage, "Privacy and Security section will be implemented in a future version.");
    }

    @FXML
    private void handleChatSettings() {
        DialogUtil.showNotificationDialog(dialogStage, "Chat Settings section will be implemented in a future version.");
    }

    @FXML
    private void handleFolders() {
        DialogUtil.showNotificationDialog(dialogStage, "Folders section will be implemented in a future version.");
    }

    @FXML
    private void handleAdvanced() {
        DialogUtil.showNotificationDialog(dialogStage, "Advanced settings will be implemented in a future version.");
    }

    @FXML
    private void handleSpeakersCamera() {
        DialogUtil.showNotificationDialog(dialogStage, "Speakers and Camera settings will be implemented in a future version.");
    }

    @FXML
    private void handleBatteryAnimations() {
        DialogUtil.showNotificationDialog(dialogStage, "Battery and Animations settings will be implemented in a future version.");
    }
}