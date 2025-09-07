package Client.Controllers;

import Shared.Utils.DialogUtil;
import Shared.Utils.SceneUtil;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.jfoenix.controls.JFXToggleButton;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Controller class for the Telegram-style sidebar menu
 */
public class SidebarMenuController implements Initializable {

    @FXML private VBox sidebarMenuContainer;

    // User Profile Elements
    @FXML private ImageView userAvatarImage;
    @FXML private Label userNameLabel;
    @FXML private Button profileDropdownButton;

    // Menu Item Buttons
    @FXML private Button myProfileButton;
    @FXML private Button newGroupButton;
    @FXML private Button newChannelButton;
    @FXML private Button contactsButton;
    @FXML private Button callsButton;
    @FXML private Button savedMessagesButton;
    @FXML private Button settingsButton;

    @FXML private Button nightModeButton;

    // Toggle Elements
    @FXML private JFXToggleButton nightModeToggleButton;

    // Footer Elements
    @FXML private Label appNameLabel;
    @FXML private Label versionLabel;

    // User data
    private String currentUserName = "User Name";
    private boolean isNightModeEnabled = true;

    // Add primaryStage field
    private Stage primaryStage;

    private Object parentController;

    private Runnable closeHandler;

    // Constructor to inject primaryStage
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setParentController(Object parentController) {
        this.parentController = parentController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupUserProfile();
        setupMenuItems();
        setupNightModeToggle();
        setupFooter();
        applyTheme();
        detectPlatform();
    }

    /**
     * Setup user profile section
     */
    private void setupUserProfile() {
        userNameLabel.setText(currentUserName);

        // Load user avatar (placeholder)
        try {
            Image avatarImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Client/images/11Devs-white.png")));
            userAvatarImage.setImage(avatarImage);
        } catch (Exception e) {
            System.out.println("Could not load user avatar image");
        }

        // Setup profile dropdown
        profileDropdownButton.setOnAction(event -> handleProfileDropdown());
    }

    /**
     * Setup menu item buttons
     */
    private void setupMenuItems() {
        // My Profile
        myProfileButton.setOnAction(event -> handleMyProfile());

        // New Group
        newGroupButton.setOnAction(event -> handleNewGroup());

        // New Channel
        newChannelButton.setOnAction(event -> handleNewChannel());

        // Contacts
        contactsButton.setOnAction(event -> handleContacts());

        // Calls
        callsButton.setOnAction(event -> handleCalls());

        // Saved Messages
        savedMessagesButton.setOnAction(event -> handleSavedMessages());

        // Settings
        settingsButton.setOnAction(event -> handleSettings());
    }

    private void setupNightModeToggle() {
        nightModeToggleButton.setSelected(isNightModeEnabled);

        nightModeButton.setOnAction(event -> {
            isNightModeEnabled = !isNightModeEnabled;

            nightModeToggleButton.setSelected(isNightModeEnabled);

            applyTheme();

            System.out.println("Night Mode: " + (isNightModeEnabled ? "Enabled" : "Disabled"));
            // TODO: Apply theme globally
        });
    }

    /**
     * Setup footer section
     */
    private void setupFooter() {
        // Make version label clickable
        versionLabel.setOnMouseClicked(event -> handleAbout());
    }

    /**
     * Apply current theme to the sidebar
     */
    private void applyTheme() {
        sidebarMenuContainer.getStyleClass().clear();
        sidebarMenuContainer.getStyleClass().add("sidebar-menu-container");

        // Theme is managed manually via CSS
        if (isNightModeEnabled) {
            // No additional class needed as CSS uses fixed dark theme for now
        } else {
            // We should add a light theme implementation
        }
    }

    /**
     * Detect platform and apply platform-specific styling
     */
    private void detectPlatform() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            sidebarMenuContainer.getStyleClass().add("windows");
        } else if (os.contains("mac")) {
            sidebarMenuContainer.getStyleClass().add("macos");
        } else {
            sidebarMenuContainer.getStyleClass().add("linux");
        }
    }

    // ============ EVENT HANDLERS ============

    /**
     * Handle profile dropdown button click
     */
    @FXML
    private void handleProfileDropdown() {
        System.out.println("Profile dropdown clicked");
        close();
        // TODO: Show profile dropdown menu
    }

    /**
     * Handle my profile button click
     */
    @FXML
    private void handleMyProfile() {
        System.out.println("My Profile clicked");
        // Navigate to profile view as a dialog
        close();
        Stage parentStage = primaryStage != null ? primaryStage : (Stage) sidebarMenuContainer.getScene().getWindow();
        try {
            Stage dialogStage = applyDialogAnimation(parentStage, this);
            dialogStage.setResizable(false); // Prevent resizing
            dialogStage.sizeToScene(); // Use default size from FXML

            // Center the dialog stage
            Platform.runLater(() -> {
                double centerX = parentStage.getX() + (parentStage.getWidth() - dialogStage.getWidth()) / 2;
                double centerY = parentStage.getY() + (parentStage.getHeight() - dialogStage.getHeight()) / 2;
                dialogStage.setX(centerX);
                dialogStage.setY(centerY);
            });
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading profile dialog: " + e.getMessage());
        }
    }

    /**
     * Handle new group button click
     */
    @FXML
    private void handleNewGroup() {
        System.out.println("New Group clicked");
        close();
        // TODO: Show create group dialog
    }

    /**
     * Handle new channel button click
     */
    @FXML
    private void handleNewChannel() {
        System.out.println("New Channel clicked");
        close();
        // TODO: Show create channel dialog
    }

    /**
     * Handle contacts button click
     */
    @FXML
    private void handleContacts() {
        System.out.println("Contacts clicked");
        close();
        // TODO: Navigate to contacts view
    }

    /**
     * Handle calls button click
     */
    @FXML
    private void handleCalls() {
        System.out.println("Calls clicked");
        close();
        if (primaryStage != null) {
            DialogUtil.showNotificationDialog(primaryStage, "Will be developed in future versions.\n");
        } else {
            System.out.println("primaryStage is null!");
        }
    }

    /**
     * Handle settings button click
     */
    @FXML
    private void handleSettings() {
        System.out.println("Settings clicked");
        close();
        //openSettings();
    }

    /**
     * Handle saved messages button click
     */
    @FXML
    private void handleSavedMessages() {
        System.out.println("Saved Messages clicked");
        close();
        // TODO: Navigate to saved messages view
    }

    /**
     * Handle about/version label click
     */
    @FXML
    private void handleAbout() {
        System.out.println("About clicked");
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/11-devs/Telegram-clone"));
        } catch (Exception e) {
            System.out.println("Failed to open link: " + e.getMessage());
        }
    }

    // ============ UTILITY METHODS ============

    /**
     * Closes the sidebar stage.
     */
    public void close() {
        if (closeHandler != null) {
            closeHandler.run();
        }
    }

    /**
     * Apply animation for dialog display and hide
     */
    private Stage applyDialogAnimation(Stage parentStage, Object controller) throws IOException {
        // Apply dim effect to parent stage with animation
        ColorAdjust dimEffect = new ColorAdjust();
        parentStage.getScene().getRoot().setEffect(dimEffect);

        Timeline fadeIn = new Timeline(
                new KeyFrame(Duration.ZERO, new javafx.animation.KeyValue(dimEffect.brightnessProperty(), 0)),
                new KeyFrame(Duration.millis(300), new javafx.animation.KeyValue(dimEffect.brightnessProperty(), -0.3, Interpolator.EASE_BOTH))
        );
        fadeIn.play();

        // Create and show dialog
        Stage dialogStage = SceneUtil.createDialog("/Client/fxml/profileSection.fxml", parentStage, controller, null, "My Profile");

        // Show and wait until dialog is closed
        dialogStage.showAndWait();

        // Reverse dim effect when dialog is closed
        Timeline fadeOut = new Timeline(
                new KeyFrame(Duration.ZERO, new javafx.animation.KeyValue(dimEffect.brightnessProperty(), -0.3, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(150), new javafx.animation.KeyValue(dimEffect.brightnessProperty(), 0))
        );
        fadeOut.setOnFinished(e -> parentStage.getScene().getRoot().setEffect(null));
        fadeOut.play();

        return dialogStage;
    }

    /**
     * Update user profile information
     */
    public void updateUserProfile(String name, Image avatar) {
        Platform.runLater(() -> {
            userNameLabel.setText(name);
            if (avatar != null) {
                userAvatarImage.setImage(avatar);
            }
        });
    }

    public void setCloseHandler(Runnable closeHandler) {
        this.closeHandler = closeHandler;
    }
    /**
     * Get current theme mode
     */
    public boolean isNightModeEnabled() {
        return isNightModeEnabled;
    }
}