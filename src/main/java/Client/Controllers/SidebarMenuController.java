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

    // Toggle Elements
    @FXML private Button nightModeToggleButton;

    // Footer Elements
    @FXML private Label appNameLabel;
    @FXML private Label versionLabel;

    // User data
    private String currentUserName = "User Name";
    private boolean isNightModeEnabled = true;

    // Add primaryStage field
    private Stage primaryStage;

    private Object parentController;

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
        setupAnimations();
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

    /**
     * Setup night mode toggle
     */
    private void setupNightModeToggle() {
        // Initialize toggle button state
        updateNightModeToggleAppearance();
        nightModeToggleButton.setOnAction(event -> handleNightModeToggle());
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
     * Setup animations for the sidebar
     */
    private void setupAnimations() {
        // Slide in animation for the entire sidebar
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(150), sidebarMenuContainer);
        slideIn.setFromX(-sidebarMenuContainer.getPrefWidth());
        slideIn.setToX(0);
        slideIn.setInterpolator(Interpolator.EASE_BOTH);
        slideIn.play();
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

    /**
     * Update night mode toggle appearance
     */
    private void updateNightModeToggleAppearance() {
        if (isNightModeEnabled) {
            nightModeToggleButton.getStyleClass().removeAll("toggle-button");
            nightModeToggleButton.getStyleClass().addAll("toggle-button", "toggle-button-on");
        } else {
            nightModeToggleButton.getStyleClass().removeAll("toggle-button-on");
            nightModeToggleButton.getStyleClass().addAll("toggle-button", "toggle-button-off");
        }
    }

    // ============ EVENT HANDLERS ============

    /**
     * Handle profile dropdown button click
     */
    @FXML
    private void handleProfileDropdown() {
        System.out.println("Profile dropdown clicked");
        // TODO: Show profile dropdown menu
        toggleSidebar(false); // Close sidebar
    }

    /**
     * Handle my profile button click
     */
    @FXML
    private void handleMyProfile() {
        System.out.println("My Profile clicked");
        // Navigate to profile view as a dialog
        toggleSidebar(false); // Close sidebar
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
        // TODO: Show create group dialog
        toggleSidebar(false); // Close sidebar
    }

    /**
     * Handle new channel button click
     */
    @FXML
    private void handleNewChannel() {
        System.out.println("New Channel clicked");
        // TODO: Show create channel dialog
        toggleSidebar(false); // Close sidebar
    }

    /**
     * Handle contacts button click
     */
    @FXML
    private void handleContacts() {
        System.out.println("Contacts clicked");
        // TODO: Navigate to contacts view
        toggleSidebar(false); // Close sidebar
    }

    /**
     * Handle calls button click
     */
    @FXML
    private void handleCalls() {
        System.out.println("Calls clicked");
        toggleSidebar(false); // Close sidebar
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
        // TODO: Navigate to settings view
        toggleSidebar(false); // Close sidebar
    }

    /**
     * Handle saved messages button click
     */
    @FXML
    private void handleSavedMessages() {
        System.out.println("Saved Messages clicked");
        // TODO: Navigate to saved messages view
        toggleSidebar(false); // Close sidebar
    }

    /**
     * Handle night mode toggle
     */
    @FXML
    private void handleNightModeToggle() {
        isNightModeEnabled = !isNightModeEnabled; // Toggle the state
        updateNightModeToggleAppearance();
        applyTheme();

        // Animate theme change
        FadeTransition themeTransition = new FadeTransition(Duration.millis(300), sidebarMenuContainer);
        themeTransition.setFromValue(0.8);
        themeTransition.setToValue(1.0);
        themeTransition.play();

        System.out.println("Night Mode: " + (isNightModeEnabled ? "Enabled" : "Disabled"));
        // TODO: Apply theme globally
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
                new KeyFrame(Duration.millis(300), new javafx.animation.KeyValue(dimEffect.brightnessProperty(), 0))
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

    /**
     * Show/hide the sidebar with animation
     */
    public void toggleSidebar(boolean show) {
        Platform.runLater(() -> {
            double width = sidebarMenuContainer.getWidth();
            System.out.println("Sidebar width: " + width + ", Show: " + show);
            if (width <= 0) {
                System.out.println("Warning: Sidebar width is zero or negative, forcing width to 300");
                sidebarMenuContainer.setPrefWidth(300);
                width = 300;
            }

            TranslateTransition slideTransition = new TranslateTransition(Duration.millis(150), sidebarMenuContainer);
            slideTransition.setInterpolator(Interpolator.EASE_BOTH);

            if (show) {
                slideTransition.setFromX(-width);
                slideTransition.setToX(0);
            } else {
                slideTransition.setFromX(0);
                slideTransition.setToX(-width);
            }

            slideTransition.play();
            slideTransition.setOnFinished(e -> System.out.println("Animation finished, X: " + sidebarMenuContainer.getTranslateX()));
        });
    }

    /**
     * Set sidebar to compact mode
     */
    public void setCompactMode(boolean compact) {
        if (compact) {
            sidebarMenuContainer.getStyleClass().add("compact");
        } else {
            sidebarMenuContainer.getStyleClass().removeAll("compact");
        }
    }

    /**
     * Get current theme mode
     */
    public boolean isNightModeEnabled() {
        return isNightModeEnabled;
    }

    /**
     * Set theme mode programmatically
     */
    public void setNightMode(boolean enabled) {
        isNightModeEnabled = enabled;
        updateNightModeToggleAppearance();
        applyTheme();
    }

    /**
     * Get sidebar container for external manipulation
     */
    public VBox getSidebarContainer() {
        return sidebarMenuContainer;
    }
}