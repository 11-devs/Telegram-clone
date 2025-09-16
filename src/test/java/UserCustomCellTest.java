import Client.Controllers.UserCustomCell;
import Shared.Models.ChatViewModel;
import Shared.Models.ChatViewModelBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class UserCustomCellTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            ObservableList<ChatViewModel> users = createSampleUsers();
            ListView<ChatViewModel> chatListView = new ListView<>(users);
            chatListView.setCellFactory(listView -> new UserCustomCell());
            chatListView.setPrefWidth(400);
            chatListView.setPrefHeight(600);

            java.net.URL cssUrl = getClass().getResource("/Client/css/userCustomCell.css");
            if (cssUrl != null) {
                chatListView.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                chatListView.setStyle("-fx-background-color: #17212B; -fx-border-color: transparent; -fx-border-width: 0;");
            }

            VBox root = new VBox(chatListView);
            Scene scene = new Scene(root, 400, 600);
            primaryStage.setTitle("UserCustomCell Test - Telegram-style Chat List");
            primaryStage.setScene(scene);
            primaryStage.show();

            setupDynamicUpdates(users, chatListView);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error in start method: " + e.getMessage());
        }
    }

    private ObservableList<ChatViewModel> createSampleUsers() {
        ObservableList<ChatViewModel> users = FXCollections.observableArrayList();

        users.add(new ChatViewModelBuilder()
                .displayName("John Doe")
                .lastMessage("Hey there! How are you doing?")
                .time(getCurrentTime())
                .notificationsNumber("2")
                .isOnline(true)
                .isVerified(true)
                .messageStatus("read")
                .type("user")
                .chatId("user1")
                .build());

        users.add(new ChatViewModelBuilder()
                .displayName("Alice Smith")
                .lastMessage("Can we meet tomorrow?")
                .time(getYesterdayTime())
                .notificationsNumber("1")
                .isOnline(false)
                .lastSeen("last seen 2 hours ago")
                .messageStatus("delivered")
                .type("group")
                .chatId("user2")
                .build());

        users.add(new ChatViewModelBuilder()
                .displayName("Tech Community")
                .lastMessage("New announcement posted!")
                .time(getTimeHoursAgo(1))
                .notificationsNumber("15")
                .isOnline(true)
                .messageStatus("read")
                .type("supergroup")
                .chatId("user3")
                .build());

        users.add(new ChatViewModelBuilder()
                .displayName("News Channel")
                .lastMessage("Breaking news: Major update released")
                .time(getTimeHoursAgo(2))
                .notificationsNumber("0")
                .isOnline(false)
                .messageStatus("read")
                .type("channel")
                .chatId("user4")
                .build());

        users.add(new ChatViewModelBuilder()
                .displayName("Admin User")
                .lastMessage("I'm an admin of this group")
                .time(getTimeHoursAgo(4))
                .notificationsNumber("0")
                .isOnline(true)
                .messageStatus("read")
                .type("admin")
                .chatId("user7")
                .build());

        users.add(new ChatViewModelBuilder()
                .displayName("Muted ContactViewModel")
                .lastMessage("This contact is muted")
                .time(getTimeHoursAgo(5))
                .notificationsNumber("5")
                .isOnline(false)
                .isMuted(true)
                .messageStatus("sent")
                .type("user")
                .chatId("user8")
                .build());

        users.add(new ChatViewModelBuilder()
                .displayName("Pinned Chat")
                .lastMessage("This chat is pinned")
                .time(getTimeHoursAgo(6))
                .notificationsNumber("0")
                .isOnline(true)
                .isPinned(true)
                .messageStatus("read")
                .type("group")
                .chatId("user9")
                .build());

        users.add(new ChatViewModelBuilder()
                .displayName("Long Message User")
                .lastMessage("This is a very long message that should be truncated in the UI to prevent layout issues and maintain consistent appearance across all chat items")
                .time(getTimeHoursAgo(7))
                .notificationsNumber("0")
                .isOnline(false)
                .messageStatus("read")
                .type("user")
                .chatId("user10")
                .build());

        return users;
    }

    private void setupDynamicUpdates(ObservableList<ChatViewModel> users, ListView<ChatViewModel> chatListView) {
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() -> {
                    if (!users.isEmpty()) {
                        ChatViewModel firstUser = users.get(0);
                        firstUser.setLastMessage("Just sent a new message!");
                        firstUser.setTime(getCurrentTime());
                        firstUser.incrementUnreadCount();
                    }
                });

                Thread.sleep(2000);
                Platform.runLater(() -> {
                    ChatViewModel newUser = new ChatViewModelBuilder()
                            .displayName("New ContactViewModel")
                            .lastMessage("Hello! I'm new here")
                            .time(getCurrentTime())
                            .isOnline(true)
                            .notificationsNumber("1")
                            .type("user")
                            .chatId("newUser")
                            .build();
                    users.add(0, newUser);
                });

                simulateTyping(users.get(0), 5000, chatListView);
                simulateTyping(users.get(1), 7000, chatListView);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrupted: " + e.getMessage());
            }
        }).start();
    }

    private void simulateTyping(ChatViewModel user, long durationMillis, ListView<ChatViewModel> chatListView) {
        new Thread(() -> {
            try {
                System.out.println("Starting typing simulation for: " + user.getDisplayName());
                Platform.runLater(() -> {
                    user.setTyping(true);
                });
                Thread.sleep(2000);
                Platform.runLater(() -> {
                    user.setTyping(false);
                    user.setLastMessage("Finished typing a message!");
                    user.setTime(getCurrentTime());
                });
                Thread.sleep(durationMillis - 2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Typing simulation interrupted for " + user.getDisplayName() + ": " + e.getMessage());
            }
        }).start();
    }

    private String getCurrentTime() {
        return LocalDateTime.of(2025, 8, 10, 13, 6)
                .atZone(ZoneId.of("Asia/Singapore"))
                .format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private String getYesterdayTime() {
        return LocalDateTime.of(2025, 8, 9, 13, 6)
                .atZone(ZoneId.of("Asia/Singapore"))
                .format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private String getTimeHoursAgo(int hours) {
        return LocalDateTime.of(2025, 8, 10, 13, 6)
                .minusHours(hours)
                .atZone(ZoneId.of("Asia/Singapore"))
                .format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private String getTimeDaysAgo(int days) {
        return LocalDateTime.of(2025, 8, 10, 13, 6)
                .minusDays(days)
                .atZone(ZoneId.of("Asia/Singapore"))
                .format(DateTimeFormatter.ofPattern("MM/dd"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}