package Client.Controllers;

import Client.Services.ChatService;
import Client.Services.FileDownloadService;
import JSocket2.Protocol.StatusCode;
import Shared.Api.Models.MembershipController.GetChatMembersOutputModel;
import Shared.Models.ChatViewModel;
import Shared.Models.ChatViewModelBuilder;
import Shared.Utils.AlertUtil;
import Shared.Utils.SceneUtil;
import Shared.Utils.TextUtil;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;

public class ProfileSectionController {

    // FXML Fields
    @FXML private Label profileHeaderTitle;
    @FXML private ImageView profilePictureImage;
    @FXML private Label displayNameLabel;
    @FXML private Label statusLabel;

    @FXML private VBox profileFieldsSection;
    @FXML private HBox phoneFieldContainer;
    @FXML private Label phoneNumberLabel;
    @FXML private HBox bioFieldContainer;
    @FXML private Label bioLabel;
    @FXML private Label bioLabelText;
    @FXML private HBox usernameFieldContainer;
    @FXML private Label usernameValueLabel;

    @FXML private VBox membersSection;
    @FXML private Label membersHeaderLabel;
    @FXML private Button addMemberButton;
    @FXML private ListView<ChatViewModel> membersListView;

    @FXML private Button closeButton;
    @FXML private Button editButton;

    // State
    private Stage dialogStage;
    private ChatViewModel userData;
    private MainChatController parentController;
    private ChatService chatService;
    private FileDownloadService fileDownloadService;

    public void initialize() {
        closeButton.setOnAction(e -> handleClose());
        editButton.setOnAction(e -> handleEditProfile());
        // Initially hide sections that depend on data
        membersSection.setVisible(false);
        membersSection.setManaged(false);

        // Setup ListView CellFactory to display user info
        membersListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ChatViewModel user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // For now, just display the name. A custom cell with avatar can be added later.
                    setText(user.getDisplayName());
                }
            }
        });
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setParentController(Object parentController) {
        if (parentController instanceof MainChatController) {
            this.parentController = (MainChatController) parentController;
            // Get services from the parent controller and singleton
            this.chatService = this.parentController.getChatService();
            this.fileDownloadService = FileDownloadService.getInstance();
        }
    }

    // Main method to receive data
    public void setData(Object data) {
        if (data instanceof ChatViewModel) {
            this.userData = (ChatViewModel) data;
            updateUI();
        }
    }

    private void updateUI() {
        if (userData == null) return;


        usernameValueLabel.setText(userData.getUsername());
        displayNameLabel.setText(userData.getDisplayName());
        bioLabel.setText(userData.getBio() != null && !userData.getBio().isEmpty() ? userData.getBio() : "No bio yet.");

        // Load real profile picture
        if (userData.getAvatarId() != null && !userData.getAvatarId().isBlank()) {
            fileDownloadService.getImage(userData.getAvatarId()).thenAccept(image -> {
                if (image != null) {
                    Platform.runLater(() -> profilePictureImage.setImage(image));
                } else {
                    loadDefaultProfilePicture();
                }
            }).exceptionally(e -> {
                e.printStackTrace();
                loadDefaultProfilePicture();
                return null;
            });
        } else {
            loadDefaultProfilePicture();
        }

        // 2. Hide all specific fields initially
        phoneFieldContainer.setVisible(false);
        phoneFieldContainer.setManaged(false);
        usernameFieldContainer.setVisible(false);
        usernameFieldContainer.setManaged(false);
        membersSection.setVisible(false);
        membersSection.setManaged(false);
        editButton.setVisible(false); // Hide edit button by default
        bioLabelText.setText("Description");
        // 3. Show fields and set labels based on chat type
        switch (userData.getType()) {
            case USER:
                profileHeaderTitle.setText("User Info");
                statusLabel.setText(userData.isOnline() ? "online" : TextUtil.formatLastSeen(userData.getLastSeen()));

                phoneFieldContainer.setVisible(true);
                phoneFieldContainer.setManaged(true);
                phoneNumberLabel.setText(userData.getPhoneNumber());

                usernameFieldContainer.setVisible(true);
                usernameFieldContainer.setManaged(true);
                //usernameLabel.setText("@" + userData.getUserIdForUsername());

                // Show edit button only if it's "My Profile"
                if (parentController != null && parentController.isMyProfile(userData)) {
                    editButton.setVisible(true);
                }
                bioLabelText.setText("Bio");
                break;

            case GROUP:
            case SUPERGROUP:
                profileHeaderTitle.setText("Group Info");
                statusLabel.setText(userData.getMembersCount() + " members");

                membersSection.setVisible(true);
                membersSection.setManaged(true);
                membersHeaderLabel.setText("Members");
                loadMembers();
                break;

            case CHANNEL:
                profileHeaderTitle.setText("Channel Info");
                statusLabel.setText(userData.getMembersCount() + " subscribers");

                membersSection.setVisible(true);
                membersSection.setManaged(true);
                membersHeaderLabel.setText("Subscribers");
                loadMembers();
                break;
        }
    }

    private void loadMembers() {
        if (chatService == null || userData == null || userData.getChatId() == null) return;

        membersListView.getItems().clear();
        var getMembersTask = chatService.getChatMembers(userData.getChatId());

        getMembersTask.setOnSucceeded(event -> {
            var response = getMembersTask.getValue();
            if (response.getStatusCode() == StatusCode.OK && response.getPayload() != null) {
                var members = response.getPayload().getMembers();
                Platform.runLater(() -> {
                    for (var memberInfo : members) {
                        ChatViewModel memberViewModel = new ChatViewModelBuilder()
                                .userId(memberInfo.getUserId().toString())
                                .displayName(memberInfo.getFirstName() + memberInfo.getLastName())
                                .avatarId(memberInfo.getProfilePictureFileId())
                                .build();
                        membersListView.getItems().add(memberViewModel);

                        // Check if the current user is an admin to show the "Add Member" button
//                        if (currentUserId != null && currentUserId.equals(memberInfo.getUserId().toString())) {
//                            if ("OWNER".equals(memberInfo.getRole()) || "ADMIN".equals(memberInfo.getRole())) {
//                                addMemberButton.setVisible(true);
//                            }
//                        }
                    }
                });
            } else {
                System.err.println("Failed to load members: " + response.getMessage());
            }
        });

        getMembersTask.setOnFailed(event -> {
            System.err.println("Error while fetching members.");
            getMembersTask.getException().printStackTrace();
        });

        new Thread(getMembersTask).start();
    }

    private void loadDefaultProfilePicture() {
        try {
            Image profileImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Client/images/11Devs-white.png")));
            Platform.runLater(() -> profilePictureImage.setImage(profileImage));
        } catch (Exception e) {
            System.err.println("Could not load default profile picture: " + e.getMessage());
        }
    }

    @FXML
    private void handleClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    @FXML
    private void handleEditProfile() {
        System.out.println("Edit Profile clicked");

        Stage primaryStage = (Stage) dialogStage.getOwner();
        if (primaryStage == null) {
            System.err.println("Could not find a parent stage to open the settings dialog.");
            AlertUtil.showError("Could not open settings.");
            return;
        }

        // Close the current profile dialog.
        handleClose();

        // Use Platform.runLater to open the new dialog after the current one has closed.
        Platform.runLater(() -> {
            try {
                // Apply dimming effect
                ColorAdjust dimEffect = new ColorAdjust();
                if (primaryStage.getScene() != null && primaryStage.getScene().getRoot() != null) {
                    primaryStage.getScene().getRoot().setEffect(dimEffect);
                }

                Timeline fadeIn = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(dimEffect.brightnessProperty(), 0)),
                        new KeyFrame(Duration.millis(300), new KeyValue(dimEffect.brightnessProperty(), -0.3, Interpolator.EASE_BOTH))
                );
                fadeIn.play();

                // Create and show settings dialog
                Stage settingsDialog = SceneUtil.createDialog(
                        "/Client/fxml/settings.fxml",
                        primaryStage,
                        this.parentController,
                        null,
                        "Settings"
                );
                settingsDialog.showAndWait();

                // Remove dimming effect
                Timeline fadeOut = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(dimEffect.brightnessProperty(), -0.3, Interpolator.EASE_BOTH)),
                        new KeyFrame(Duration.millis(150), new KeyValue(dimEffect.brightnessProperty(), 0))
                );
                if (primaryStage.getScene() != null && primaryStage.getScene().getRoot() != null) {
                    fadeOut.setOnFinished(e -> primaryStage.getScene().getRoot().setEffect(null));
                }
                fadeOut.play();

            } catch (IOException e) {
                e.printStackTrace();
                AlertUtil.showError("Could not open the settings window.");
                // Ensure dim effect is removed on error
                if (primaryStage.getScene() != null && primaryStage.getScene().getRoot() != null) {
                    primaryStage.getScene().getRoot().setEffect(null);
                }
            }
        });
    }
}