package Client.Controllers;
import Client.AppConnectionManager;
import Client.RpcCaller;
import Client.Tasks.UploadTask;
import JSocket2.Core.Client.ConnectionManager;
import JSocket2.Protocol.Rpc.RpcResponse;
import JSocket2.Protocol.StatusCode;
import JSocket2.Protocol.Transfer.FileInfoModel;
import JSocket2.Protocol.Transfer.IProgressListener;
import JSocket2.Utils.FileUtil;
import Shared.Api.Models.AccountController.GetAccountInfoOutputModel;
import Shared.Api.Models.AccountController.SetProfilePictureInputModel;
import Shared.Api.Models.AccountController.UpdateNameInputModel;
import Shared.Api.Models.MediaController.CreateMediaInputModel;
import Shared.Utils.AlertUtil;
import Shared.Utils.SceneUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;

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

    private ConnectionManager connectionManager;
    private RpcCaller rpcCaller;

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
        connectionManager = AppConnectionManager.getInstance().getConnectionManager();
        rpcCaller = AppConnectionManager.getInstance().getRpcCaller();
        setupClickableFields();

        bioTextArea.textProperty().addListener((obs, oldVal, newVal) -> {
            bioCharCountLabel.setText(String.valueOf(70 - newVal.length()));
        });
    }

    public void setUserData(GetAccountInfoOutputModel data) {
        originalFirstName = data.getFirstName();
        originalLastName = data.getLastName();
        originalUsername = data.getUsername() != null ? "@" + data.getUsername() : "";
        originalBio = data.getBio() != null ? data.getBio() : "Any details such as age, occupation or city.\\\\nExample: 23 y.o. designer from San Francisco";
        phoneValueLabel.setText(data.getPhoneNumber());
        updateUIFields();

        // TODO: Load profile picture from data.getProfilePictureId()
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
        String newBio = bioTextArea.getText();

        if (!Objects.equals(originalBio, newBio)) {
            Task<RpcResponse<Object>> setBioTask = new Task<>() {
                @Override
                protected RpcResponse<Object> call() throws Exception {
                    return rpcCaller.setBio(newBio);
                }
            };

            setBioTask.setOnSucceeded(event -> {
                RpcResponse<Object> response = setBioTask.getValue();
                if (response.getStatusCode() == StatusCode.OK) {
                    originalBio = newBio;
                    System.out.println("Bio updated successfully.");
                } else {
                    System.err.println("Failed to update bio: " + response.getMessage());
                    Platform.runLater(() -> bioTextArea.setText(originalBio));
                }
            });

            setBioTask.setOnFailed(event -> {
                System.err.println("Task failed to update bio.");
                setBioTask.getException().printStackTrace();
                Platform.runLater(() -> bioTextArea.setText(originalBio));
            });

            new Thread(setBioTask).start();
        }
        System.out.println("Changes saved!");
    }


    public void discardChanges() {
        // Reset UI fields to original values
        updateUIFields();
        bioTextArea.setText(originalBio);
    }

    @FXML
    private void handleChangePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(dialogStage);

        if (selectedFile != null) {
            changePhotoButton.setDisable(true);
            IProgressListener listener = (transferred, total) -> {
                double frac = (total > 0) ? ((double) transferred / total) : 0;
                int percentage = (int) (frac * 100);
                Platform.runLater(() -> {
                    if (dialogStage != null) {
                        dialogStage.setTitle("Uploading... " + percentage + "%");
                    }
                });
            };

            ExecutorService backgroundExecutor = connectionManager.getClient().getBackgroundExecutor();
            backgroundExecutor.submit(() -> {
                try {
                    FileInfoModel info = connectionManager.getClient().getFileTransferManager().initiateUpload(selectedFile);
                    String fileId = info.FileId;
                    CreateMediaInputModel createMediaInput = new CreateMediaInputModel(
                            UUID.fromString(fileId),
                            selectedFile.getName(),
                            selectedFile.length(),
                            FileUtil.getFileExtension(selectedFile)
                    );
                    RpcResponse<UUID> createMediaResponse = rpcCaller.createMediaEntry(createMediaInput);

                    if (createMediaResponse.getStatusCode() != StatusCode.OK) {
                        System.err.println("Failed to create media entry for profile photo: " + createMediaResponse.getMessage());
                        Platform.runLater(this::restoreUIOnFailure);
                        return;
                    }

                    UploadTask uploadTask = new UploadTask(connectionManager.getClient().getFileTransferManager(), info, selectedFile, listener);
                    connectionManager.getClient().registerTask(fileId, uploadTask);

                    uploadTask.setOnSucceeded(e -> {
                        try {
                            String mediaId = createMediaResponse.getPayload().toString();
                            setProfilePictureOnServer(mediaId, selectedFile);
                        } catch (Exception ex) {
                            System.err.println("Error setting profile picture after upload: " + ex.getMessage());
                            Platform.runLater(this::restoreUIOnFailure);
                        } finally {
                            connectionManager.getClient().unregisterTask(fileId);
                        }
                    });

                    uploadTask.setOnFailed(e -> Platform.runLater(() -> {
                        connectionManager.getClient().unregisterTask(fileId);
                        restoreUIOnFailure();
                    }));

                    uploadTask.setOnCancelled(e -> Platform.runLater(() -> {
                        connectionManager.getClient().unregisterTask(fileId);
                        restoreUIOnFailure();
                    }));

                    backgroundExecutor.submit(uploadTask);

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        System.err.println("Error initiating upload: " + e.getMessage());
                        restoreUIOnFailure();
                    });
                }
            });
        }
    }
    private void setProfilePictureOnServer(String mediaId, File imageFile) {
        SetProfilePictureInputModel model = new SetProfilePictureInputModel(mediaId);
        Task<RpcResponse<Object>> setProfilePictureTask = new Task<>() {
            @Override
            protected RpcResponse<Object> call() throws Exception {
                return rpcCaller.setProfilePicture(model);
            }
        };

        setProfilePictureTask.setOnSucceeded(event -> {
            RpcResponse<Object> response = setProfilePictureTask.getValue();
            if (response.getStatusCode() == StatusCode.OK) {
                Platform.runLater(() -> {
                    Image newImage = new Image(imageFile.toURI().toString());
                    profilePictureImage.setImage(newImage);
                    System.out.println("Profile picture updated successfully.");
                    restoreUIOnSuccess();
                });
            } else {
                System.err.println("Failed to set profile picture on server: " + response.getMessage());
                Platform.runLater(this::restoreUIOnFailure);
            }
        });

        setProfilePictureTask.setOnFailed(event -> {
            System.err.println("Task to set profile picture failed.");
            setProfilePictureTask.getException().printStackTrace();
            Platform.runLater(this::restoreUIOnFailure);
        });

        new Thread(setProfilePictureTask).start();
    }
    private void restoreUIOnSuccess() {
        if (dialogStage != null) {
            dialogStage.setTitle("Settings");
        }
        changePhotoButton.setDisable(false);
    }
    private void restoreUIOnFailure() {
        if (dialogStage != null) {
            dialogStage.setTitle("Settings");
        }
        changePhotoButton.setDisable(false);
        System.err.println("Profile picture update failed.");
    }

    public void updateName(String firstName, String lastName) {
        UpdateNameInputModel model = new UpdateNameInputModel(firstName, lastName);
        Task<RpcResponse<Object>> updateNameTask = new Task<>() {
            @Override
            protected RpcResponse<Object> call() throws Exception {
                return rpcCaller.updateName(model);
            }
        };

        updateNameTask.setOnSucceeded(event -> {
            RpcResponse<Object> response = updateNameTask.getValue();
            if (response.getStatusCode() == StatusCode.OK) {
                Platform.runLater(() -> {
                    this.originalFirstName = firstName;
                    this.originalLastName = lastName;
                    updateUIFields();
                    if (parentController != null) {
                        parentController.updateDisplayNameOnHeader(firstName + " " + lastName);
                    }
                    System.out.println("Name updated to: " + firstName + " " + lastName);
                });
            } else {
                System.err.println("Failed to update name: " + response.getMessage());
            }
        });

        updateNameTask.setOnFailed(event -> {
            System.err.println("Task failed to update name.");
            updateNameTask.getException().printStackTrace();
        });

        new Thread(updateNameTask).start();
    }

    public void updateUsername(String username) {
        Task<RpcResponse<Object>> setUsernameTask = new Task<>() {
            @Override
            protected RpcResponse<Object> call() throws Exception {
                return rpcCaller.setUsername(username);
            }
        };

        setUsernameTask.setOnSucceeded(event -> {
            RpcResponse<Object> response = setUsernameTask.getValue();
            if (response.getStatusCode() == StatusCode.OK) {
                Platform.runLater(() -> {
                    this.originalUsername = "@" + username;
                    updateUIFields();
                    if (parentController != null) {
                        parentController.updateUsernameOnHeader(this.originalUsername);
                    }
                    System.out.println("Username updated to: " + username);
                });
            } else {
                System.err.println("Failed to update username: " + response.getMessage());
            }
        });

        setUsernameTask.setOnFailed(event -> {
            System.err.println("Task failed to update username.");
            setUsernameTask.getException().printStackTrace();
        });

        new Thread(setUsernameTask).start();
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
            // Strip the '@' prefix before passing to the dialog
            String currentUsername = originalUsername.startsWith("@") ? originalUsername.substring(1) : originalUsername;
            SceneUtil.createDialog("/Client/fxml/editUsernameDialog.fxml", dialogStage, this, currentUsername, "Edit Username").showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}