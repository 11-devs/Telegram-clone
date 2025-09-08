// Path: java/Client/Controllers/SettingsController.java

package Client.Controllers;

import Client.AppConnectionManager;
import Client.RpcCaller;
import Client.Services.FileDownloadService;
import JSocket2.Core.Client.ConnectionManager;
import JSocket2.Protocol.Rpc.RpcResponse;
import JSocket2.Protocol.StatusCode;
import Shared.Api.Models.AccountController.GetAccountInfoOutputModel;
import Shared.Utils.AlertUtil;
import Shared.Utils.DialogUtil;
import Shared.Utils.SceneUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import java.util.Optional;

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
    @FXML private Button moreOptionsButton;

    private Stage dialogStage;
    private FileDownloadService fileDownloadService;
    private Object parentController;
    private Object currentSubController;
    private ConnectionManager connectionManager;
    private RpcCaller rpcCaller;
    private GetAccountInfoOutputModel accountInfo;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setParentController(Object parentController) {
        this.parentController = parentController;
    }

    public void setCurrentSubController(Object subController) {
        this.currentSubController = subController;
    }

    @FXML
    private void initialize() {
        connectionManager = AppConnectionManager.getInstance().getConnectionManager();
        rpcCaller = AppConnectionManager.getInstance().getRpcCaller();
        fileDownloadService = FileDownloadService.getInstance();
        loadInitialUserData();

        try {
            Image profileImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Client/images/11Devs-white.png")));
            profilePictureImage.setImage(profileImage);
        } catch (Exception e) {
            System.out.println("Could not load profile picture: " + e.getMessage());
        }
        moreOptionsButton.setOnAction(event -> showMoreOptionsMenu());

        // Initialize to show main settings content and hide sub-section pane
        mainSettingsContent.setVisible(true);
        mainSettingsContent.setManaged(true);
        settingsContentPane.setVisible(false);
        settingsContentPane.setManaged(false);
        settingsContentPane.getChildren().clear();
    }
    private void loadProfilePicture(String pictureId) {
        // Set default first.
        try {
            Image defaultImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Client/images/11Devs-white.png")));
            profilePictureImage.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Could not load default profile picture: " + e.getMessage());
        }

        if (pictureId != null && !pictureId.isEmpty()) {
            fileDownloadService.getFile(pictureId).thenAccept(file -> {
                Platform.runLater(() -> {
                    try {
                        Image avatar = new Image(file.toURI().toString());
                        profilePictureImage.setImage(avatar);
                    } catch (Exception e) {
                        System.err.println("Failed to load downloaded profile avatar: " + e.getMessage());
                    }
                });
            }).exceptionally(e -> {
                System.err.println("Failed to download profile avatar " + pictureId + ": " + e.getMessage());
                return null;
            });
        }
    }
    private void loadInitialUserData() {
        Task<RpcResponse<GetAccountInfoOutputModel>> getAccountInfoTask = new Task<>() {
            @Override
            protected RpcResponse<GetAccountInfoOutputModel> call() throws Exception {
                return rpcCaller.getAccountInfo();
            }
        };

        getAccountInfoTask.setOnSucceeded(event -> {
            RpcResponse<GetAccountInfoOutputModel> response = getAccountInfoTask.getValue();
            if (response.getStatusCode() == StatusCode.OK) {
                this.accountInfo = response.getPayload();
                Platform.runLater(() -> {
                    String fullName = this.accountInfo.getFirstName() + " " + this.accountInfo.getLastName();
                    String username = this.accountInfo.getUsername() != null ? "@" + this.accountInfo.getUsername() : "";
                    updateUserInfoOnHeader(this.accountInfo);
                    // TODO: Load profile picture from accountInfo.getProfilePictureId()
                });
            } else {
                System.err.println("Failed to load user data: " + response.getMessage());
                Platform.runLater(() -> {
                    AlertUtil.showError("Failed to load account information: " + response.getMessage());
                    if (dialogStage != null) {
                        dialogStage.close();
                    }
                });
            }
        });

        getAccountInfoTask.setOnFailed(event -> {
            System.err.println("Task failed to get user data.");
            getAccountInfoTask.getException().printStackTrace();
            Platform.runLater(() -> {
                AlertUtil.showError("An error occurred while loading account information.");
                if (dialogStage != null) {
                    dialogStage.close();
                }
            });
        });

        new Thread(getAccountInfoTask).start();
    }

    @FXML
    private void handleClose() {
        if (dialogStage != null) {
            dialogStage.close();
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
    private void handleAppearance() {
        showSubSection("/Client/fxml/appearanceSettings.fxml", "Appearance");
    }

    public void updateUserInfoOnHeader(GetAccountInfoOutputModel model) {
        String name = model.getFirstName() + " " + model.getLastName();
        updateDisplayNameOnHeader(name);
        updateUsernameOnHeader(model.getUsername());
        if (model.getPhoneNumber() != null && !model.getPhoneNumber().trim().isEmpty()) {
            phoneNumberLabel.setText(model.getPhoneNumber());
        }
        loadProfilePicture(model.getProfilePictureFileId());
    }
    public void updateUsernameOnHeader(String username){
        if (username != null && !username.trim().isEmpty()) {
            usernameLabel.setText("@" + username);
        }
    }
    public void updateDisplayNameOnHeader(String name){
        if (name != null && !name.trim().isEmpty()) {
            nameLabel.setText(name);
        }
    }

    /**
     * Shows a sub-section by loading its FXML and placing it in the content pane.
     * Delegates the loading and controller setup to SceneUtil.
     *
     * @param fxmlPath The path to the FXML file for the sub-section.
     * @param title    The title to display in the header.
     */
    private void showSubSection(String fxmlPath, String title) {
        if (fxmlPath.contains("myAccountSettings.fxml") && accountInfo == null) {
            return; // Wait for user data to be loaded
        }

        // 1. Use SceneUtil to load the sub-scene and set up its controller
        Parent subSectionRoot = SceneUtil.loadSubScene(
                fxmlPath,
                this,          // Pass this SettingsController as the parent
                this.dialogStage, // Pass the dialog's stage
                this.accountInfo  // Pass the account info as data
        );

        // 2. If loading was successful, update the UI
        if (subSectionRoot != null) {
            settingsTitleLabel.setText(title);
            backButton.setVisible(true);
            backButton.setManaged(true);

            settingsContentPane.getChildren().setAll(subSectionRoot);
            settingsContentPane.setVisible(true);
            settingsContentPane.setManaged(true);
            mainSettingsContent.setVisible(false);
            mainSettingsContent.setManaged(false);
        } else {
            AlertUtil.showError("Failed to load settings sub-section: " + title);
        }
    }

    // Method to navigate back to the main settings content
    public void goBackToMainSettings() {
        if (currentSubController instanceof MyAccountSettingsController) {
            ((MyAccountSettingsController) currentSubController).saveChanges();
            loadInitialUserData();
        }
        currentSubController = null;

        settingsTitleLabel.setText("Settings");
        backButton.setVisible(false);
        backButton.setManaged(false);

        settingsContentPane.getChildren().clear();
        settingsContentPane.setVisible(false);
        settingsContentPane.setManaged(false);
        mainSettingsContent.setVisible(true);
        mainSettingsContent.setManaged(true);
    }
    /**
     * Creates and shows a context menu
     */
    private void showMoreOptionsMenu() {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setAutoHide(true);

        // --- Create "editProfile" menu item ---
        MenuItem editItem = new MenuItem("Edit Profile");

        try {
            ImageView editLogo = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/Client/images/edit-icon.png")).toExternalForm()));
            editLogo.setFitHeight(18);
            editLogo.setFitWidth(18);
            editItem.setGraphic(editLogo);
        } catch (Exception e) {
            System.err.println("Could not load editProfile icon.");
        }

        // --- Set the action
        editItem.setOnAction(event -> handleEdit());

        contextMenu.getItems().add(editItem);

        // --- Create "Log Out" menu item ---
        MenuItem logOutItem = new MenuItem("Log Out");

        logOutItem.getStyleClass().add("logout-menu-item");

        try {
            ImageView logOutIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/Client/images/logout-icon.png")).toExternalForm()));
            logOutIcon.setFitHeight(18);
            logOutIcon.setFitWidth(18);
            logOutItem.setGraphic(logOutIcon);
        } catch (Exception e) {
            System.err.println("Could not load logout icon.");
        }

        // --- Set the action for the "Log Out" item ---
        logOutItem.setOnAction(event -> handleLogOut());

        contextMenu.getItems().add(logOutItem);

        // --- Show the menu aligned to the button ---
        contextMenu.show(moreOptionsButton, Side.BOTTOM, 0, 5);
    }

    private void handleEdit() {

    }

    private void handleLogOut() {

    }
}