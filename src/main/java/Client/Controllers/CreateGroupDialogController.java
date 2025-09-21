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
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader; // اضافه شده
import javafx.scene.image.WritableImage; // اضافه شده
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle; // اضافه شده
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO; // اضافه شده
import javax.imageio.stream.ImageInputStream; // اضافه شده
import java.awt.image.BufferedImage; // اضافه شده
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
    private Image currentDisplayedImage;

    public void init(MainChatController mainChatController, Stage parentStage) {
        this.mainChatController = mainChatController;
        this.parentStage = parentStage;
        setupUI();
    }

    private void setupUI() {
        profilePicturePane.setOnMouseClicked(event -> handleChooseAvatar());
    }

    private void handleChooseAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Group Avatar");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(dialogRoot.getScene().getWindow());

        if (selectedFile != null) {
            System.out.println("File selected: " + selectedFile.getAbsolutePath());
            long maxSizeBytes = 10L * 1024 * 1024;
            long fileSizeBytes = selectedFile.length();

            if (fileSizeBytes <= maxSizeBytes) {
                ImageInputStream inputStream = null;
                try {
                    inputStream = ImageIO.createImageInputStream(selectedFile);
                    if (inputStream == null) {
                        System.err.println("Failed to create ImageInputStream for file: " + selectedFile.getAbsolutePath());
                        return;
                    }
                    BufferedImage bufferedImage = ImageIO.read(inputStream);
                    if (bufferedImage == null) {
                        System.err.println("Failed to read image from file: " + selectedFile.getAbsolutePath());
                        return;
                    }
                    double originalWidth = bufferedImage.getWidth();
                    double originalHeight = bufferedImage.getHeight();
                    if (originalWidth <= 0 || originalHeight <= 0) {
                        System.err.println("Invalid image dimensions: " + originalWidth + "x" + originalHeight);
                        return;
                    }

                    Image originalImage = new Image(selectedFile.toURI().toString());
                    if (originalImage.isError()) {
                        System.err.println("Failed to load image into JavaFX: " + originalImage.getException().getMessage());
                        return;
                    }

                    Image imageToRestoreOnFailure = originalImage;

                    double targetWidth = profileImageView.getFitWidth(); // 80.0
                    double targetHeight = profileImageView.getFitHeight(); // 80.0
                    double targetAspectRatio = targetWidth / targetHeight;

                    PixelReader pixelReader = originalImage.getPixelReader();
                    if (pixelReader != null) {
                        double imageAspectRatio = originalWidth / originalHeight;

                        double cropWidth, cropHeight;
                        if (imageAspectRatio > targetAspectRatio) {
                            cropHeight = originalHeight;
                            cropWidth = originalHeight * targetAspectRatio;
                        } else {
                            cropWidth = originalWidth;
                            cropHeight = originalWidth / targetAspectRatio;
                        }

                        double startX = (originalWidth - cropWidth) / 2;
                        double startY = (originalHeight - cropHeight) / 2;

                        WritableImage croppedImage = new WritableImage(pixelReader, (int) startX, (int) startY, (int) cropWidth, (int) cropHeight);

                        profileImageView.setImage(croppedImage);
                        profileImageView.setPreserveRatio(false);
                        currentDisplayedImage = croppedImage;
                        System.out.println("Image loaded successfully with centered crop and fixed circular clip. Original Size: " + originalWidth + "x" + originalHeight + ", Cropped Size: " + cropWidth + "x" + cropHeight);

                    } else {
                        profileImageView.setImage(originalImage);
                        profileImageView.setPreserveRatio(true);
                        currentDisplayedImage = originalImage;
                        System.out.println("PixelReader is null, using original image without crop. Original Size: " + originalWidth + "x" + originalHeight);
                    }

                    selectedAvatarFile = selectedFile;
                    uploadedAvatarMediaId = null;

                    nextButton.setDisable(true);
                    cancelButton.setDisable(true);

                    IProgressListener listener = (transferred, total) -> {
                        double frac = (total > 0) ? ((double) transferred / total) : 0;
                        int percentage = (int) (frac * 100);
                        Platform.runLater(() -> {
                            if (dialogRoot.getScene() != null && dialogRoot.getScene().getWindow() instanceof Stage) {
                                Stage stage = (Stage) dialogRoot.getScene().getWindow();
                                if (stage.getScene().getRoot() == dialogRoot) {
                                    stage.setTitle("Uploading Avatar... " + percentage + "%");
                                }
                            }
                        });
                    };

                    ConnectionManager connectionManager = AppConnectionManager.getInstance().getConnectionManager();
                    RpcCaller rpcCaller = AppConnectionManager.getInstance().getRpcCaller();
                    var app = connectionManager.getClient();
                    ExecutorService backgroundExecutor = app.getBackgroundExecutor();

                    backgroundExecutor.submit(() -> {
                        try {
                            FileInfoModel info = app.getFileTransferManager().initiateUpload(selectedFile);
                            String fileId = info.FileId;
                            CreateMediaInputModel createMediaInput = new CreateMediaInputModel(
                                    UUID.fromString(fileId),
                                    selectedFile.getName(),
                                    selectedFile.length(),
                                    getFileExtension(selectedFile)
                            );
                            RpcResponse<UUID> createMediaResponse = rpcCaller.createMediaEntry(createMediaInput);

                            if (createMediaResponse.getStatusCode() != StatusCode.OK || createMediaResponse.getPayload() == null) {
                                System.err.println("Failed to create media entry for group avatar: " + createMediaResponse.getMessage());
                                Platform.runLater(() -> restoreUIOnFailure(imageToRestoreOnFailure));
                                return;
                            } else {
                                System.out.println("Success creating media entry with ID: " + fileId);
                            }
                            UUID mediaId = createMediaResponse.getPayload();
                            uploadedAvatarMediaId = mediaId.toString();

                            UploadTask uploadTask = new UploadTask(app.getFileTransferManager(), info, selectedFile, listener);
                            app.registerTask(fileId, uploadTask);

                            uploadTask.setOnSucceeded(e -> {
                                Platform.runLater(() -> {
                                    app.unregisterTask(fileId);
                                    System.out.println("Avatar upload successful. Media ID: " + uploadedAvatarMediaId);
                                    restoreUIOnSuccess();
                                    nextButton.setDisable(false);
                                    cancelButton.setDisable(false);
                                });
                            });

                            uploadTask.setOnFailed(e -> {
                                Platform.runLater(() -> {
                                    app.unregisterTask(fileId);
                                    e.getSource().getException().printStackTrace();
                                    AlertUtil.showError("Avatar upload failed.");
                                    restoreUIOnFailure(imageToRestoreOnFailure);
                                });
                            });

                            uploadTask.setOnCancelled(e -> {
                                Platform.runLater(() -> {
                                    app.unregisterTask(fileId);
                                    AlertUtil.showWarning("Avatar upload cancelled.");
                                    restoreUIOnFailure(imageToRestoreOnFailure);
                                });
                            });

                            backgroundExecutor.submit(uploadTask);

                        } catch (Exception e) {
                            Platform.runLater(() -> {
                                System.err.println("Error initiating avatar upload: " + e.getMessage());
                                AlertUtil.showError("Error starting upload: " + e.getMessage());
                                restoreUIOnFailure(imageToRestoreOnFailure);
                            });
                        }
                    });

                } catch (IOException e) {
                    System.err.println("Error loading or processing image: " + e.getMessage());
                    AlertUtil.showError("Error loading or processing image: " + e.getMessage());
                    restoreUIOnFailure(currentDisplayedImage);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                AlertUtil.showError("File size exceeds 10 MB limit. Please select a smaller image.");
                selectedAvatarFile = null;
            }
        } else {
            System.out.println("No file selected or dialog cancelled.");
        }
    }

    @FXML
    private void handleNext() {
        String groupName = groupNameField.getText().trim();
        if (groupName.isEmpty()) {
            AlertUtil.showError("Group name cannot be empty.");
            return;
        }

        if (selectedAvatarFile != null && uploadedAvatarMediaId == null) {
            AlertUtil.showError("Please wait for avatar upload to complete or select an avatar.");
            return;
        }
        proceedToAddMembers(groupName, uploadedAvatarMediaId);
    }

    private void proceedToAddMembers(String groupName, String profilePictureId) {
        try {
            URL fxmlUrl = getClass().getResource("/Client/fxml/addMembersDialog.fxml");
            if (fxmlUrl == null) {
                throw new IOException("FXML file not found: /Client/fxml/addMembersDialog.fxml. Check your resources path and build configuration.");
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent addMembersRoot = loader.load();

            AddMembersDialogController controller = loader.getController();
            controller.init(mainChatController, groupName, profilePictureId);

            Stage stage = (Stage) dialogRoot.getScene().getWindow();
            stage.getScene().setRoot(addMembersRoot);

            stage.sizeToScene();
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Could not open the add members window: " + e.getMessage());
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

    private void restoreUIOnSuccess() {
        if (dialogRoot.getScene() != null && dialogRoot.getScene().getWindow() instanceof Stage) {
            Stage stage = (Stage) dialogRoot.getScene().getWindow();
            stage.setTitle("New Group");
        }
        nextButton.setDisable(false);
        cancelButton.setDisable(false);
    }

    private void restoreUIOnFailure(Image imageToRestore) {
        if (dialogRoot.getScene() != null && dialogRoot.getScene().getWindow() instanceof Stage) {
            Stage stage = (Stage) dialogRoot.getScene().getWindow();
            stage.setTitle("New Group");
        }
        profileImageView.setImage(imageToRestore);
        currentDisplayedImage = imageToRestore;
        uploadedAvatarMediaId = null;
        selectedAvatarFile = null;
        nextButton.setDisable(false);
        cancelButton.setDisable(false);
    }
}