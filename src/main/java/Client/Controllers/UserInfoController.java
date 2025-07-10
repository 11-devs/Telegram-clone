package Client.Controllers;

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

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static Shared.Utils.SceneUtil.changeSceneWithSameSize;

public class UserInfoController implements Initializable {

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
            signUpButton.setTranslateY(75);
            var transition = new javafx.animation.TranslateTransition(javafx.util.Duration.seconds(0.5), signUpButton);
            transition.setToY(0);
            transition.setAutoReverse(false);
            transition.setCycleCount(1);
            transition.play();
        }
        firstNameField.requestFocus(); // Initial focus on first name field
    }

    @FXML
    private void handleSignUp() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        if (firstName != null && !firstName.isEmpty() && lastName != null && !lastName.isEmpty()) {
            System.out.println("Sign Up details - First Name: " + firstName + ", Last Name: " + lastName);
            // TODO: Add sign-up logic (e.g., photo upload, validation)
            // Example: changeSceneWithSameSize(root, "Client/fxml/verificationViaSms.fxml");
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
                // Load image
                Image originalImage = new Image(selectedFile.toURI().toString());
                double originalWidth = originalImage.getWidth();
                double originalHeight = originalImage.getHeight();

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

                // Create a cropped image (zoom effect)
                javafx.scene.image.PixelReader pixelReader = originalImage.getPixelReader();
                javafx.scene.image.WritableImage croppedImage = new javafx.scene.image.WritableImage(pixelReader, (int)startX, (int)startY, (int)cropWidth, (int)cropHeight);

                // Set the cropped image to photoPreview
                photoPreview.setImage(croppedImage);
                photoPreview.setFitWidth(171.0); // Fixed width
                photoPreview.setFitHeight(168.0); // Fixed height
                photoPreview.setPreserveRatio(false); // Allow fitting to fixed size

                // Apply circular clip with fixed radius
                Circle clip = new Circle();
                clip.setCenterX(85.5); // Center based on FXML design
                clip.setCenterY(84.0); // Center based on FXML design
                clip.setRadius(84.0); // Fixed radius to match FXML design

                // Apply the clip to photoPreview
                photoPreview.setClip(clip);

                System.out.println("Image loaded successfully with zoom, centered crop, and fixed circular clip. Original Size: " + originalWidth + "x" + originalHeight + ", Cropped Size: " + cropWidth + "x" + cropHeight);
            } else {
                System.out.println("File size exceeds 10 MB limit. Please select a smaller image.");
            }
        } else {
            System.out.println("No file selected or dialog cancelled.");
        }
    }
}