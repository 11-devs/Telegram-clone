package Client.Controllers;

import Shared.Utils.AlertUtil;
import Shared.Utils.DialogUtil;
import Shared.Utils.SceneUtil;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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

    private void setupUserProfile() {
        userNameLabel.setText(currentUserName);

        try {
            Image avatarImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Client/images/11Devs-white.png")));
            userAvatarImage.setImage(avatarImage);
        } catch (Exception e) {
            System.out.println("Could not load user avatar image");
        }

        profileDropdownButton.setOnAction(event -> handleProfileDropdown());
    }

    private void setupMenuItems() {
        myProfileButton.setOnAction(event -> handleMyProfile());

        newGroupButton.setOnAction(event -> handleNewGroup());

        newChannelButton.setOnAction(event -> handleNewChannel());

        contactsButton.setOnAction(event -> handleContacts());

        callsButton.setOnAction(event -> handleCalls());

        savedMessagesButton.setOnAction(event -> handleSavedMessages());

        settingsButton.setOnAction(event -> handleSettings());
    }

    private void setupNightModeToggle() {
        nightModeToggleButton.setSelected(isNightModeEnabled);

        nightModeButton.setOnAction(event -> {
            isNightModeEnabled = !isNightModeEnabled;

            nightModeToggleButton.setSelected(isNightModeEnabled);

            applyTheme();

            System.out.println("Night Mode: " + (isNightModeEnabled ? "Enabled" : "Disabled"));
        });
    }

    private void setupFooter() {
        versionLabel.setOnMouseClicked(event -> handleAbout());
    }

    private void applyTheme() {
        sidebarMenuContainer.getStyleClass().clear();
        sidebarMenuContainer.getStyleClass().add("sidebar-menu-container");

        if (isNightModeEnabled) {
        } else {
        }
    }

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

    @FXML
    private void handleProfileDropdown() {
        System.out.println("Profile dropdown clicked");
        close();
    }

    @FXML
    private void handleMyProfile() {
        System.out.println("My Profile clicked");
        close();
        Stage currentPrimaryStage = primaryStage != null ? primaryStage : (Stage) sidebarMenuContainer.getScene().getWindow();
        try {
            Stage dialogStage = applyDialogAnimation(currentPrimaryStage, this);
            dialogStage.setResizable(false);
            dialogStage.sizeToScene();

            Platform.runLater(() -> {
                double centerX = currentPrimaryStage.getX() + (currentPrimaryStage.getWidth() - dialogStage.getWidth()) / 2;
                double centerY = currentPrimaryStage.getY() + (currentPrimaryStage.getHeight() - dialogStage.getHeight()) / 2;
                dialogStage.setX(centerX);
                dialogStage.setY(centerY);
            });
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading profile dialog: " + e.getMessage());
            AlertUtil.showError("Could not open profile settings.");
        }
    }

    @FXML
    private void handleNewGroup() {
        System.out.println("New Group clicked");
        close();
        Stage currentPrimaryStage = primaryStage != null ? primaryStage : (Stage) sidebarMenuContainer.getScene().getWindow();

        if (currentPrimaryStage == null) {
            System.err.println("Could not find a parent stage to open the new group dialog.");
            return;
        }

        try {
            ColorAdjust dimEffect = new ColorAdjust();
            if (currentPrimaryStage.getScene() != null && currentPrimaryStage.getScene().getRoot() != null) {
                currentPrimaryStage.getScene().getRoot().setEffect(dimEffect);
            }

            Timeline fadeIn = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(dimEffect.brightnessProperty(), 0)),
                    new KeyFrame(Duration.millis(300), new KeyValue(dimEffect.brightnessProperty(), -0.3, Interpolator.EASE_BOTH))
            );
            fadeIn.play();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client/fxml/createGroupDialog.fxml"));
            Parent root = loader.load();

            if (!(parentController instanceof MainChatController)) {
                throw new IllegalStateException("SidebarMenuController's parent is not MainChatController, cannot create group.");
            }
            CreateGroupDialogController controller = loader.getController();
            controller.init((MainChatController) parentController, currentPrimaryStage);

            Stage dialogStage = new Stage();
            dialogStage.initOwner(currentPrimaryStage);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initStyle(StageStyle.UNDECORATED);

            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            dialogStage.setScene(scene);
            dialogStage.initStyle(StageStyle.TRANSPARENT);

            dialogStage.setOnShown(e -> {
                dialogStage.setX(currentPrimaryStage.getX() + currentPrimaryStage.getWidth() / 2 - dialogStage.getWidth() / 2);
                dialogStage.setY(currentPrimaryStage.getY() + currentPrimaryStage.getHeight() / 2 - dialogStage.getHeight() / 2);
            });

            dialogStage.showAndWait();

            Timeline fadeOut = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(dimEffect.brightnessProperty(), -0.3, Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.millis(150), new KeyValue(dimEffect.brightnessProperty(), 0))
            );
            if (currentPrimaryStage.getScene() != null && currentPrimaryStage.getScene().getRoot() != null) {
                fadeOut.setOnFinished(e -> currentPrimaryStage.getScene().getRoot().setEffect(null));
            }
            fadeOut.play();

        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
            System.err.println("Error loading new group dialog: " + e.getMessage());
            AlertUtil.showError("Could not open the new group creation window.");
            // Also, remove the dim effect in case of an error
            if (currentPrimaryStage.getScene() != null && currentPrimaryStage.getScene().getRoot() != null) {
                currentPrimaryStage.getScene().getRoot().setEffect(null);
            }
        }
    }

    @FXML
    private void handleNewChannel() {
        System.out.println("New Channel clicked");
        close();
        Stage currentPrimaryStage = primaryStage != null ? primaryStage : (Stage) sidebarMenuContainer.getScene().getWindow();

        if (currentPrimaryStage == null) {
            System.err.println("Could not find a parent stage to open the new channel dialog.");
            return;
        }

        try {
            ColorAdjust dimEffect = new ColorAdjust();
            if (currentPrimaryStage.getScene() != null && currentPrimaryStage.getScene().getRoot() != null) {
                currentPrimaryStage.getScene().getRoot().setEffect(dimEffect);
            }

            Timeline fadeIn = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(dimEffect.brightnessProperty(), 0)),
                    new KeyFrame(Duration.millis(300), new KeyValue(dimEffect.brightnessProperty(), -0.3, Interpolator.EASE_BOTH))
            );
            fadeIn.play();

            Stage newChannelDialog = SceneUtil.createDialog(
                    "/Client/fxml/createChannelDialog.fxml",
                    currentPrimaryStage,
                    this,
                    null,
                    "New Channel"
            );

            newChannelDialog.showAndWait();

            Timeline fadeOut = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(dimEffect.brightnessProperty(), -0.3, Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.millis(150), new KeyValue(dimEffect.brightnessProperty(), 0))
            );
            if (currentPrimaryStage.getScene() != null && currentPrimaryStage.getScene().getRoot() != null) {
                fadeOut.setOnFinished(e -> currentPrimaryStage.getScene().getRoot().setEffect(null));
            }
            fadeOut.play();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading new channel dialog: " + e.getMessage());
            AlertUtil.showError("Could not open the new channel creation window.");
        }
    }

    @FXML
    private void handleContacts() {
        System.out.println("Contacts clicked");
        close();
        Stage currentPrimaryStage = primaryStage != null ? primaryStage : (Stage) sidebarMenuContainer.getScene().getWindow();

        if (currentPrimaryStage == null) {
            System.err.println("Could not find a parent stage to open the contacts dialog.");
            return;
        }

        try {
            ColorAdjust dimEffect = new ColorAdjust();
            if (currentPrimaryStage.getScene() != null && currentPrimaryStage.getScene().getRoot() != null) {
                currentPrimaryStage.getScene().getRoot().setEffect(dimEffect);
            }

            Timeline fadeIn = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(dimEffect.brightnessProperty(), 0)),
                    new KeyFrame(Duration.millis(300), new KeyValue(dimEffect.brightnessProperty(), -0.3, Interpolator.EASE_BOTH))
            );
            fadeIn.play();

            Stage contactsDialog = SceneUtil.createDialog(
                    "/Client/fxml/contactsSection.fxml",
                    currentPrimaryStage,
                    this,
                    null,
                    "Contacts"
            );

            contactsDialog.showAndWait();

            Timeline fadeOut = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(dimEffect.brightnessProperty(), -0.3, Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.millis(150), new KeyValue(dimEffect.brightnessProperty(), 0))
            );
            if (currentPrimaryStage.getScene() != null && currentPrimaryStage.getScene().getRoot() != null) {
                fadeOut.setOnFinished(e -> currentPrimaryStage.getScene().getRoot().setEffect(null));
            }
            fadeOut.play();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading contacts dialog: " + e.getMessage());
            AlertUtil.showError("Could not open the contacts window.");
        }
    }

    @FXML
    private void handleCalls() {
        System.out.println("Calls clicked");
        close();
        if (primaryStage != null) {
            DialogUtil.showNotificationDialog(primaryStage, "Will be developed in future versions.\\n");
        } else {
            System.out.println("primaryStage is null!");
        }
    }

    @FXML
    private void handleSettings() {
        System.out.println("Settings clicked");
        close();
    }

    @FXML
    private void handleSavedMessages() {
        System.out.println("Saved Messages clicked");
        close();
    }

    @FXML
    private void handleAbout() {
        System.out.println("About clicked");
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/11-devs/Telegram-clone"));
        } catch (Exception e) {
            System.out.println("Failed to open link: " + e.getMessage());
        }
    }

    public void close() {
        if (closeHandler != null) {
            closeHandler.run();
        }
    }

    private Stage applyDialogAnimation(Stage parentStage, Object controller) throws IOException {
        ColorAdjust dimEffect = new ColorAdjust();
        parentStage.getScene().getRoot().setEffect(dimEffect);

        Timeline fadeIn = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(dimEffect.brightnessProperty(), 0)),
                new KeyFrame(Duration.millis(300), new KeyValue(dimEffect.brightnessProperty(), -0.3, Interpolator.EASE_BOTH))
        );
        fadeIn.play();

        Stage dialogStage = SceneUtil.createDialog("/Client/fxml/profileSection.fxml", parentStage, controller, null, "My Profile");

        dialogStage.showAndWait();

        Timeline fadeOut = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(dimEffect.brightnessProperty(), -0.3, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(150), new KeyValue(dimEffect.brightnessProperty(), 0))
        );
        fadeOut.setOnFinished(e -> parentStage.getScene().getRoot().setEffect(null));
        fadeOut.play();

        return dialogStage;
    }

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

    public boolean isNightModeEnabled() {
        return isNightModeEnabled;
    }
}