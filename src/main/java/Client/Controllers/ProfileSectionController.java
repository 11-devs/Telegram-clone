package Client.Controllers;

import Shared.Models.UserType;
import Shared.Models.UserViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    @FXML private Label usernameLabel;

    @FXML private VBox membersSection;
    @FXML private Label membersHeaderLabel;
    @FXML private Button addMemberButton;
    @FXML private ListView<String> membersListView; // Placeholder, should be ListView<UserViewModel>

    @FXML private Button closeButton;
    @FXML private Button editButton;

    // State
    private Stage dialogStage;
    private UserViewModel userData;
    private MainChatController parentController;

    public void initialize() {
        closeButton.setOnAction(e -> handleClose());
        editButton.setOnAction(e -> handleEditProfile());
        // Initially hide sections that depend on data
        membersSection.setVisible(false);
        membersSection.setManaged(false);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setParentController(Object parentController) {
        if (parentController instanceof MainChatController) {
            this.parentController = (MainChatController) parentController;
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
        userNameLabel.setText(userData.getUserName());
        bioLabel.setText(userData.getBio() != null && !userData.getBio().isEmpty() ? userData.getBio() : "No bio yet.");

        // TODO: Load real profile picture using userData.getAvatarId()
        loadDefaultProfilePicture();

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
                statusLabel.setText(userData.isOnline() ? "online" : userData.getLastSeen());

                phoneFieldContainer.setVisible(true);
                phoneFieldContainer.setManaged(true);
                phoneNumberLabel.setText(userData.getPhoneNumber());

                usernameFieldContainer.setVisible(true);
                usernameFieldContainer.setManaged(true);
                usernameLabel.setText("@" + userData.getUserId());

                // TODO
                // Show edit button only if it's "My Profile"
                // This logic needs to be based on the actual logged-in user's ID
                // if (parentController != null && parentController.isMyProfile(userData)) {
                //     editButton.setVisible(true);
                // }
                break;

            case GROUP:
            case SUPERGROUP:
                profileHeaderTitle.setText("Group Info");
                statusLabel.setText(userData.getMembersCount() + " members");

                // Show members section
                membersSection.setVisible(true);
                membersSection.setManaged(true);
                membersHeaderLabel.setText("Members");

                // TODO: Load actual members from server
                membersListView.getItems().addAll("Member 1", "Member 2", "Member 3");

                // If current user is admin, show "Add Member" button
                if (parentController != null && parentController.isCurrentUserAdmin()) {
                    addMemberButton.setVisible(true);
                }
                break;

            case CHANNEL:
                profileHeaderTitle.setText("Channel Info");
                statusLabel.setText(userData.getMembersCount() + " subscribers");

                // Show members section for admins
                if (parentController != null && parentController.isCurrentUserAdmin()) {
                    membersSection.setVisible(true);
                    membersSection.setManaged(true);
                    membersHeaderLabel.setText("Subscribers");
                    // TODO: Load actual subscribers from server
                    membersListView.getItems().addAll("Subscriber 1", "Subscriber 2");
                }
                break;
        }
    }

    private void loadDefaultProfilePicture() {
        try {
            Image profileImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Client/images/11Devs-white.png")));
            profilePictureImage.setImage(profileImage);
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