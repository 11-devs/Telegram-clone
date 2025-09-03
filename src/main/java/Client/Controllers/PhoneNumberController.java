package Client.Controllers;

import Client.AppConnectionManager;
import Client.RpcCaller;
import JSocket2.Core.Client.ConnectionManager;
import JSocket2.Protocol.Rpc.RpcResponse;
import JSocket2.Protocol.StatusCode;
import Shared.Api.Models.AccountController.RequestCodePhoneNumberInputModel;
import Shared.Api.Models.AccountController.RequestCodePhoneNumberOutputModel;
import Shared.Models.CountryCode;
import Shared.Utils.AnimationUtil;
import Shared.Utils.DeviceUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

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
    private final PseudoClass errorPseudoClass = PseudoClass.getPseudoClass("error");
    private ObservableList<CountryCode> allCountries;
    private String currentFormatPattern = "############";

    public void initialize() {
        connectionManager = AppConnectionManager.getInstance().getConnectionManager();
        rpcCaller = AppConnectionManager.getInstance().getRpcCaller();
        if (infoBox != null) {
            infoBox.setTranslateX(75);
            TranslateTransition transition = new TranslateTransition(Duration.seconds(0.5), infoBox);
            transition.setToX(0);
            transition.play();
        }

        if (continueButton != null) {
            continueButton.setTranslateY(75);
            TranslateTransition transition = new TranslateTransition(Duration.seconds(0.5), continueButton);
            transition.setToY(0);
            transition.play();
        }

        allCountries = loadCountryCodes();

        setDefaultCountry();

        countryPreCode.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.startsWith("+") && newText.substring(1).matches("\\d{0,6}")) {
                return change;
            } else if (!newText.startsWith("+")) {
                // This logic prevents the user from deleting the "+" sign
                change.setText("+");
                change.setRange(0, change.getControlText().length());
                change.setCaretPosition(1);
                change.setAnchor(1);
                return change;
            }
            return null;
        }));

        countryPreCode.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.startsWith("+")) {
                String code = newValue.substring(1);
                updateCountryNameAndFormat(code);
            }
        });
    }

    private void setDefaultCountry() {
        String systemCountryISO = Locale.getDefault().getCountry();
        if (systemCountryISO != null && !systemCountryISO.isEmpty()) {
            Optional<CountryCode> defaultCountry = allCountries.stream()
                    .filter(c -> systemCountryISO.equalsIgnoreCase(c.getIso()))
                    .findFirst();

            if (defaultCountry.isPresent()) {
                CountryCode country = defaultCountry.get();
                setCountryCode(country.getCode(), country.getCountry());
                return; // Exit after setting the specific default
            }
        }
        countryPreCode.setText("+");
        selectCountryButton.setText("Country Code");
        applyPhoneNumberFormatting("############");
    }
    private void applyPhoneNumberFormatting(String formatPattern) {
        this.currentFormatPattern = formatPattern;
        phoneNumberField.setPromptText(formatPattern.replace('#', '-'));

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String currentText = change.getControlText();
            String currentDigits = currentText.replaceAll("[^\\d]", "");

            int rangeStart = change.getRangeStart();
            int rangeEnd = change.getRangeEnd();

            int digitsBeforeStart = 0;
            for (int i = 0; i < rangeStart; i++) {
                if (Character.isDigit(currentText.charAt(i))) {
                    digitsBeforeStart++;
                }
            }

            int digitsInRange = 0;
            for (int i = rangeStart; i < rangeEnd; i++) {
                if (Character.isDigit(currentText.charAt(i))) {
                    digitsInRange++;
                }
            }

            String addedDigits = change.getText().replaceAll("[^\\d]", "");
            String newDigitsString = currentDigits.substring(0, digitsBeforeStart) +
                    addedDigits +
                    currentDigits.substring(digitsBeforeStart + digitsInRange);

            int maxLen = formatPattern.replaceAll("[^#]", "").length();
            if (newDigitsString.length() > maxLen) {
                newDigitsString = newDigitsString.substring(0, maxLen);
            }

            StringBuilder formatted = new StringBuilder();
            int digitIndex = 0;
            for (char p : formatPattern.toCharArray()) {
                if (digitIndex >= newDigitsString.length()) {
                    break;
                }
                if (p == '#') {
                    formatted.append(newDigitsString.charAt(digitIndex++));
                } else {
                    formatted.append(p);
                }
            }

            int newCaretPosition = 0;
            int targetDigits = digitsBeforeStart + addedDigits.length();
            int digitsCounted = 0;
            for (int i = 0; i < formatted.length(); i++) {
                if (Character.isDigit(formatted.charAt(i))) {
                    digitsCounted++;
                }
                if (digitsCounted == targetDigits) {
                    newCaretPosition = i + 1;
                    break;
                }
            }

            if (targetDigits == 0) {
                newCaretPosition = 0;
            } else if (newCaretPosition == 0) {
                newCaretPosition = formatted.length();
            }

            while (newCaretPosition < formatted.length() && !Character.isDigit(formatted.charAt(newCaretPosition))) {
                newCaretPosition++;
            }

            change.setText(formatted.toString());
            change.setRange(0, change.getControlText().length());
            change.setCaretPosition(newCaretPosition);
            change.setAnchor(newCaretPosition);

            return change;
        };

        phoneNumberField.setTextFormatter(new TextFormatter<>(filter));
    }



    private ObservableList<CountryCode> loadCountryCodes() {
        ObservableList<CountryCode> codes = FXCollections.observableArrayList();
        Gson gson = new Gson();
        Type listType = new TypeToken<List<CountryCode>>(){}.getType();

        try (Reader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/client/json/country_codes.json")))) {
            List<CountryCode> countryList = gson.fromJson(reader, listType);
            if (countryList != null) {
                codes.addAll(countryList);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        String phoneNumberDigits = phoneNumberField.getText().replaceAll("[^\\d]", "");
        int requiredLength = currentFormatPattern.replaceAll("[^#]", "").length();
        if (phoneNumberDigits.length() < requiredLength) {
            System.out.println("Phone number is too short!");
            AnimationUtil.showErrorAnimation(phoneNumberField,errorPseudoClass);
            return;
        }

        if (preCode.length() < 2) {
            System.out.println("Country code is invalid!");
            AnimationUtil.showErrorAnimation(countryPreCode,errorPseudoClass);
            return;
        }

        final boolean[] isRegistered = new boolean[1];
        Task<RpcResponse<RequestCodePhoneNumberOutputModel>> otpTask = new Task<>() {
            @Override
            protected RpcResponse<RequestCodePhoneNumberOutputModel> call() throws Exception {
                String fullPhoneNumber = preCode + phoneNumberDigits;
                isRegistered[0] = rpcCaller.isPhoneNumberRegistered(fullPhoneNumber).getPayload();
                return rpcCaller.requestOTP(new RequestCodePhoneNumberInputModel(fullPhoneNumber, isRegistered[0] ? "telegram" : "sms", DeviceUtil.getDeviceInfo()));
            }
        };
        otpTask.setOnSucceeded(event -> {
            try {
                var response = otpTask.getValue();
                if(response.getStatusCode() == StatusCode.OK){
                    if(isRegistered[0]){
                        changeSceneWithSameSize(root, "/Client/fxml/verificationViaTelegram.fxml",(VerificationViaTelegramController controller) -> {
                            controller.setRequestCodeOutputModel(response.getPayload());
                        });
                    }else{
                        changeSceneWithSameSize(root, "/Client/fxml/verificationViaSms.fxml",(VerificationViaSmsController controller) -> {
                            controller.setRequestCodeOutputModel(response.getPayload());
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        otpTask.setOnFailed(event -> otpTask.getException().printStackTrace());

        new Thread(otpTask).start();
    }

    @FXML
    private void showCountrySelection() {
        Stage parentStage = (Stage) root.getScene().getWindow();
        try {
            ColorAdjust dimEffect = new ColorAdjust();
            parentStage.getScene().getRoot().setEffect(dimEffect);

            Timeline fadeIn = new Timeline(
                    new KeyFrame(Duration.ZERO, new javafx.animation.KeyValue(dimEffect.brightnessProperty(), 0)),
                    new KeyFrame(Duration.millis(300), new javafx.animation.KeyValue(dimEffect.brightnessProperty(), -0.3, Interpolator.EASE_BOTH))
            );
            fadeIn.play();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client/fxml/countrySelection.fxml"));
            Parent dialogRoot = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(dialogRoot);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            dialogStage.setScene(scene);
            dialogStage.initOwner(parentStage);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setResizable(false);

            CountrySelectionController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setParentController(this);
            controller.setAllCountries(allCountries);

            dialogStage.showAndWait();

            Timeline fadeOut = new Timeline(
                    new KeyFrame(Duration.ZERO, new javafx.animation.KeyValue(dimEffect.brightnessProperty(), -0.3, Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.millis(300), new javafx.animation.KeyValue(dimEffect.brightnessProperty(), 0))
            );
            fadeOut.setOnFinished(e -> parentStage.getScene().getRoot().setEffect(null));
            fadeOut.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateCountryNameAndFormat(String code) {
        if (allCountries != null) {
            var countryOptional = allCountries.stream()
                    .filter(c -> c.getCode().replace("+", "").equals(code))
                    .findFirst();

            if (countryOptional.isPresent()) {
                CountryCode country = countryOptional.get();
                selectCountryButton.setText(country.getCountry());
                applyPhoneNumberFormatting(country.getFormat());
            } else {
                selectCountryButton.setText("Invalid Country Code");
                applyPhoneNumberFormatting("############"); // A generic default
            }
        } else {
            selectCountryButton.setText("Country Code");
        }
    }

    public void setCountryCode(String code, String countryName) {
        if (code != null && !code.isEmpty()) {
            countryPreCode.setText("+" + code);
            selectCountryButton.setText(countryName);

            phoneNumberField.clear();
            phoneNumberField.requestFocus();

            String format = allCountries.stream()
                    .filter(c -> c.getCode().replace("+", "").equals(code))
                    .findFirst()
                    .map(CountryCode::getFormat)
                    .orElse("############");

            applyPhoneNumberFormatting(format);
        }
    }
}