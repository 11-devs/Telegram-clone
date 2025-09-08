package Client.Controllers;

import Client.Services.FileDownloadService;
import Shared.Models.UserViewModel;
import Shared.Models.UserType;
import Shared.Utils.TextUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Circle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class UserCustomCellController {

    @FXML
    private GridPane root;

    @FXML
    private ImageView avatarImage;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label lastMessageLabel;

    @FXML
    private Label messageTimeLabel;

    @FXML
    private Label nombreMessageLabel;

    @FXML
    private SVGPath statusIconSvg;

    @FXML
    private StackPane messageStatusPanel;

    @FXML
    private Circle onlineIndicator;

    @FXML
    private SVGPath typeLogo;

    @FXML
    private Label mutedIcon; // Can be used in fxml

    @FXML
    private Label pinnedIcon;

    @FXML
    private StackPane notificationPanel;

    private UserViewModel currentUser;
    private boolean isSelected = false;
    private ChangeListener<String> messageStatusListener;
    private ChangeListener<Boolean> mutedPropertyListener;
    @FXML
    public void initialize() {
        setupClickHandlers();
    }

    public void setCurrentUser(UserViewModel user) {
        this.currentUser = user;
    }

    // The main method of setting up the bindings

    private void setupBindings() {
        if (currentUser == null) return;

        bindUserName();
        bindLastMessage();
        bindMessageTime();
        bindOnlineIndicator();
        bindNotifications();
        bindMessageStatus();
        bindMutedPinnedIcons();
        setupTypeLogoListener();
    }

    private void bindUserName() {
        if (userNameLabel != null) {
            userNameLabel.textProperty().bind(currentUser.displayNameProperty());
        }
    }

    private void bindLastMessage() {
        if (lastMessageLabel == null) return;

        lastMessageLabel.textProperty().bind(
                currentUser.isTypingProperty().flatMap(isTyping -> {
                    if (Boolean.TRUE.equals(isTyping)) {
                        // When typing, the text depends on the typing user's name.
                        return currentUser.typingUserNameProperty().map(typingUser -> {
                            lastMessageLabel.getStyleClass().setAll("last-message-label", "typing-indicator");
                            return generateTypingText();
                        });
                    } else {
                        // Check if it's a public search result first
                        return currentUser.isFromPublicSearchProperty().flatMap(isPublicSearch -> {
                            if (Boolean.TRUE.equals(isPublicSearch)) {
                                // For public search results, bind to the subtitle property
                                return currentUser.subtitleProperty().map(subtitle -> {
                                    lastMessageLabel.getStyleClass().setAll("last-message-label", "search-subtitle");
                                    return formatLastMessageText(subtitle);
                                });
                            } else {
                                // When not a search result, use original logic for drafts and last messages.
                                return currentUser.isDraftProperty().flatMap(isDraft ->
                                        currentUser.lastMessageProperty().map(lastMessage -> {
                                            if (Boolean.TRUE.equals(isDraft)) {
                                                lastMessageLabel.getStyleClass().setAll("last-message-label", "draft-indicator");
                                                return formatDraftText(lastMessage);
                                            } else {
                                                lastMessageLabel.getStyleClass().setAll("last-message-label");
                                                return formatLastMessageText(lastMessage);
                                            }
                                        })
                                );
                            }
                        });
                    }
                })
        );
    }
    private String generateTypingText() {
        UserType type = currentUser.getType();
        if (type == UserType.GROUP || type == UserType.SUPERGROUP) {
            String typingUserName = currentUser.getTypingUserName();
            if (typingUserName != null && !typingUserName.isEmpty()) {
                String truncatedName = typingUserName.length() > 10 ? typingUserName.substring(0, 10) + "..." : typingUserName;
                return truncatedName + " is typing...";
            } else {
                return "Someone is typing...";
            }
        }
        return "Typing...";
    }

    private String formatDraftText(String lastMessage) {
        if (lastMessage != null && !lastMessage.isEmpty()) {
            String strippedMessage = TextUtil.stripFormattingForPreview(lastMessage);
            return "Draft: " + (strippedMessage.length() > 35 ? strippedMessage.substring(0, 32) + "..." : strippedMessage);
        } else {
            return "Draft: No draft";
        }
    }

    private String formatLastMessageText(String lastMessage) {
        if (lastMessage != null && !lastMessage.isEmpty()) {
            String strippedMessage = TextUtil.stripFormattingForPreview(lastMessage);
            return strippedMessage.length() > 35 ? strippedMessage.substring(0, 32) + "..." : strippedMessage;
        } else {
            return "No messages yet";
        }
    }

    private void bindMessageTime() {
        if (messageTimeLabel == null) return;

        messageTimeLabel.textProperty().bind(currentUser.timeProperty().map(time -> {
            if (time != null) {
                try {
                    LocalDateTime messageTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    LocalDateTime now = LocalDateTime.now();
                    return formatTime(messageTime, now);
                } catch (Exception e) {
                    return time;  // If the date format was incorrect, the original string is returned.
                }
            }
            return "";
        }));

        messageTimeLabel.getStyleClass().add("message-time-label");
    }

    private void bindOnlineIndicator() {
        if (onlineIndicator != null) {
            onlineIndicator.visibleProperty().bind(
                    currentUser.isOnlineProperty()
                            .and(currentUser.typeProperty().isEqualTo(UserType.USER))
                            .and(currentUser.lastSeenProperty().isNotEqualTo("never"))
            );
            onlineIndicator.getStyleClass().add("online-indicator");
        }
    }

    private void bindNotifications() {
        if (notificationPanel == null || nombreMessageLabel == null) return;

        nombreMessageLabel.textProperty().bind(currentUser.notificationsNumberProperty());
        // Keep base style classes
        nombreMessageLabel.getStyleClass().add("notification-count-label");
        notificationPanel.getStyleClass().add("notification-badge");

        // Visibility is still based on count
        notificationPanel.visibleProperty().bind(currentUser.notificationsNumberProperty().map(count ->
                count != null && !count.equals("0") && !count.isEmpty()
        ));

        // Listener for count changes to update style
        currentUser.notificationsNumberProperty().addListener((obs, oldVal, newVal) -> updateNotificationBadgeStyle());

        // Listener for mute state changes to update style
        mutedPropertyListener = (obs, oldVal, newVal) -> updateNotificationBadgeStyle();
        currentUser.isMutedProperty().addListener(mutedPropertyListener);

        // Set initial style
        updateNotificationBadgeStyle();
    }

    private void bindMessageStatus() {
        if (messageStatusPanel == null || statusIconSvg == null) return;

        messageStatusPanel.getStyleClass().add("message-status");
        messageStatusPanel.visibleProperty().bind(
                currentUser.messageStatusProperty().isNotEqualTo("none")
                        .and(currentUser.isMessageSentByCurrentUserProperty())
        );
        statusIconSvg.visibleProperty().bind(messageStatusPanel.visibleProperty());
        statusIconSvg.contentProperty().bind(currentUser.messageStatusProperty().map(status -> switch (status) {
            case "sent", "delivered" -> "M2.75 8.75 L6.25 12.25 L13.25 4.75";
            case "read" -> "m1.75 9.75 2.5 2.5m3.5-4 2.5-2.5m-4.5 4 2.5 2.5 6-6.5";
            default -> "";
        }));
        statusIconSvg.getStyleClass().add("status-icon");
        messageStatusListener = (obs, oldStatus, newStatus) -> {
            statusIconSvg.getStyleClass().removeAll("sent", "delivered", "read");
            if (newStatus != null && !newStatus.equals("none")) {
                statusIconSvg.getStyleClass().add(newStatus);
            }
        };
        currentUser.messageStatusProperty().addListener(messageStatusListener);

        // Set initial state correctly
        String initialStatus = currentUser.getMessageStatus();
        statusIconSvg.getStyleClass().removeAll("sent", "delivered", "read");
        if (initialStatus != null && !initialStatus.equals("none")) {
            statusIconSvg.getStyleClass().add(initialStatus);
        }
    }

    private void bindMutedPinnedIcons() {
        if (mutedIcon != null) {
            mutedIcon.getStyleClass().add("muted-icon");
            mutedIcon.visibleProperty().bind(currentUser.isMutedProperty());
        }
        if (pinnedIcon != null) {
            pinnedIcon.getStyleClass().add("pinned-indicator");
            pinnedIcon.visibleProperty().bind(currentUser.isPinnedProperty());
        }
    }

    private void setupTypeLogoListener() {
        if (typeLogo != null) {
            currentUser.typeProperty().addListener((obs, oldVal, newVal) -> updateTypeLogo());
        }
    }

    // Main cell update method

    public void updateCell(UserViewModel item) {
        if (item == null) return;

        unbindAll();  // Cancel previous bindings
        currentUser = item;
        setupBindings();  // New bonding setup
        updateCellContent();
        updateCellAppearance();
    }

    void unbindAll() {
        if (userNameLabel != null) userNameLabel.textProperty().unbind();
        if (lastMessageLabel != null) lastMessageLabel.textProperty().unbind();
        if (messageTimeLabel != null) messageTimeLabel.textProperty().unbind();
        if (onlineIndicator != null) onlineIndicator.visibleProperty().unbind();
        if (nombreMessageLabel != null) nombreMessageLabel.textProperty().unbind();
        if (notificationPanel != null) notificationPanel.visibleProperty().unbind();
        if (messageStatusPanel != null) messageStatusPanel.visibleProperty().unbind();
        if (statusIconSvg != null) {
            statusIconSvg.visibleProperty().unbind();
            statusIconSvg.contentProperty().unbind();
        }
        if (mutedIcon != null) mutedIcon.visibleProperty().unbind();
        if (pinnedIcon != null) pinnedIcon.visibleProperty().unbind();
        if (currentUser != null && messageStatusListener != null) {
            currentUser.messageStatusProperty().removeListener(messageStatusListener);
            messageStatusListener = null;
        }
    }

    private String stripFormattingForPreview(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;

        // 1. Spoilers: ||content|| -> ██████
        result = result.replaceAll("\\|\\|.*?\\|\\|", "██████");

        // 2. Bold: **content** -> content
        result = result.replaceAll("\\*\\*(.*?)\\*\\*", "$1");

        // 3. Italic: __content__ -> content
        result = result.replaceAll("__(.*?)__", "$1");

        // 4. Underline: ++content++ -> content
        result = result.replaceAll("\\+\\+(.*?)\\+\\+", "$1");

        return result;
    }
    // Update cell appearance and content

    private void updateCellContent() {
        updateAvatar();
        updateTypeLogo();
    }

    private void updateTypeLogo() {
        if (typeLogo == null) return;

        UserType type = currentUser.getType();
        typeLogo.setVisible(false);

        try {
            switch (type) {
                case USER -> typeLogo.setVisible(false);
                case GROUP -> {
                    typeLogo.setContent("M13,13 C15.2091,13 17,14.7909 17,17 L17,18.5 C17,19.3284 16.3284,20 15.5,20 L3.5,20 C2.67157,20 2,19.3284 2,18.5 L2,17 C2,14.7909 3.79086,13 6,13 L13,13 Z M19,13.0002 C20.6569,13.0002 22,14.3434 22,16.0002 L22,17.5002 C22,18.3287 21.3284,19.0002 20.5,19.0002 L19,19.0002 L19,17 C19,15.3645 18.2148,13.9125 17.0008,13.0002 L19,13.0002 Z M9.5,3 C11.9853,3 14,5.01472 14,7.5 C14,9.98528 11.9853,12 9.5,12 C7.01472,12 5,9.98528 5,7.5 C5,5.01472 7.01472,3 9.5,3 Z M18,6 C19.6569,6 21,7.34315 21,9 C21,10.6569 19.6569,12 18,12 C16.3431,12 15,10.6569 15,9 C15,7.34315 16.3431,6 18,6 Z");
                    typeLogo.setScaleX(0.65);
                    typeLogo.setScaleY(0.65);
                    typeLogo.setTranslateX(0);
                    typeLogo.setTranslateY(0);
                    typeLogo.setVisible(true);
                }
                case SUPERGROUP -> {
                    typeLogo.setContent("M16.2,16.4A4.2,4.2,0,0,0,12,12.2h0A4.2,4.2,0,0,0,7.8,16.4Z M17.6,16.4H22.4A4.2,4.2,0,0,0,18.2,12.2h0a4.2,4.2,0,0,0-2,0.5 M6.4,16.4H1.6A4.2,4.2,0,0,1,5.8,12.2h0a4.2,4.2,0,0,1,2,0.5 M12 8.5 m-2.1 0 a2.1 2.1 0 1 1 4.2 0 a2.1 2.1 0 1 1 -4.2 0 M18.2 8.5 m-2.1 0 a2.1 2.1 0 1 1 4.2 0 a2.1 2.1 0 1 1 -4.2 0 M5.8 8.5 m-2.1 0 a2.1 2.1 0 1 1 4.2 0 a2.1 2.1 0 1 1 -4.2 0");
                    typeLogo.setScaleX(1);
                    typeLogo.setScaleY(1);
                    typeLogo.setTranslateX(0);
                    typeLogo.setTranslateY(0);
                    typeLogo.setVisible(true);
                }
                case CHANNEL -> {
                    typeLogo.setContent("M183.795,118.154 L183.795,301.949 L393.846,393.846 L393.846,26.256 M433.231,131.282V288.82c43.503,0,78.769-35.266,78.769-78.769S476.734,131.282,433.231,131.282z M98.462,118.154V91.897H59.077v26.256H39.385C17.634,118.154,0,135.788,0,157.538v105.026c0,21.751,17.634,39.385,39.385,39.385H144.41V118.154H98.462z M120.174,341.333 41.405,341.333 52.513,485.744 131.282,485.744");
                    typeLogo.setScaleX(0.027);
                    typeLogo.setScaleY(0.027);
                    typeLogo.setTranslateX(0);
                    typeLogo.setTranslateY(2.5);
                    typeLogo.setVisible(true);
                }
                case ADMIN -> {
                    typeLogo.setContent("M440.367,440.415l-10.173-29.91c-19.102-56.262-70.83-94.605-129.763-97.121c-5.187,4.03-10.655,7.493-16.322,10.521 c-8.449,22.185-36.836,28.195-53.468,11.205c-19.676-1.738-37.69-9.511-53.422-21.725c-58.933,2.508-110.647,40.851-129.763,97.121 L37.3,440.415c-2.936,8.603-1.522,18.084,3.774,25.469c5.279,7.391,13.821,11.771,22.906,11.771h349.693 c9.083,0,17.626-4.379,22.906-11.771C441.873,458.499,443.286,449.018,440.367,440.415z M277.758,290.619c34.212-24.047,58.141-77.151,58.141-128.491c0-145.907-194.133-145.752-194.133,0 c0,62.397,35.33,127.303,81.546,139.556c4.456-12.626,16.382-21.757,30.515-21.757C263.331,279.926,271.81,284.095,277.758,290.619z M99.169,223.042c4.813,18.906,31.044,13.704,31.044-3.805c0-70.178,3.354-76.731-6.041-84.348 C145.679,2.361,330.946,3.355,353.495,134.904c-9.381,7.641-6.025,14.163-6.025,84.333c0,5.489,2.95,10.095,7.189,12.952 c0,54.594-22.145,51.402-88.736,69.052c-10.295-11.174-28.683-3.899-28.683,11.173c0,18.876,27.053,23.293,32.302,4.318 c53.762-14.256,101.018-18.752,101.018-72.484v-11.027c3.991-2.066,6.817-5.729,7.951-10.179c51.822-1.056,51.838-78.719,0-79.775 c-1.072-4.24-3.711-7.703-7.423-9.815c1.336-15.902-1.94-36.805-11.057-56.985C296.626-54.368,109.355-3.176,106.422,123.622 c-0.404,4.294-0.078,7.338,0.17,9.83c-3.712,2.112-6.351,5.575-7.423,9.815c-21.71,0.419-39.212,18.084-39.212,39.888 C59.957,204.958,77.459,222.622,99.169,223.042z");
                    typeLogo.setScaleX(0.030);
                    typeLogo.setScaleY(0.030);
                    typeLogo.setTranslateX(0);
                    typeLogo.setTranslateY(0);
                    typeLogo.setVisible(true);
                }
                default -> typeLogo.setVisible(false);
            }
        } catch (Exception e) {
            System.err.println("Error setting SVG path for type logo: " + type + ": " + e.getMessage());
            typeLogo.setVisible(false);
        }
    }
    private void updateAvatar() {
        if (avatarImage == null) return;

        // Set default avatar immediately to clear the old image from the recycled cell.
        // This prevents a "flicker" of the wrong user's avatar while the new one loads.
        loadDefaultAvatar();

        String avatarPath = currentUser.getAvatarId();
        if (currentUser.getType() == UserType.SAVED_MESSAGES){
            Image defaultSavedMessagesAvatar = new Image(Objects.requireNonNull(getClass().getResource("/Client/images/SavedMessagesProfile.png")).toExternalForm());
            avatarImage.setImage(defaultSavedMessagesAvatar);
        }
        else if (avatarPath != null && !avatarPath.isEmpty()) {
            loadAvatar(avatarPath);
        }
    }
    private void loadAvatar(String pictureId) {
         if (pictureId != null && !pictureId.isEmpty()) {
            FileDownloadService.getInstance().getImage(pictureId).thenAccept(avatar -> {
                // This check is crucial. The cell might have been recycled for another user
                // by the time the image has loaded. We only set the image if the cell
                // still represents the user for whom the request was made.
                if (avatar != null && currentUser != null && pictureId.equals(currentUser.getAvatarId())) {
                    Platform.runLater(() -> {
                        avatarImage.setImage(avatar);
                    });
                }
            }).exceptionally(e -> {
                // Handle cases where image loading fails.
                System.err.println("Failed to get or load profile avatar " + pictureId + ": " + e.getMessage());
                if (currentUser != null && pictureId.equals(currentUser.getAvatarId())) {
                    Platform.runLater(this::loadDefaultAvatar);
                }
                return null;
            });
        }
    }
    private void loadDefaultAvatar() {
        try {

            avatarImage.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Client/images/11Devs-white.png"))));
        } catch (Exception e) {
            System.err.println("Failed to load default avatar image.");
        }
    }

    private void updateCellAppearance() {
        if (root == null) return;

        if (isSelected) {
            root.getStyleClass().add("selected");
        } else {
            root.getStyleClass().remove("selected");
        }
    }

    private String formatTime(LocalDateTime messageTime, LocalDateTime now) {
        if (messageTime.toLocalDate().equals(now.toLocalDate())) {
            // Today: just show HH:mm
            return messageTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else if (messageTime.getYear() == now.getYear()) {
            // Same year: show MM/dd
            return messageTime.format(DateTimeFormatter.ofPattern("MM/dd"));
        } else {
            // Different year: show yyyy/MM/dd
            return messageTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        }
    }

    private void setupClickHandlers() {
        if (root != null) {
            root.setOnMouseClicked(event -> {
                // TODO: click handler logic here
            });
        }
    }
    private void updateNotificationBadgeStyle() {
        if (currentUser == null || notificationPanel == null) return;

        String count = currentUser.getNotificationsNumber();
        boolean hasUnread = count != null && !count.equals("0") && !count.isEmpty();

        // Clear previous state styles
        notificationPanel.getStyleClass().removeAll("active", "muted");
        root.getStyleClass().removeAll("unread", "unread-muted");

        if (hasUnread) {
            if (currentUser.isMuted()) {
                notificationPanel.getStyleClass().add("muted");
                root.getStyleClass().add("unread-muted");
            } else {
                notificationPanel.getStyleClass().add("active");
                root.getStyleClass().add("unread");
            }
        }
    }
    public void setSelected(boolean selected) {
        this.isSelected = selected;
        updateCellAppearance();
    }
}
