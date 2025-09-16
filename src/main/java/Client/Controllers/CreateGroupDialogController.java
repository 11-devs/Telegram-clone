package Client.Controllers;

import Client.AppConnectionManager; // <-- IMPORT THE SINGLETON
import Client.RpcCaller;
import Client.Tasks.UploadTask;
import JSocket2.Core.Client.ConnectionManager;
import JSocket2.Protocol.Rpc.RpcResponse;
import JSocket2.Protocol.StatusCode;
import JSocket2.Protocol.Transfer.FileInfoModel;
import JSocket2.Protocol.Transfer.IProgressListener;
import Shared.Api.Models.MediaController.CreateMediaInputModel;
import Shared.Utils.AlertUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static JSocket2.Utils.FileUtil.getFileExtension;

public class CreateGroupDialogController {

    @FXML private VBox dialogRoot;
    @FXML private StackPane profilePicturePane;
    @FXML private ImageView profileImageView;
    @FXML private TextField groupNameField;
    @FXML private Button cancelButton;
    @FXML private Button nextButton;

    private MainChatController mainChatController;
    private Stage parentStage;
    private File selectedAvatarFile;
    private String uploadedAvatarMediaId = null;

    public void init(MainChatController mainChatController, Stage parentStage) {
        this.mainChatController = mainChatController;
        this.parentStage = parentStage;
        setupUI();
    }

    private void setupUI() {
        // Use a more distinct default avatar for groups
        Image defaultAvatar = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Client/images/group-icon.png")));
        profileImageView.setImage(defaultAvatar);
        profilePicturePane.setOnMouseClicked(event -> handleChooseAvatar());
    }

    private void handleChooseAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Group Avatar");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        selectedAvatarFile = fileChooser.showOpenDialog(dialogRoot.getScene().getWindow());

        if (selectedAvatarFile != null) {
            Image image = new Image(selectedAvatarFile.toURI().toString());
            profileImageView.setImage(image);
            uploadedAvatarMediaId = null; // Reset until successfully uploaded
        }
    }

    @FXML
    private void handleNext() {
        String groupName = groupNameField.getText().trim();
        if (groupName.isEmpty()) {
            AlertUtil.showError("Group name cannot be empty.");
            return;
        }

        nextButton.setDisable(true);
        cancelButton.setDisable(true);

        if (selectedAvatarFile != null) {
            uploadAvatarAndProceed(groupName);
        } else {
            proceedToAddMembers(groupName, null);
        }
    }

    private void uploadAvatarAndProceed(String groupName) {
        // FIX: Get ConnectionManager and RpcCaller from the AppConnectionManager singleton
        ConnectionManager connectionManager = AppConnectionManager.getInstance().getConnectionManager();
        RpcCaller rpcCaller = AppConnectionManager.getInstance().getRpcCaller();

        var app = connectionManager.getClient();
        ExecutorService backgroundExecutor = app.getBackgroundExecutor();

        backgroundExecutor.submit(() -> {
            try {
                FileInfoModel info = app.getFileTransferManager().initiateUpload(selectedAvatarFile);
                String fileId = info.FileId;

                CreateMediaInputModel createMediaInput = new CreateMediaInputModel(
                        UUID.fromString(fileId),
                        selectedAvatarFile.getName(),
                        selectedAvatarFile.length(),
                        getFileExtension(selectedAvatarFile)
                );
                RpcResponse<UUID> createMediaResponse = rpcCaller.createMediaEntry(createMediaInput);

                if (createMediaResponse.getStatusCode() != StatusCode.OK || createMediaResponse.getPayload() == null) {
                    Platform.runLater(() -> {
                        AlertUtil.showError("Failed to prepare avatar upload: " + createMediaResponse.getMessage());
                        nextButton.setDisable(false);
                        cancelButton.setDisable(false);
                    });
                    return;
                }
                UUID mediaId = createMediaResponse.getPayload();
                uploadedAvatarMediaId = mediaId.toString();

                IProgressListener listener = (transferred, total) -> {}; // No-op listener
                UploadTask uploadTask = new UploadTask(app.getFileTransferManager(), info, selectedAvatarFile, listener);
                app.registerTask(fileId, uploadTask);

                uploadTask.setOnSucceeded(e -> {
                    app.unregisterTask(fileId);
                    System.out.println("Upload successful. Media ID: " + uploadedAvatarMediaId);
                    Platform.runLater(() -> proceedToAddMembers(groupName, uploadedAvatarMediaId));
                });

                uploadTask.setOnFailed(failEvent -> {
                    app.unregisterTask(fileId);
                    failEvent.getSource().getException().printStackTrace();
                    Platform.runLater(() -> {
                        AlertUtil.showError("Avatar upload failed.");
                        nextButton.setDisable(false);
                        cancelButton.setDisable(false);
                    });
                });

                backgroundExecutor.submit(uploadTask);

            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    AlertUtil.showError("Error starting upload: " + ex.getMessage());
                    nextButton.setDisable(false);
                    cancelButton.setDisable(false);
                });
            }
        });
    }


    private void proceedToAddMembers(String groupName, String profilePictureId) {
        try {
            // --- ROBUST FXML LOADING ---
            URL fxmlUrl = getClass().getResource("/Client/fxml/addMembersDialog.fxml");
            if (fxmlUrl == null) {
                // This will now be the error you see, which is much clearer.
                throw new IOException("FXML file not found: /Client/fxml/addMembersDialog.fxml. Check your resources path and build configuration.");
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent addMembersRoot = loader.load();

            AddMembersDialogController controller = loader.getController();
            controller.init(mainChatController, groupName, profilePictureId);

            // --- FIX: Replace the scene's root instead of closing and opening a new stage ---
            // This avoids destroying native window resources, which was causing the JVM crash.
            Stage stage = (Stage) dialogRoot.getScene().getWindow();
            stage.getScene().setRoot(addMembersRoot);

            // Recenter the stage as the new content might have a different size
            stage.sizeToScene();
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Could not open the add members window: " + e.getMessage());
            // Re-enable buttons if opening the next dialog failed
            Platform.runLater(() -> {
                nextButton.setDisable(false);
                cancelButton.setDisable(false);
            });
        }
    }
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) dialogRoot.getScene().getWindow();
        stage.close();
    }
}