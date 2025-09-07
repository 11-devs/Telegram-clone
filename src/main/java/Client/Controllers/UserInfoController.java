package Client.Controllers;

import Client.AccessKeyManager;
import Client.AppConnectionManager;
import Client.RpcCaller;
import Client.Tasks.UploadTask;
import JSocket2.Core.Client.ConnectionManager;
import JSocket2.Protocol.Rpc.RpcResponse;
import JSocket2.Protocol.StatusCode;
import JSocket2.Protocol.Transfer.FileInfoModel;
import JSocket2.Protocol.Transfer.IProgressListener;
import JSocket2.Utils.FileUtil;
import Shared.Api.Models.AccountController.BasicRegisterInputModel;
import Shared.Api.Models.AccountController.BasicRegisterOutputModel;
import Shared.Api.Models.AccountController.VerifyCodeInputModel;
import Shared.Api.Models.AccountController.VerifyCodeOutputModel;
import Shared.Api.Models.MediaController.CreateMediaInputModel;
import Shared.Utils.DeviceUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static Shared.Utils.SceneUtil.changeScene;
import static Shared.Utils.SceneUtil.changeSceneWithSameSize;

public class UserInfoController implements Initializable {
    private String phoneNumber;
    private String profilePhotoId;
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    private ConnectionManager connectionManager;
    private RpcCaller rpcCaller;
    @FXML
    private VBox root;
    @FXML
    public VBox infoBox;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private ImageView photoPreview;
    @FXML
    public Button signUpButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connectionManager = AppConnectionManager.getInstance().getConnectionManager();
        rpcCaller = AppConnectionManager.getInstance().getRpcCaller();
        System.out.println("Controller initialized successfully");
        // Animation of moving from a little further right to the main target
        if (infoBox != null) {
            infoBox.setTranslateX(75);
            var transition = new javafx.animation.TranslateTransition(javafx.util.Duration.seconds(0.5), infoBox);
            transition.setToX(0);
            transition.setAutoReverse(false);
            transition.setCycleCount(1);
            transition.play();
        }
        // Animation of moving from a little further lower to the main target
        if (signUpButton != null) {
            signUpButton.setTranslateY(50);
            var transition = new javafx.animation.TranslateTransition(javafx.util.Duration.seconds(0.5), signUpButton);
            transition.setToY(0);
            transition.setAutoReverse(false);
            transition.setCycleCount(1);
            transition.play();
        }
        firstNameField.requestFocus(); // Initial focus on first name field
    }

    @FXML
    private void handleSettings() {
        // TODO: develop setting dialog
    }

    @FXML
    private void handleBack() {
        changeSceneWithSameSize(root, "/Client/fxml/phoneNumber.fxml");
    }

    @FXML
    private void handleSignUp() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        if (firstName != null && !firstName.isEmpty() && lastName != null && !lastName.isEmpty()) {
            System.out.println("Sign Up details - First Name: " + firstName + ", Last Name: " + lastName);
            // TODO: Add sign-up logic (e.g., photo upload, validation)
            // Example: changeSceneWithSameSize(root, "Client/fxml/verificationViaSms.fxml");
            Task<RpcResponse<BasicRegisterOutputModel>> basicRegisterTask = new Task<>() {
                @Override
                protected RpcResponse<BasicRegisterOutputModel> call() throws Exception {
                    var deviceInfo = DeviceUtil.getDeviceInfo();
                    return rpcCaller.basicRegister(new BasicRegisterInputModel(phoneNumber,firstName,lastName,profilePhotoId,deviceInfo));
                }
            };
            basicRegisterTask.setOnSucceeded(event -> {
                try {
                    var response = basicRegisterTask.getValue();
                    if(response.getStatusCode() == StatusCode.OK){
                        var resultCode = AccessKeyManager.LoginWithAccessKey(response.getPayload().getAccessKey(),connectionManager.getClient());
                        if (resultCode == StatusCode.OK) {
                            System.out.println("Successful login");
                            changeScene(root, "/Client/fxml/mainChat.fxml", (MainChatController controller) -> {}, true);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                }
            });
            basicRegisterTask.setOnFailed(event -> {
                System.out.println("Task failed.");
                basicRegisterTask.getException().printStackTrace();
            });

            // Start the background task
            new Thread(basicRegisterTask).start();
        } else {
            System.out.println("Please fill all fields.");
        }
    }

    @FXML
    private void handleUploadPhoto(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(photoPreview.getScene().getWindow());
        if (selectedFile != null) {
            System.out.println("File selected: " + selectedFile.getAbsolutePath());
            long maxSizeBytes = 10L * 1024 * 1024; // 10 MB
            long fileSizeBytes = selectedFile.length();
            if (fileSizeBytes <= maxSizeBytes) {
                ImageInputStream inputStream = null;
                try {
                    // Load image using ImageIO to check dimensions
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

                    // Load into JavaFX Image
                    Image originalImage = new Image(selectedFile.toURI().toString());
                    if (originalImage.isError()) {
                        System.err.println("Failed to load image into JavaFX: " + originalImage.getException().getMessage());
                        return;
                    }

                    // Set the original image to photoPreview if cropping fails
                    photoPreview.setImage(originalImage);
                    photoPreview.setFitWidth(171.0); // Fixed width
                    photoPreview.setFitHeight(168.0); // Fixed height
                    photoPreview.setPreserveRatio(true); // Keep aspect ratio

                    // Apply circular clip with fixed radius
                    Circle clip = new Circle();
                    clip.setCenterX(85.5); // Center based on FXML design
                    clip.setCenterY(84.0); // Center based on FXML design
                    clip.setRadius(84.0); // Fixed radius to match FXML design

                    // Apply the clip to photoPreview
                    photoPreview.setClip(clip);

                    // Try cropping if PixelReader is available
                    javafx.scene.image.PixelReader pixelReader = originalImage.getPixelReader();
                    if (pixelReader != null) {
                        // Calculate the target aspect ratio (171:168 ≈ 1.019)
                        double targetAspectRatio = 171.0 / 168.0;
                        double imageAspectRatio = originalWidth / originalHeight;

                        // Determine the crop dimensions to match the target aspect ratio
                        double cropWidth, cropHeight;
                        if (imageAspectRatio > targetAspectRatio) {
                            // Image is wider than target, crop width
                            cropWidth = originalHeight * targetAspectRatio;
                            cropHeight = originalHeight;
                        } else {
                            // Image is taller than target, crop height
                            cropWidth = originalWidth;
                            cropHeight = originalWidth / targetAspectRatio;
                        }

                        // Calculate the starting points for centered crop
                        double startX = (originalWidth - cropWidth) / 2;
                        double startY = (originalHeight - cropHeight) / 2;

                        // Create a cropped image
                        javafx.scene.image.WritableImage croppedImage = new javafx.scene.image.WritableImage(pixelReader, (int)startX, (int)startY, (int)cropWidth, (int)cropHeight);

                        // Update photoPreview with cropped image
                        photoPreview.setImage(croppedImage);
                        photoPreview.setPreserveRatio(false); // Allow fitting to fixed size

                        System.out.println("Image loaded successfully with zoom, centered crop, and fixed circular clip. Original Size: " + originalWidth + "x" + originalHeight + ", Cropped Size: " + cropWidth + "x" + cropHeight);

                    } else {
                        System.out.println("PixelReader is null, using original image without crop. Original Size: " + originalWidth + "x" + originalHeight);
                    }
                    IProgressListener listener = (transferred, total) -> {
                        double frac = (total > 0) ? ((double) transferred / total) : 0;
                        int percentage = (int) (frac * 100);
                        Platform.runLater(() -> {
                            // Update the title bar to show upload progress
                            if (root.getScene() != null && root.getScene().getWindow() instanceof Stage) {
                                Stage stage = (Stage) root.getScene().getWindow();
                                stage.setTitle("Uploading... " + percentage + "%");
                            }
                        });
                    };
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
                                    FileUtil.getFileExtension(selectedFile)
                            );
                            RpcResponse<UUID> createMediaResponse = rpcCaller.createMediaEntry(createMediaInput);

                            if (createMediaResponse.getStatusCode() != StatusCode.OK) {
                                System.err.println("Failed to create media entry for profile photo: " + createMediaResponse.getMessage());
                                Platform.runLater(this::restoreUIOnFailure);
                                return;
                            }else{
                                System.out.println("success create media id: " + fileId);
                            }

                            UploadTask uploadTask = new UploadTask(app.getFileTransferManager(), info, selectedFile, listener);
                            app.registerTask(fileId, uploadTask);

                            uploadTask.setOnSucceeded(e -> {
                                Platform.runLater(() -> {
                                    try {
                                        photoPreview.setImage(originalImage);
                                        profilePhotoId = createMediaResponse.getPayload().toString();
                                        signUpButton.setDisable(false);
                                        restoreUIOnSuccess();
                                    } catch (Exception ex) {
                                        System.err.println("Error updating UI after upload success: " + ex.getMessage());
                                        restoreUIOnFailure();
                                    } finally {
                                        app.unregisterTask(fileId);
                                    }
                                });
                            });

                            uploadTask.setOnFailed(e -> {
                                Platform.runLater(() -> {
                                    app.unregisterTask(fileId);
                                    restoreUIOnFailure();
                                });
                            });

                            uploadTask.setOnCancelled(e -> {
                                Platform.runLater(() -> {
                                    app.unregisterTask(fileId);
                                    restoreUIOnFailure();
                                });
                            });

                            backgroundExecutor.submit(uploadTask);

                        } catch (Exception e) {
                            Platform.runLater(() -> {
                                System.err.println("Error initiating upload: " + e.getMessage());
                                restoreUIOnFailure();
                            });
                        }
                    });
                } catch (IOException e) {
                    System.err.println("Error loading or processing image: " + e.getMessage());
                }
            } else {
                System.out.println("File size exceeds 10 MB limit. Please select a smaller image.");
            }
        } else {
            System.out.println("No file selected or dialog cancelled.");
        }
    }
    private void restoreUIOnSuccess() {
        if (root.getScene() != null && root.getScene().getWindow() instanceof Stage) {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setTitle("Telegram Desktop");
        }
    }

    private void restoreUIOnFailure() {
        if (root.getScene() != null && root.getScene().getWindow() instanceof Stage) {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setTitle("Telegram Desktop");
        }
        Image defaultImage = new Image(getClass().getResourceAsStream("/Client/images/11Devs-white.png"));
        photoPreview.setImage(defaultImage);
        profilePhotoId = null;
        signUpButton.setDisable(false);
    }
}