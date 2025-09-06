package Client.Controllers;

import Shared.Utils.SceneUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class MyAccountSettingsController {

    private SettingsController parentController;
    private Stage dialogStage; // Reference to the actual dialog stage

    @FXML
    private VBox root; // The root VBox of this FXML
    @FXML
    private ImageView profilePictureImage;
    @FXML
    private Label userNameField; // Changed to Label
    @FXML
    private Label statusLabel;
    @FXML
    private Label bioCharCountLabel;
    @FXML
    private TextArea bioTextArea;
    @FXML
    private Label nameValueField; // Changed to Label
    @FXML
    private Label phoneValueLabel; // Corrected fx:id from phoneNumberFieldDisplay to phoneValueLabel
    @FXML
    private Label usernameValueField; // Changed to Label
    @FXML
    private Button changePhotoButton;
    @FXML
    private HBox nameRow;
    @FXML
    private HBox usernameRow;

    private String originalFirstName;
    private String originalLastName;
    private String originalUsername;
    private String originalBio;

    public void setParentController(SettingsController parentController) {
        this.parentController = parentController;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setSectionTitle(String title) {
        // Method is called via reflection, must exist.
    }

    @FXML
    private void initialize() {
        loadUserData();
        setupClickableFields();

        bioTextArea.textProperty().addListener((obs, oldVal, newVal) -> {
            bioCharCountLabel.setText(String.valueOf(70 - newVal.length()));
        });
    }

    private void loadUserData() {
        // In a real app, this data would come from a user service or model
        originalFirstName = "Ali";
        originalLastName = "Ghaedrahmat";
        originalUsername = "@AliGhaedrahmat";
        originalBio = "Any details such as age, occupation or city.\nExample: 23 y.o. designer from San Francisco";

        updateUIFields();

        try {
            Image profileImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Client/images/11Devs-white.png")));
            profilePictureImage.setImage(profileImage);
        } catch (Exception e) {
            System.err.println("Could not load default profile picture: " + e.getMessage());
        }
    }

    private void updateUIFields() {
        userNameField.setText(originalFirstName + " " + originalLastName);
        nameValueField.setText(originalFirstName + " " + originalLastName);
        statusLabel.setText("online");
        phoneValueLabel.setText("+98 901 650 1463");
        usernameValueField.setText(originalUsername);
        bioTextArea.setText(originalBio);
    }

    private void setupClickableFields() {
        nameRow.setOnMouseClicked(event -> handleEditNameClick());
        usernameRow.setOnMouseClicked(event -> handleEditUsernameClick());
    }

    // This method is no longer directly used for text fields, but might be for other controls.
    public void setEditing(boolean editing) {
        // bioTextArea is still editable directly
        bioTextArea.setEditable(editing);

        if (editing) {
            bioTextArea.getStyleClass().add("editable");
        } else {
            bioTextArea.getStyleClass().remove("editable");
        }
        // Name and username are now edited via dialogs, so their labels are not directly editable.
    }

    public void saveChanges() {
        // Logic to save bio changes and other settings not handled by dialogs
        originalBio = bioTextArea.getText();
        // In a real app, you would save these values to a database or server.
        System.out.println("Changes saved!");
    }

    public void discardChanges() {
        // Reset UI fields to original values
        updateUIFields();
        bioTextArea.setText(originalBio);
    }

    @FXML
    private void handleChangePhoto() {
        System.out.println("Change photo clicked");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(dialogStage);
        if (selectedFile != null) {
            Image newImage = new Image(selectedFile.toURI().toString());
            profilePictureImage.setImage(newImage);
            // TODO: Upload new profile picture to server
        }
    }

    @FXML
    private void handleEditNameClick() {
        System.out.println("Edit Name clicked");
        try {
            String[] currentNames = {originalFirstName, originalLastName};
            SceneUtil.createDialog("/Client/fxml/editNameDialog.fxml", dialogStage, this, currentNames, "Edit Name").showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditUsernameClick() {
        System.out.println("Edit Username clicked");
        try {
            SceneUtil.createDialog("/Client/fxml/editUsernameDialog.fxml", dialogStage, this, originalUsername.substring(1), "Edit Username").showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Methods called by dialogs to update this controller's state
    public void updateName(String firstName, String lastName) {
        this.originalFirstName = firstName; // Update internal state
        this.originalLastName = lastName;
        updateUIFields(); // Refresh UI
        System.out.println("Name updated to: " + firstName + " " + lastName);
        // TODO: Call RPC to update name on server
    }

    public void updateUsername(String username) {
        this.originalUsername = "@" + username; // Update internal state
        updateUIFields(); // Refresh UI
        System.out.println("Username updated to: " + username);
        // TODO: Call RPC to update username on server
    }
}