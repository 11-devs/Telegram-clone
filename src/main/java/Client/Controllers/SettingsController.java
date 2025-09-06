package Client.Controllers;

import Shared.Utils.AlertUtil;
import Shared.Utils.DialogUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
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
    @FXML
    private StackPane settingsContentPane; // New StackPane to hold dynamic sub-sections
    @FXML
    private VBox mainSettingsContent; // Original content, now a single unit
    @FXML
    private Button backButton;
    @FXML
    private Label settingsTitleLabel;

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
        nameLabel.setText("My Account");
        phoneNumberLabel.setText("+98 912 345 6789");
        usernameLabel.setText("@me");
        try {
            Image profileImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Client/images/11Devs-white.png")));
            profilePictureImage.setImage(profileImage);
        } catch (Exception e) {
            System.out.println("Could not load profile picture: " + e.getMessage());
        }
        // Initialize to show main settings content and hide sub-section pane
        mainSettingsContent.setVisible(true);
        mainSettingsContent.setManaged(true);
        settingsContentPane.setVisible(false);
        settingsContentPane.setManaged(false);
        settingsContentPane.getChildren().clear();
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
        }
    }

    @FXML
    private void handleMyAccount() {
        showSubSection("/Client/fxml/myAccountSettings.fxml", "Info");
    }

    @FXML
    private void handleNotifications() {
        showSubSection("/Client/fxml/notificationsSettings.fxml", "Notifications and Sounds");
    }

    @FXML
    private void handlePrivacySecurity() {
        showSubSection("/Client/fxml/privacySecuritySettings.fxml", "Privacy and Security");
    }

    @FXML
    private void handleChatSettings() {
        showSubSection("/Client/fxml/chatSettings.fxml", "Chat Settings");
    }

    @FXML
    private void handleFolders() {
        showSubSection("/Client/fxml/foldersSettings.fxml", "Folders");
    }

    @FXML
    private void handleAdvanced() {
        showSubSection("/Client/fxml/advancedSettings.fxml", "Advanced");
    }

    @FXML
    private void handleSpeakersCamera() {
        showSubSection("/Client/fxml/speakersCameraSettings.fxml", "Speakers and Camera");
    }

    @FXML
    private void handleBatteryAnimations() {
        showSubSection("/Client/fxml/batteryAnimationsSettings.fxml", "Battery and Animations");
    }

    // Method to show a subsection within the settings dialog
    private void showSubSection(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent subSectionRoot = loader.load();

            // Set parent controller and dialog stage to the sub-section controller
            Object subController = loader.getController();
            if (subController != null) {
                try {
                    subController.getClass().getMethod("setParentController", SettingsController.class).invoke(subController, this);
                    subController.getClass().getMethod("setDialogStage", Stage.class).invoke(subController, dialogStage);
                    // The sub-controller's setSectionTitle is now effectively unused for the title bar,
                    // but we keep the call to avoid breaking other sub-sections that might still use it internally.
                    subController.getClass().getMethod("setSectionTitle", String.class).invoke(subController, title);
                } catch (NoSuchMethodException e) {
                    System.err.println("Sub-controller " + subController.getClass().getName() + " missing required setter methods. " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Error invoking setter on sub-controller: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            settingsTitleLabel.setText(title);
            backButton.setVisible(true);
            backButton.setManaged(true);

            settingsContentPane.getChildren().setAll(subSectionRoot);
            settingsContentPane.setVisible(true);
            settingsContentPane.setManaged(true);
            mainSettingsContent.setVisible(false);
            mainSettingsContent.setManaged(false);

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Failed to load settings sub-section: " + title);
        }
    }

    // Method to navigate back to the main settings content
    public void goBackToMainSettings() {
        settingsTitleLabel.setText("Settings");
        backButton.setVisible(false);
        backButton.setManaged(false);
        
        settingsContentPane.getChildren().clear();
        settingsContentPane.setVisible(false);
        settingsContentPane.setManaged(false);
        mainSettingsContent.setVisible(true);
        mainSettingsContent.setManaged(true);
    }
}