package Client.Controllers;

import Shared.Utils.DialogUtil;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Controller class for the Telegram-style profile section
 */
public class ProfileSectionController implements Initializable {

    @FXML private VBox profileContainer;

    // Header Elements
    @FXML private Button editButton;
    @FXML private Button closeButton;

    // Profile Elements
    @FXML private ImageView profilePictureImage;
    @FXML private Label userNameLabel;
    @FXML private Label statusLabel;

    // Profile Information Labels
    @FXML private Label phoneNumberLabel;
    @FXML private Label bioLabel;
    @FXML private Label usernameLabel;
    @FXML private Button qrCodeButton;

    // User data
    private String currentUserName = "User Name";
    private String currentUserID = "ID";
    private String currentPhone = "+98 111 111 1111";
    private String currentBio = "Bio text";

    // Reference to primary stage and parent controller
    private Stage primaryStage;
    private Object parentController;
    private Stage dialogStage; // Added for dialog mode

    /**
     * Set the primary stage reference
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Set the parent controller reference
     */
    public void setParentController(Object parentController) {
        this.parentController = parentController;
    }

    /**
     * Set the dialog stage reference
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupProfilePicture();
        setupProfileLabels();
        setupHeaderButtons();
        setupAnimations();
        detectPlatform();
        loadUserData();
    }

    /**
     * Setup profile picture
     */
    private void setupProfilePicture() {
        // Load default profile picture
        try {
            Image profileImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Client/images/11Devs-white.png")));
            profilePictureImage.setImage(profileImage);
        } catch (Exception e) {
            System.out.println("Could not load profile picture: " + e.getMessage());
        }

        // Make profile picture clickable for changing
        profilePictureImage.setOnMouseClicked(event -> {
            handleChangePicture();
        });
    }

    /**
     * Setup profile labels with current data
     */
    private void setupProfileLabels() {
        userNameLabel.setText(currentUserName);
        phoneNumberLabel.setText(currentPhone);
        bioLabel.setText(currentBio);
        usernameLabel.setText("@" + currentUserID);
    }

    /**
     * Setup header buttons
     */
    private void setupHeaderButtons() {
        editButton.setOnAction(event -> handleEditProfile());
        closeButton.setOnAction(event -> handleClose());
        qrCodeButton.setOnAction(event -> handleQRCode());
    }

    /**
     * Setup animations for the profile section
     */
    private void setupAnimations() {
        // Slide in animation for the entire profile container
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(250), profileContainer);
        slideIn.setFromX(profileContainer.getPrefWidth());
        slideIn.setToX(0);
        slideIn.setInterpolator(Interpolator.EASE_OUT);
        slideIn.play();
    }

    /**
     * Detect platform and apply platform-specific styling
     */
    private void detectPlatform() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            profileContainer.getStyleClass().add("windows");
        } else if (os.contains("mac")) {
            profileContainer.getStyleClass().add("macos");
        } else {
            profileContainer.getStyleClass().add("linux");
        }
    }

    /**
     * Load user data from storage or database
     */
    private void loadUserData() {
        // TODO: Load actual user data from storage/database
        updateProfileDisplay();
    }

    /**
     * Update the profile display with current data
     */
    private void updateProfileDisplay() {
        Platform.runLater(() -> {
            userNameLabel.setText(currentUserName);
            phoneNumberLabel.setText(currentPhone);
            bioLabel.setText(currentBio.isEmpty() ? "Add a bio..." : currentBio);
            usernameLabel.setText("@" + currentUserID);
        });
    }

    // ============ EVENT HANDLERS ============

    /**
     * Handle edit button click
     */
    @FXML
    private void handleEditProfile() {
        System.out.println("Edit Profile clicked");
        // TODO: dev this section
    }

    /**
     * Handle close button click
     */
    @FXML
    private void handleClose() {
        System.out.println("Close Profile clicked");
        if (dialogStage != null) {
            dialogStage.close();
        } else {
            hideProfileWithAnimation();
        }
    }

    /**
     * Handle change picture
     */
    private void handleChangePicture() {
        System.out.println("Change Picture clicked");

        if (primaryStage != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Profile Picture");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );

            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                try {
                    Image newImage = new Image(selectedFile.toURI().toString());

                    // Animate picture change
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(150), profilePictureImage);
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.3);
                    fadeOut.setOnFinished(e -> {
                        profilePictureImage.setImage(newImage);
                        FadeTransition fadeIn = new FadeTransition(Duration.millis(150), profilePictureImage);
                        fadeIn.setFromValue(0.3);
                        fadeIn.setToValue(1.0);
                        fadeIn.play();
                    });
                    fadeOut.play();

                    System.out.println("Profile picture updated");
                } catch (Exception e) {
                    System.out.println("Error loading image: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Handle QR code button click
     */
    @FXML
    private void handleQRCode() {
        System.out.println("QR Code clicked");
        if (primaryStage != null) {
            DialogUtil.showNotificationDialog(primaryStage, "QR Code feature will be available in future versions.");
        }
    }

    // ============ PUBLIC METHODS ============

    /**
     * Update user profile information
     */
    public void updateUserProfile(String name, String username, String phone, String bio, Image profileImage) {
        Platform.runLater(() -> {
            currentUserName = name != null ? name : "User Name";
            currentUserID = username != null ? username : "";
            currentPhone = phone != null ? phone : "";
            currentBio = bio != null ? bio : "";

            if (profileImage != null) {
                profilePictureImage.setImage(profileImage);
            }

            updateProfileDisplay();
        });
    }

    /**
     * Show the profile section with animation
     */
    public void showProfile() {
        profileContainer.setVisible(true);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(250), profileContainer);
        slideIn.setFromX(profileContainer.getPrefWidth());
        slideIn.setToX(0);
        slideIn.setInterpolator(Interpolator.EASE_OUT);
        slideIn.play();
    }

    /**
     * Hide the profile section with animation
     */
    public void hideProfileWithAnimation() {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(250), profileContainer);
        slideOut.setFromX(0);
        slideOut.setToX(profileContainer.getWidth());
        slideOut.setInterpolator(Interpolator.EASE_IN);
        slideOut.setOnFinished(e -> {
            profileContainer.setVisible(false);
        });
        slideOut.play();
    }

    /**
     * Get current username
     */
    public String getCurrentUserName() {
        return currentUserName;
    }

    /**
     * Get current username
     */
    public String getCurrentUserID() {
        return currentUserID;
    }

    /**
     * Get current phone number
     */
    public String getCurrentPhone() {
        return currentPhone;
    }

    /**
     * Get current bio
     */
    public String getCurrentBio() {
        return currentBio;
    }

    /**
     * Get current profile image
     */
    public Image getCurrentProfileImage() {
        return profilePictureImage.getImage();
    }

    /**
     * Get the profile container for external manipulation
     */
    public VBox getProfileContainer() {
        return profileContainer;
    }
}