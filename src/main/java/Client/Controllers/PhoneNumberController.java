package Client.Controllers;

import Client.AppConnectionManager;
import Client.RpcCaller;
import JSocket2.Core.Client.ConnectionManager;
import JSocket2.Protocol.Rpc.RpcResponse;
import JSocket2.Protocol.StatusCode;
import Shared.Api.Models.AccountController.RequestCodeOutputModel;
import Shared.Models.CountryCode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.util.converter.NumberStringConverter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

import static Shared.Utils.SceneUtil.changeSceneWithSameSize;

public class PhoneNumberController {
    private ConnectionManager connectionManager;
    private RpcCaller rpcCaller;
    @FXML
    private VBox root;

    @FXML
    public VBox infoBox;

    @FXML
    private TextField countryPreCode;

    @FXML
    private TextField phoneNumberField;

    @FXML
    private Button continueButton;

    @FXML
    private Button selectCountryButton;

    private ObservableList<CountryCode> allCountries;

    public void initialize() {
        // Animation for infoBox (moving from right to center)
        connectionManager = AppConnectionManager.getInstance().getConnectionManager();
        rpcCaller = AppConnectionManager.getInstance().getRpcCaller();
        if (infoBox != null) {
            infoBox.setTranslateX(75);
            TranslateTransition transition = new TranslateTransition(Duration.seconds(0.5), infoBox);
            transition.setToX(0);
            transition.setAutoReverse(false);
            transition.setCycleCount(1);
            transition.play();
        } else {
            System.err.println("infoBox is null! Check FXML binding.");
        }

        // Animation for continueButton (moving from bottom to center)
        if (continueButton != null) {
            continueButton.setTranslateY(75);
            TranslateTransition transition = new TranslateTransition(Duration.seconds(0.5), continueButton);
            transition.setToY(0);
            transition.setAutoReverse(false);
            transition.setCycleCount(1);
            transition.play();
        } else {
            System.err.println("continueButton is null! Check FXML binding.");
        }

        System.out.println("Initializing PhoneNumberController...");
        allCountries = loadCountryCodes();
        System.out.println("Loaded " + allCountries.size() + " country codes.");

        // Default
        countryPreCode.setText("+");
        selectCountryButton.setText("Country Code");

        // Limit countryPreCode to "+[numbers]" format (max 6 digits after +), keeping "+" non-removable
        countryPreCode.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            System.out.println("TextFormatter - New text: " + newText);

            if (newText.startsWith("+") && newText.substring(1).matches("\\d{0,6}")) {
                return change;
            } else if (!newText.startsWith("+")) {
                change.setText("+");
                change.setRange(0, change.getControlText().length());
                change.setCaretPosition(1);
                change.setAnchor(1);
                return change;
            }
            return null;
        }));

        // Add ChangeListener to dynamically update country name
        countryPreCode.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null && newValue.startsWith("+")) {
                    String code = newValue.replace("+", "").replace("-", "");
                    System.out.println("ChangeListener - Updating for code: " + code);
                    updateCountryName(code);
                }
            }
        });

        // Limit phoneNumberField to numbers (max 12 digits)
        phoneNumberField.setTextFormatter(new TextFormatter<>(new NumberStringConverter(), null, change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*") && newText.length() <= 12) {
                return change;
            }
            return null;
        }));
    }

    private ObservableList<CountryCode> loadCountryCodes() {
        ObservableList<CountryCode> codes = FXCollections.observableArrayList();
        Gson gson = new Gson();
        Type listType = new TypeToken<List<CountryCode>>(){}.getType();

        try (Reader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/client/json/country_codes.json")))) {
            List<CountryCode> countryList = gson.fromJson(reader, listType);
            if (countryList != null) {
                codes.addAll(countryList);
                countryList.forEach(c -> System.out.println("Loaded code: " + c.getCode() + ", Country: " + c.getCountry()));
            } else {
                System.err.println("Country list is null from JSON.");
            }
        } catch (Exception e) {
            System.err.println("Failed to load country codes: " + e.getMessage());
        }
        return codes;
    }

    @FXML
    private void handleSettings() {
        // TODO: develop setting dialog
    }

    @FXML
    private void handleBack() {
        changeSceneWithSameSize(root, "/Client/fxml/Welcome.fxml");
    }

    @FXML
    private void continueAction() {
        String preCode = countryPreCode.getText();
        String phoneNumber = phoneNumberField.getText();

        if (phoneNumber.isEmpty() || !preCode.startsWith("+")) {
            System.out.println("Phone number or country code is invalid!");
            return;
        }

        // Show a loading indicator to the user
        // e.g., progressIndicator.setVisible(true);

        Task<RpcResponse<RequestCodeOutputModel>> otpTask = new Task<>() {
            @Override
            protected RpcResponse<RequestCodeOutputModel> call() throws Exception {
                return rpcCaller.requestOTP(preCode + phoneNumber);
            }
        };
        otpTask.setOnSucceeded(event -> {
            try {
                var response = otpTask.getValue();
                if(response.getStatusCode() == StatusCode.OK){
                    changeSceneWithSameSize(root, "/Client/fxml/verificationViaTelegram.fxml",(VerificationViaTelegramController controller) -> {
                        controller.setRequestCodeOutputModel(response.getPayload());
                    });
                }


            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
        });
        otpTask.setOnFailed(event -> {
            System.out.println("Task failed.");
            otpTask.getException().printStackTrace();
        });

        // Start the background task
        new Thread(otpTask).start();
    }

    @FXML
    private void showCountrySelection() {
        Stage parentStage = (Stage) root.getScene().getWindow();
        try {
            // Apply dim effect to parent stage with animation
            ColorAdjust dimEffect = new ColorAdjust();
            parentStage.getScene().getRoot().setEffect(dimEffect);

            Timeline fadeIn = new Timeline(
                    new KeyFrame(Duration.ZERO, new javafx.animation.KeyValue(dimEffect.brightnessProperty(), 0)),
                    new KeyFrame(Duration.millis(300), new javafx.animation.KeyValue(dimEffect.brightnessProperty(), -0.3, Interpolator.EASE_BOTH))
            );
            fadeIn.play();

            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client/fxml/countrySelection.fxml"));
            Parent root = loader.load();

            // Create and configure the dialog stage
            Stage dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            dialogStage.setScene(scene);
            dialogStage.initOwner(parentStage);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setResizable(false);
            dialogStage.setTitle("Select Country");

            // Set the dialog stage to the controller
            CountrySelectionController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setParentController(this);
            controller.setAllCountries(allCountries); // Pass the loaded countries

            // Adjust height dynamically after layout is fully computed
            Platform.runLater(() -> {
                controller.countryList.applyCss();
                double listHeight = controller.countryList.getHeight() > 0 ? controller.countryList.getHeight() : 200;
                double marginTotal = 20.0;
                double searchHeight = controller.searchField.getHeight() > 0 ? controller.searchField.getHeight() : 30.0;
                double newVBoxHeight = listHeight + marginTotal + searchHeight;
                controller.rootVBox.setPrefHeight(newVBoxHeight);
                dialogStage.sizeToScene();
            });

            // Center the dialog stage with an offset downward
            Platform.runLater(() -> {
                double centerX = parentStage.getX() + (parentStage.getWidth() - dialogStage.getWidth()) / 2;
                double centerY = parentStage.getY() + (parentStage.getHeight() - dialogStage.getHeight()) / 2;
                dialogStage.setX(centerX);
                dialogStage.setY(centerY);
            });

            // Show the dialog and handle closing with reverse animation
            dialogStage.showAndWait();

            // Reverse animation for undimming
            Timeline fadeOut = new Timeline(
                    new KeyFrame(Duration.ZERO, new javafx.animation.KeyValue(dimEffect.brightnessProperty(), -0.3, Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.millis(300), new javafx.animation.KeyValue(dimEffect.brightnessProperty(), 0))
            );
            fadeOut.setOnFinished(e -> parentStage.getScene().getRoot().setEffect(null));
            fadeOut.play();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading country selection dialog: " + e.getMessage());
        }
    }

    private void updateCountryName(String code) {
        // Avoid creating a new instance, use the existing logic
        if (allCountries != null) {
            String cleanCode = code.replace("-", "");
            System.out.println("Processing code: " + cleanCode + ", allCountries size: " + allCountries.size());
            String countryName = allCountries.stream()
                    .filter(c -> c.getCode().replace("+", "").replace("-", "").equals(cleanCode))
                    .findFirst()
                    .map(CountryCode::getCountry)
                    .orElse("Invalid Country Code");
            System.out.println("Found country name: " + countryName);
            if ("Invalid Country Code".equals(countryName)) {
                selectCountryButton.setText("Invalid Country Code");
            } else {
                countryPreCode.setText("+" + code);
                selectCountryButton.setText(countryName);
            }
        } else {
            System.err.println("allCountries is null in updateCountryName!");
            selectCountryButton.setText("Country Code");
        }
    }

    public void setCountryCode(String code, String countryName) {
        if (code != null && !code.isEmpty()) {
            countryPreCode.setText("+" + code);
            selectCountryButton.setText(countryName);
        }
    }
}