// Path: java/Client/Controllers/SidebarMenuController.java

package Client.Controllers;

import Client.AppConnectionManager;
import Client.RpcCaller;
import Client.Services.FileDownloadService;
import Client.Services.ThemeManager;
import JSocket2.Protocol.Rpc.RpcResponse;
import JSocket2.Protocol.StatusCode;
import Shared.Api.Models.AccountController.GetAccountInfoOutputModel;
import Shared.Models.UserType;
import Shared.Models.UserViewModel;
import Shared.Models.UserViewModelBuilder;
import Shared.Utils.AlertUtil;
import Shared.Utils.DialogUtil;
import Shared.Utils.SceneUtil;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
import java.util.*;

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
    private boolean isNightModeEnabled = true;

    // Add primaryStage field
    private Stage primaryStage;

    private Object parentController;

    private Runnable closeHandler;

    private RpcCaller rpcCaller;
    private FileDownloadService fileDownloadService;
    ThemeManager themeManager;

    // Constructor to inject primaryStage
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setParentController(Object parentController) {
        this.parentController = parentController;
    }
    public Object getParentController() {
        return parentController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rpcCaller = AppConnectionManager.getInstance().getRpcCaller();
        fileDownloadService = FileDownloadService.getInstance();
        themeManager  = ThemeManager.getInstance();
        setupUserProfile();
        setupMenuItems();
        setupNightModeToggle();
        setupFooter();
        applyTheme();
        detectPlatform();
        loadUserProfile();
    }

    private void setupUserProfile() {

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
        isNightModeEnabled = themeManager.getCurrentTheme() == ThemeManager.Theme.DARK;
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

        if (isNightModeEnabled) {
            themeManager.setTheme(ThemeManager.Theme.DARK);
        } else {
            themeManager.setTheme(ThemeManager.Theme.LIGHT);
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

    private void loadUserProfile() {
        Task<RpcResponse<GetAccountInfoOutputModel>> getAccountInfoTask = new Task<>() {
            @Override
            protected RpcResponse<GetAccountInfoOutputModel> call() throws Exception {
                return rpcCaller.getAccountInfo();
            }
        };

        getAccountInfoTask.setOnSucceeded(event -> {
            RpcResponse<GetAccountInfoOutputModel> response = getAccountInfoTask.getValue();
            if (response.getStatusCode() == StatusCode.OK && response.getPayload() != null) {
                GetAccountInfoOutputModel accountInfo = response.getPayload();
                String displayName = accountInfo.getFirstName() + " " + accountInfo.getLastName();
                String avatarId = accountInfo.getProfilePictureFileId();

                if (avatarId != null && !avatarId.isBlank()) {
                    fileDownloadService.getImage(avatarId).thenAccept(image -> {
                        updateUserProfile(displayName, image);
                    }).exceptionally(e -> {
                        // If image download fails, still update name with default avatar
                        System.err.println("Failed to download sidebar avatar: " + e.getMessage());
                        updateUserProfile(displayName, null);
                        return null;
                    });
                } else {
                    updateUserProfile(displayName, null);
                }
            } else {
                System.err.println("Failed to load user profile for sidebar: " + (response != null ? response.getMessage() : "No response"));
            }
        });

        getAccountInfoTask.setOnFailed(event -> {
            System.err.println("Task to get account info for sidebar failed.");
            if (getAccountInfoTask.getException() != null) {
                getAccountInfoTask.getException().printStackTrace();
            }
        });

        new Thread(getAccountInfoTask).start();
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

        if (!(parentController instanceof MainChatController mainChatController)) {
            AlertUtil.showError("Cannot open profile. Internal controller error.");
            return;
        }

        // Start a task to get account info
        var getAccountInfoTask = mainChatController.getChatService().getAccountInfo();

        getAccountInfoTask.setOnSucceeded(workerStateEvent -> {
            var response = getAccountInfoTask.getValue();
            if (response.getStatusCode() == StatusCode.OK && response.getPayload() != null) {
                var accountInfo = response.getPayload();
                // This is important for the "Edit Profile" button logic
                mainChatController.setCurrentUserId(accountInfo.getId().toString());

                UserViewModel myProfileViewModel = new UserViewModelBuilder()
                        .userId(accountInfo.getId().toString())
                        .displayName(accountInfo.getFirstName() + " " + accountInfo.getLastName())
                        .username(accountInfo.getUsername())
                        .bio(accountInfo.getBio())
                        .phoneNumber(accountInfo.getPhoneNumber())
                        .avatarId(accountInfo.getProfilePictureFileId())
                        .isOnline(true)
                        .type(UserType.USER.toString())
                        .build();

                // Open the dialog on the JavaFX thread
                Platform.runLater(() -> {
                    try {
                        Stage currentPrimaryStage = primaryStage != null ? primaryStage : (Stage) sidebarMenuContainer.getScene().getWindow();
                        applyDialogAnimation(currentPrimaryStage, mainChatController, myProfileViewModel);
                    } catch (IOException e) {
                        e.printStackTrace();
                        AlertUtil.showError("Could not open profile settings.");
                    }
                });
            } else {
                Platform.runLater(() -> AlertUtil.showError("Failed to load profile: " + response.getMessage()));
            }
        });

        getAccountInfoTask.setOnFailed(workerStateEvent -> {
            Platform.runLater(() -> {
                getAccountInfoTask.getException().printStackTrace();
                AlertUtil.showError("Error fetching profile information.");
            });
        });

        new Thread(getAccountInfoTask).start();
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
            DialogUtil.showNotificationDialog(primaryStage, "Will be developed in future versions.\\\\\\\\n");
        } else {
            System.out.println("primaryStage is null!");
        }
    }

    @FXML
    private void handleSettings() {
        System.out.println("Settings clicked");
        close();
        Stage currentPrimaryStage = primaryStage != null ? primaryStage : (Stage) sidebarMenuContainer.getScene().getWindow();

        if (currentPrimaryStage == null) {
            System.err.println("Could not find a parent stage to open the settings dialog.");
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

            Stage settingsDialog = SceneUtil.createDialog(
                    "/Client/fxml/settings.fxml",
                    currentPrimaryStage,
                    this.parentController, // This should be MainChatController
                    null,
                    "Settings"
            );

            settingsDialog.showAndWait();

            loadUserProfile();

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
            System.err.println("Error loading settings dialog: " + e.getMessage());
            AlertUtil.showError("Could not open the settings window.");
            // Clean up on error
            if (currentPrimaryStage.getScene() != null && currentPrimaryStage.getScene().getRoot() != null) {
                currentPrimaryStage.getScene().getRoot().setEffect(null);
            }
        }
    }

    @FXML
    private void handleSavedMessages() {
        System.out.println("Saved Messages clicked");
        if(parentController instanceof MainChatController mainChatController){
            var getChatsTask =  mainChatController.getChatService().getSavedMessage();
            getChatsTask.setOnSucceeded(event -> {
                var response = getChatsTask.getValue();
                if (response.getStatusCode() == StatusCode.OK && response.getPayload() != null) {
                    Optional<UserViewModel> existingUser = mainChatController.getAllChatUsers().stream()
                            .filter(uvm -> uvm.getUserId().equals(response.getPayload().getId().toString()))
                            .findFirst();

                    UserViewModel userToSelect;
                    if (existingUser.isPresent()) {
                        userToSelect = existingUser.get();
                    } else {
                        // If not present, create a new UserViewModel and add it to the list
                        UserViewModel uvm = new UserViewModelBuilder()
                                .userId(response.getPayload().getId().toString())
                                .avatarId(response.getPayload().getProfilePictureId())
                                .displayName(response.getPayload().getTitle())
                                .lastMessage(response.getPayload().getLastMessage())
                                .time(response.getPayload().getLastMessageTimestamp())
                                .type(response.getPayload().getType())
                                .notificationsNumber(String.valueOf(response.getPayload().getUnreadCount()))
                                .build();
                        mainChatController.getAllChatUsers().add(0, uvm); // Add to the top of the master list

                        userToSelect = uvm;
                    }

                    // Select the user in the ListView, which will open the chat
                    mainChatController.getChatListView().getSelectionModel().select(userToSelect);
                    mainChatController.getChatListView().scrollTo(userToSelect);
                } else {
                    System.err.println("Failed to load chats: " + response.getMessage());
                }
            });

            getChatsTask.setOnFailed(event -> getChatsTask.getException().printStackTrace());
            new Thread(getChatsTask).start();
        }
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
        return applyDialogAnimation(parentStage, controller, null);
    }

    private Stage applyDialogAnimation(Stage parentStage, Object controller, Object data) throws IOException {
        ColorAdjust dimEffect = new ColorAdjust();
        parentStage.getScene().getRoot().setEffect(dimEffect);

        Timeline fadeIn = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(dimEffect.brightnessProperty(), 0)),
                new KeyFrame(Duration.millis(300), new KeyValue(dimEffect.brightnessProperty(), -0.3, Interpolator.EASE_BOTH))
        );
        fadeIn.play();

        Stage dialogStage = SceneUtil.createDialog("/Client/fxml/profileSection.fxml", parentStage, controller, data, "My Profile");

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
            if (name != null && !name.trim().isEmpty()) {
                userNameLabel.setText(name);
            }
            if (avatar != null && !avatar.isError()) {
                userAvatarImage.setImage(avatar);
            } else {
                // Fallback to default if provided avatar is null or has an error
                try {
                    Image defaultAvatar = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Client/images/11Devs-white.png")));
                    userAvatarImage.setImage(defaultAvatar);
                } catch (Exception e) {
                    System.err.println("Could not load default sidebar avatar: " + e.getMessage());
                }
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