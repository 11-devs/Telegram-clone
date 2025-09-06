package Client.Controllers;

import Client.AccessKeyManager;
import Client.AppConnectionManager;
import Client.RpcCaller;
import JSocket2.Protocol.Rpc.RpcResponse;
import JSocket2.Protocol.StatusCode;
import Shared.Utils.AnimationUtil;
import Shared.Utils.DeviceUtil;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Map;

import static Shared.Utils.SceneUtil.changeSceneWithSameSize;

public class ResetAccountController {
    private RpcCaller rpcCaller;
    private String phoneNumber;

    @FXML
    private VBox root;
    @FXML
    private VBox infoBox;
    @FXML
    private Label warningLabel;
    @FXML
    private CheckBox confirmationCheckBox;
    @FXML
    private Button resetAccountButton;
    @FXML
    private Label statusLabel;

    private final PseudoClass errorPseudoClass = PseudoClass.getPseudoClass("error");

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @FXML
    private void initialize() {
        rpcCaller = AppConnectionManager.getInstance().getRpcCaller();

        if (infoBox != null) {
            infoBox.setTranslateX(75);
            TranslateTransition transition = new TranslateTransition(Duration.seconds(0.5), infoBox);
            transition.setToX(0);
            transition.play();
        }
        if (resetAccountButton != null) {
            resetAccountButton.setTranslateY(50);
            TranslateTransition transition = new TranslateTransition(Duration.seconds(0.5), resetAccountButton);
            transition.setToY(0);
            transition.play();
        }

        confirmationCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            resetAccountButton.setDisable(!newVal);
            clearStatusMessage();
        });
    }

    @FXML
    private void handleSettings() {

    }

    @FXML
    private void handleBack() {
        changeSceneWithSameSize(root, "/Client/fxml/CloudPasswordCheck.fxml");
    }

    @FXML
    private void handleResetAccount() {
        if (!confirmationCheckBox.isSelected()) {
            setStatusMessage("Please confirm that you understand the data loss.", true);
            AnimationUtil.showErrorAnimation(confirmationCheckBox);
            return;
        }

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            setStatusMessage("Error: Phone number is missing.", true);
            return;
        }

        setStatusMessage("Resetting account...", false);
        resetAccountButton.setDisable(true);

        Task<RpcResponse<Object>> resetAccountTask = new Task<>() {
            @Override
            protected RpcResponse<Object> call() throws Exception {
                return rpcCaller.resetAccount(phoneNumber, DeviceUtil.getDeviceInfo());
            }
        };

        resetAccountTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                var response = resetAccountTask.getValue();
                if (response.getStatusCode() == StatusCode.OK) {
                    Map<String, String> resetPayload = (Map<String, String>) response.getPayload();
                    String accessKey = resetPayload.get("accessKey");
                    if (accessKey != null) {
                        try {
                            if (AccessKeyManager.LoginWithAccessKey(accessKey, AppConnectionManager.getInstance().getConnectionManager().getClient()) == StatusCode.OK) {
                                setStatusMessage("Account reset and logged in successfully!", false);
                                System.out.println("Account reset and logged in successfully.");
                                changeSceneWithSameSize(root, "/Client/fxml/mainChat.fxml");
                            } else {
                                setStatusMessage("Account reset, but failed to log in automatically.", true);
                                System.err.println("Account reset, but failed to log in automatically.");
                                changeSceneWithSameSize(root, "/Client/fxml/phoneNumber.fxml");
                            }
                        } catch (IOException e) {
                            System.err.println("Error during access key login after account reset: " + e.getMessage());
                            setStatusMessage("Error logging in after reset. Please try re-logging.", true);
                            changeSceneWithSameSize(root, "/Client/fxml/phoneNumber.fxml");
                        }
                    } else {
                        setStatusMessage("Account reset, but no access key received for automatic login.", true);
                        changeSceneWithSameSize(root, "/Client/fxml/phoneNumber.fxml");
                    }
                } else {
                    setStatusMessage("Failed to reset account: " + response.getMessage(), true);
                    System.err.println("Failed to reset account: " + response.getMessage());
                }
                resetAccountButton.setDisable(false);
            });
        });

        resetAccountTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                setStatusMessage("An unexpected error occurred during account reset. Please check your connection and try again.", true);
                resetAccountTask.getException().printStackTrace();
                resetAccountButton.setDisable(false);
            });
        });

        new Thread(resetAccountTask).start();
    }

    private void setStatusMessage(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.pseudoClassStateChanged(errorPseudoClass, isError);
        if (isError) {
            statusLabel.setStyle("-fx-text-fill: #d34343;");
        } else {
            statusLabel.setStyle("-fx-text-fill: #28a745;");
        }
    }

    private void clearStatusMessage() {
        statusLabel.setText("");
        statusLabel.pseudoClassStateChanged(errorPseudoClass, false);
        statusLabel.setStyle("");
    }
}