package Client.Controllers;

import Client.AppConnectionManager;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static JSocket2.Utils.FileUtil.getFileExtension;

public class CreateChannelDialogController {

    @FXML private VBox dialogRoot;
    @FXML private StackPane profilePicturePane;
    @FXML private ImageView profileImageView;
    @FXML private TextField channelNameField;
    @FXML private TextArea descriptionArea;
    @FXML private Label charCountLabel;
    @FXML private Button cancelButton;
    @FXML private Button createButton;

    private Client.Controllers.MainChatController mainChatController; // Explicitly use fully qualified type
    private Stage dialogStage;
    private File selectedAvatarFile;
    private String uploadedAvatarMediaId = null;

    // Method to set the dialog stage, required by SceneUtil.createDialog
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void init(Client.Controllers.MainChatController mainChatController, Stage parentStage) {
        if (mainChatController == null) {
            throw new IllegalArgumentException("MainChatController cannot be null");
        }
        this.mainChatController = mainChatController;
        this.dialogStage = parentStage;
        setupUI();
    }

    private void setupUI() {
        // Use a channel-specific default avatar
        Image defaultAvatar = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Client/images/channel-icon.png")));
        profileImageView.setImage(defaultAvatar);
        profilePicturePane.setOnMouseClicked(event -> handleChooseAvatar());

        // Setup character count listener for description
        descriptionArea.textProperty().addListener((observable, oldValue, newValue) -> {
            int length = newValue.length();
            charCountLabel.setText(length + "/255");
            if (length > 255) {
                descriptionArea.setText(oldValue);
                charCountLabel.setText("255/255");
            }
        });
    }

    private void handleChooseAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Channel Avatar");
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
    private void handleCreate() {
        String channelName = channelNameField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (channelName.isEmpty()) {
            AlertUtil.showError("Channel name cannot be empty.");
            return;
        }

        createButton.setDisable(true);
        cancelButton.setDisable(true);

        if (selectedAvatarFile != null) {
            uploadAvatarAndProceed(channelName, description);
        } else {
            proceedToAddMembers(channelName, description, null);
        }
    }

    private void uploadAvatarAndProceed(String channelName, String description) {
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
                        createButton.setDisable(false);
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
                    Platform.runLater(() -> proceedToAddMembers(channelName, description, uploadedAvatarMediaId));
                });

                uploadTask.setOnFailed(failEvent -> {
                    app.unregisterTask(fileId);
                    failEvent.getSource().getException().printStackTrace();
                    Platform.runLater(() -> {
                        AlertUtil.showError("Avatar upload failed.");
                        createButton.setDisable(false);
                        cancelButton.setDisable(false);
                    });
                });

                backgroundExecutor.submit(uploadTask);

            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    AlertUtil.showError("Error starting upload: " + ex.getMessage());
                    createButton.setDisable(false);
                    cancelButton.setDisable(false);
                });
            }
        });
    }

    private void proceedToAddMembers(String channelName, String description, String profilePictureId) {
        if (mainChatController == null) {
            Platform.runLater(() -> {
                AlertUtil.showError("MainChatController is not initialized.");
                createButton.setDisable(false);
                cancelButton.setDisable(false);
            });
            return;
        }

        try {
            URL fxmlUrl = getClass().getResource("/Client/fxml/addMembersDialog.fxml");
            if (fxmlUrl == null) {
                throw new IOException("FXML file not found: /Client/fxml/addMembersDialog.fxml. Check your resources path and build configuration.");
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent addMembersRoot = loader.load();

            AddMembersDialogController controller = loader.getController();
            controller.init(mainChatController, channelName, profilePictureId); // TODO: Add description parameter

            Stage stage = (Stage) dialogRoot.getScene().getWindow();
            stage.getScene().setRoot(addMembersRoot);

            stage.sizeToScene();
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                AlertUtil.showError("Could not open the add members window: " + e.getMessage());
                createButton.setDisable(false);
                cancelButton.setDisable(false);
            });
        }
    }

    @FXML
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}