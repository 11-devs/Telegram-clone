import Client.Controllers.MainChatController;
import Client.Controllers.SidebarMenuController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TelegramApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load FXML chat
        FXMLLoader chatLoader = new FXMLLoader(getClass().getResource("/Client/fxml/mainChat.fxml"));
        Parent chatRoot = chatLoader.load();
        MainChatController chatController = chatLoader.getController();

        // Load FXML sidebar
        FXMLLoader sidebarLoader = new FXMLLoader(getClass().getResource("/Client/fxml/sidebarMenu.fxml"));
        Parent sidebarRoot = sidebarLoader.load();
        SidebarMenuController sidebarController = sidebarLoader.getController();

        // Communication between controllers
        chatController.setSidebarController(sidebarController);

        // Setting the scene
        Scene scene = new Scene(chatRoot);
        scene.getStylesheets().add("/Client/css/mainChat.css");
        primaryStage.setScene(scene);
        primaryStage.setTitle("Telegram Clone");
        primaryStage.show();

        // Setting Stage for Sidebar (Optional)
        sidebarController.setPrimaryStage(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}