package Client.Controllers;

import Client.Services.ChatService;
import Client.Services.FileDownloadService;
import Shared.Models.UserViewModel;
import Shared.Utils.TextUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

public class ProfileSectionController {

    // FXML Fields
    @FXML private Label profileHeaderTitle;
    @FXML private ImageView profilePictureImage;
    @FXML private Label userNameLabel;
    @FXML private Label statusLabel;

    @FXML private VBox profileFieldsSection;
    @FXML private HBox phoneFieldContainer;
    @FXML private Label phoneNumberLabel;
    @FXML private HBox bioFieldContainer;
    @FXML private Label bioLabel;
    @FXML private HBox usernameFieldContainer;
    @FXML private Label usernameValueLabel;

    @FXML private VBox membersSection;
    @FXML private Label membersHeaderLabel;
    @FXML private Button addMemberButton;
    @FXML private ListView<UserViewModel> membersListView; // Changed from <String>

    @FXML private Button closeButton;
    @FXML private Button editButton;

    // State
    private Stage dialogStage;
    private UserViewModel userData;
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
            protected void updateItem(UserViewModel user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // For now, just display the name. A custom cell with avatar can be added later.
                    setText(user.getDisplayName() + (user.getType() != null ? " (" + user.getType() + ")" : ""));
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
        if (data instanceof UserViewModel) {
            this.userData = (UserViewModel) data;
            updateUI();
        }
    }

    private void updateUI() {
        if (userData == null) return;

        // 1. Update common fields
        usernameValueLabel.setText(userData.getDisplayName());
        userNameLabel.setText(userData.getUsername());
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
//        if (chatService == null || userData == null || userData.getUserId() == null) return;
//
//        membersListView.getItems().clear();
//        var getMembersTask = chatService.getChatMembers(UUID.fromString(userData.getUserId()));
//
//        getMembersTask.setOnSucceeded(event -> {
//            var response = getMembersTask.getValue();
//            if (response.getStatusCode() == StatusCode.OK && response.getPayload() != null) {
//                var members = response.getPayload().getMembers();
//                String currentUserId = parentController.getCurrentUserId();
//
//                Platform.runLater(() -> {
//                    for (GetChatMembersOutputModel.MemberInfo memberInfo : members) {
//                        UserViewModel memberViewModel = new UserViewModelBuilder()
//                                .userId(memberInfo.getUserId().toString())
//                                .userName(memberInfo.getUserName())
//                                .avatarId(memberInfo.getAvatarFileId())
//                                .role(memberInfo.getRole()) // Store the role
//                                .build();
//                        membersListView.getItems().add(memberViewModel);
//
//                        // Check if the current user is an admin to show the "Add Member" button
//                        if (currentUserId != null && currentUserId.equals(memberInfo.getUserId().toString())) {
//                            if ("OWNER".equals(memberInfo.getRole()) || "ADMIN".equals(memberInfo.getRole())) {
//                                addMemberButton.setVisible(true);
//                            }
//                        }
//                    }
//                });
//            } else {
//                System.err.println("Failed to load members: " + response.getMessage());
//            }
//        });
//
//        getMembersTask.setOnFailed(event -> {
//            System.err.println("Error while fetching members.");
//            getMembersTask.getException().printStackTrace();
//        });
//
//        new Thread(getMembersTask).start();
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
        // TODO: Implement logic to open an "Edit Profile" dialog
        System.out.println("Edit Profile clicked");
    }
}