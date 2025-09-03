package Client.Controllers;

import Client.AccessKeyManager;
import Client.AppConnectionManager;
import Client.RpcCaller;
import JSocket2.Core.Client.ConnectionManager;
import JSocket2.Protocol.Rpc.RpcResponse;
import JSocket2.Protocol.StatusCode;
import Shared.Api.Models.AccountController.LoginInputModel;
import Shared.Api.Models.AccountController.LoginOutputModel;
import Shared.Utils.DeviceUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;

import static Shared.Utils.SceneUtil.changeSceneWithSameSize;

public class CloudPasswordCheckController {
    private ConnectionManager connectionManager;
    private RpcCaller rpcCaller;

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    private String phoneNumber;

    @FXML
    private VBox root;
    @FXML
    private VBox infoBox;
    @FXML
    private Button submitButton;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label passwordLabel;

    private final PseudoClass errorPseudoClass = PseudoClass.getPseudoClass("error");
    @FXML
    private void initialize() {
        connectionManager = AppConnectionManager.getInstance().getConnectionManager();
        rpcCaller = AppConnectionManager.getInstance().getRpcCaller();
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
        if (submitButton != null) {
            submitButton.setTranslateY(50);
            var transition = new javafx.animation.TranslateTransition(javafx.util.Duration.seconds(0.5), submitButton);
            transition.setToY(0);
            transition.setAutoReverse(false);
            transition.setCycleCount(1);
            transition.play();
        }
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            passwordField.pseudoClassStateChanged(errorPseudoClass, false);
            passwordLabel.pseudoClassStateChanged(errorPseudoClass, false);
        });
        passwordField.requestFocus(); // Initial focus on password field
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
    private void handleSubmit() {
        String password = passwordField.getText();
        if (password != null && !password.isEmpty()) {
            System.out.println("Cloud password entered: " + password);
            // TODO: Add password validation logic
            Task<RpcResponse<LoginOutputModel>> loginTask = new Task<RpcResponse<LoginOutputModel>>() {
                @Override
                protected RpcResponse<LoginOutputModel> call() throws Exception {
                    return rpcCaller.login(new LoginInputModel(password,phoneNumber, DeviceUtil.getDeviceInfo()));
                }
            };
            loginTask.setOnSucceeded(event -> {
                try {
                    var response = loginTask.getValue();
                    if(response.getStatusCode() == StatusCode.OK){
                        switch (response.getPayload().getStatus()){
                            case "logged_in":
                                var resultCode = AccessKeyManager.LoginWithAccessKey(response.getPayload().getAccessKey(),connectionManager.getClient());
                                if(resultCode == StatusCode.OK) System.out.println("Successful login");
                                break;
                        }
                    }else{
                        switch (response.getMessage()) {
                            case "invalid_password":
                                Platform.runLater(() -> {
                                    passwordField.clear();
                                    passwordField.pseudoClassStateChanged(errorPseudoClass, true);
                                    passwordLabel.pseudoClassStateChanged(errorPseudoClass, true);
                                    System.out.println("Invalid password. Field cleared and bordered in red.");
                                });
                                break;
                        }
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                }
            });
            loginTask.setOnFailed(event -> {
                System.out.println("Task failed.");
                loginTask.getException().printStackTrace();
            });

            // Start the background task
            new Thread(loginTask).start();
        } else {
            System.out.println("Please enter a password.");
        }
    }

    @FXML
    private void handleForgotPassword() {
        // TODO: Implement forgot password logic
        System.out.println("Forgot password link clicked.");
        // Example: changeSceneWithSameSize(root, "Client/fxml/forgotPassword.fxml");
    }
}