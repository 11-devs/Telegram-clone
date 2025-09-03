package Client.Controllers;

import Shared.Models.MessageViewModel;
import Shared.Models.UserType;
import Shared.Models.UserViewModel;
import Shared.Models.UserViewModelBuilder;
import Shared.Utils.TelegramCellUtils;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainChatController implements Initializable {

    // ============ FXML INJECTED COMPONENTS ============

    // Main container
    @FXML private BorderPane mainChatContainer;

    // Sidebar elements
    @FXML private VBox leftSidebar;
    @FXML private Button menuButton;
    @FXML private HBox searchContainer;
    @FXML private TextField searchField;
    @FXML private ListView<UserViewModel> chatListView;
    @FXML private Button settingsButton;
    @FXML private Button nightModeButton;
    @FXML private Circle connectionIndicator;
    @FXML private Label connectionLabel;

    // Chat header elements
    @FXML private ImageView headerAvatarImage;
    @FXML private Circle onlineIndicator;
    @FXML private Label chatTitleLabel;
    @FXML private Label chatSubtitleLabel;
    @FXML private Label membersCountLabel;
    @FXML private ImageView verifiedBadge;
    @FXML private ImageView mutedIcon;
    @FXML private Button searchInChatButton;
    @FXML private Button callButton;
    @FXML private Button videoCallButton;
    @FXML private Button moreOptionsButton;

    // Messages area
    @FXML private ScrollPane messagesScrollPane;
    @FXML private VBox messagesContainer;
    @FXML private VBox welcomeStateContainer;
    @FXML private VBox emptyChatStateContainer;
    @FXML private StackPane scrollToBottomContainer;
    @FXML private Button scrollToBottomButton;
    @FXML private Label unreadBadge;


    // Reply preview
    @FXML private HBox replyPreviewContainer;
    @FXML private Label replyToLabel;
    @FXML private Label replyMessageLabel;
    @FXML private Button closeReplyButton;

    // Message input area
    @FXML private Button attachmentButton;
    @FXML private TextArea messageInputField;
    @FXML private Button emojiButton;
    @FXML private Button sendButton;
    @FXML private ImageView sendButtonIcon;

    // Right panel elements
    @FXML private VBox rightPanel;
    @FXML private ImageView profileAvatarImage;
    @FXML private Circle profileOnlineIndicator;
    @FXML private Label profileNameLabel;
    @FXML private Label profileUsernameLabel;
    @FXML private Label profileStatusLabel;
    @FXML private Label profilePhoneLabel;
    @FXML private ImageView profileVerifiedBadge;
    @FXML private Button profileCallButton;
    @FXML private Button profileVideoButton;
    @FXML private Button profileSearchButton;
    @FXML private Label notificationStatusLabel;
    @FXML private Button notificationsToggle;
    @FXML private Button showAllMediaButton;
    @FXML private ToggleButton mediaTab;
    @FXML private ToggleButton filesTab;
    @FXML private ToggleButton linksTab;
    @FXML private ToggleButton musicTab;
    @FXML private ScrollPane mediaScrollPane;
    @FXML private GridPane mediaGrid;
    @FXML private VBox emptyMediaState;

    // ============ DATA AND STATE ============

    private ObservableList<UserViewModel> allChatUsers;
    private ObservableList<UserViewModel> filteredChatUsers;
    private UserViewModel currentSelectedUser;
    private ObservableList<MessageViewModel> currentMessages;
    private MessageViewModel replyToMessage;

    // Animation timelines
    private Timeline typingAnimationTimeline;
    private Timeline onlineStatusTimeline;
    private Timeline connectionStatusTimeline;

    // State flags
    private boolean isDarkTheme = true;
    private boolean isRightPanelVisible = false;
    private boolean isTypingIndicatorVisible = false;
    private String currentMediaFilter = "media";
    private int unreadScrollCount = 0;

    // Sidebars settings
    private double leftInitialX;
    private double rightInitialX;

    // Sidebar Menu
    private SidebarMenuController sidebarController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeSidebarsSplitPane();
        initializeData();
        setupChatList();
        setupMessageInput();
        setupEventHandlers();
        setupAnimations();
        // setupKeyboardShortcuts(); // TODO
        loadInitialState();
    }

    // ============ INITIALIZATION METHODS ============

    private void initializeSidebarsSplitPane() {
        // Convert BorderPane to SplitPane to support dragging
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(leftSidebar, mainChatContainer.getCenter(), rightPanel);
        mainChatContainer.setCenter(splitPane);

        // Setting minimum and maximum widths for panels
        SplitPane.setResizableWithParent(leftSidebar, Boolean.TRUE);
        SplitPane.setResizableWithParent(rightPanel, Boolean.TRUE);
        leftSidebar.setMinWidth(300.0);
        leftSidebar.setMaxWidth(420.0);
        rightPanel.setMinWidth(300.0);
        rightPanel.setMaxWidth(420.0);

        // Ensuring that panels don't take up the entire screen
        splitPane.setDividerPositions(0.25, 0.75); // 25% left, 50% center, 25% right
        splitPane.getDividers().get(0).positionProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() < 0.2 || newVal.doubleValue() > 0.4) {
                splitPane.setDividerPosition(0, oldVal.doubleValue()); // Expansion limit
            }
        });
        splitPane.getDividers().get(1).positionProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() < 0.6 || newVal.doubleValue() > 0.8) {
                splitPane.setDividerPosition(1, oldVal.doubleValue()); // Expansion limit
            }
        });
    }

    private void initializeData() {
        allChatUsers = FXCollections.observableArrayList();
        filteredChatUsers = FXCollections.observableArrayList();
        currentMessages = FXCollections.observableArrayList();

        // Load sample data for demonstration
        loadSampleChats();
        // TODO: Get data from data base
    }

    // TODO: Replace with real data
    // TODO: This section will be deleted (This is an example)
    private void loadSampleChats() {
        UserViewModel user1 = new UserViewModelBuilder()
                .userName("Alice Johnson")
                .lastMessage("Hey there! How are you doing?")
                .time("13:06")
                .isOnline(true)
                .notificationsNumber("3")
                .isVerified(true)
                .type(UserType.USER.name()).bio("Hi im Alice").phoneNumber("+981111111111")
                .build();

        UserViewModel user2 = new UserViewModelBuilder()
                .userName("Bob Smith")
                .lastMessage("Let's meet tomorrow")
                .time("13:00")
                .isOnline(false)
                .lastSeen("last seen 2 hours ago")
                .type(UserType.USER.name()).bio("Hi im Bob").phoneNumber("+982222222222")
                .build();

        UserViewModel group1 = new UserViewModelBuilder()
                .userName("Development Team")
                .lastMessage("John: Great work on the project!")
                .time("12:20")
                .notificationsNumber("12")
                .isPinned(true)
                .type(UserType.GROUP.name()).bio("Hi we are Development Team")
                .build();

        UserViewModel channel1 = new UserViewModelBuilder()
                .userName("Tech News")
                .lastMessage("Latest updates in technology")
                .time("11:15")
                .isMuted(true)
                .type(UserType.CHANNEL.name()).bio("Hi we are Tech News")
                .build();

        allChatUsers.addAll(user1, user2, group1, channel1);
        filteredChatUsers.setAll(allChatUsers);
    }

    private void setupChatList() {
        chatListView.setItems(filteredChatUsers);
        chatListView.setCellFactory(listView -> new UserCustomCell());

        // Handle chat selection
        chatListView.getSelectionModel().selectedItemProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser != null && newUser != currentSelectedUser) {
                selectChat(newUser);
            }
        });

        // Initially show welcome state
        showWelcomeState();
    }

    private void setupMessageInput() {
        // Auto-resize text area
        messageInputField.textProperty().addListener((obs, oldText, newText) -> {
            updateSendButtonState();
            adjustTextAreaHeight();
            handleTypingDetection();
        });

        // Handle Enter key for sending messages
        messageInputField.setOnKeyPressed(this::handleKeyPressed);

        // Focus handling
        messageInputField.focusedProperty().addListener((obs, oldFocused, newFocused) -> {
            if (newFocused) {
                TelegramCellUtils.addStyleClass(messageInputField.getParent(), "focused");
            } else {
                TelegramCellUtils.removeStyleClass(messageInputField.getParent(), "focused");
            }
        });

        // Initial state
        updateSendButtonState();
        disableChatControls();
    }

    private void setupEventHandlers() {
        // Sidebar buttons
        menuButton.setOnAction(e -> showSideBar());
        settingsButton.setOnAction(e -> openSettings());
        nightModeButton.setOnAction(e -> toggleTheme());

        // Search functionality
        searchField.textProperty().addListener((obs, oldText, newText) -> performSearch(newText));

        // Header buttons
        searchInChatButton.setOnAction(e -> showSearchInChat());
        callButton.setOnAction(e -> startVoiceCall());
        videoCallButton.setOnAction(e -> startVideoCall());
        moreOptionsButton.setOnAction(e -> showMoreOptions());

        // Message input buttons
        attachmentButton.setOnAction(e -> showAttachmentOptions());
        emojiButton.setOnAction(e -> showEmojiPicker());
        sendButton.setOnAction(e -> handleSendAction());

        // Scroll and navigation
        scrollToBottomButton.setOnAction(e -> scrollToBottom());

        // Reply functionality
        closeReplyButton.setOnAction(e -> closeReplyPreview());

        // Right panel buttons
        profileCallButton.setOnAction(e -> startVoiceCall());
        profileVideoButton.setOnAction(e -> startVideoCall());
        profileSearchButton.setOnAction(e -> showSearchInChat());
        notificationsToggle.setOnAction(e -> toggleNotifications());
        showAllMediaButton.setOnAction(e -> showAllMedia());

        // Media tabs
        mediaTab.setOnAction(e -> switchMediaFilter("media"));
        filesTab.setOnAction(e -> switchMediaFilter("files"));
        linksTab.setOnAction(e -> switchMediaFilter("links"));
        musicTab.setOnAction(e -> switchMediaFilter("music"));

        // Scroll listener for messages
        messagesScrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            updateScrollToBottomVisibility();
            handleScrollPositionChange(newVal.doubleValue());
        });
    }

    // TODO: Connect to the server
    private void setupAnimations() {

        // Connection status pulse
        connectionStatusTimeline = TelegramCellUtils.createOnlineStatusPulse(connectionIndicator);
        connectionStatusTimeline.play();

        // Online status animation
        onlineStatusTimeline = TelegramCellUtils.createOnlineStatusPulse(onlineIndicator);
    }

    // TODO: Dev in the future
//    private void setupKeyboardShortcuts() {
//        mainChatContainer.setOnKeyPressed(event -> {
//            if (event.isControlDown() || event.isMetaDown()) {
//                switch (event.getCode()) {
//                    case N -> ;
//                    case F -> searchField.requestFocus();
//                    case W -> closeCurrentChat();
//                    case DIGIT1 -> selectChatFilter(allChatsFilter);
//                    case DIGIT2 -> selectChatFilter(unreadChatsFilter);
//                    case DIGIT3 -> selectChatFilter(favoritesFilter);
//                    case COMMA -> openSettings();
//                    case SLASH -> searchInChatButton.fire();
//                    case I -> toggleRightPanel();
//                    case D -> toggleTheme();
//                }
//            } else if (event.getCode() == KeyCode.ESCAPE) {
//                handleEscapeKey();
//            }
//        });
//    }

    private void loadInitialState() {
        // Set initial theme
        updateThemeClasses();

        // Update connection status
        updateConnectionStatus(true); // TODO: connect to the server

        // Show welcome state
        showWelcomeState();

        // Initialize right panel state
        hideRightPanel();
    }

    public void setSidebarController(SidebarMenuController controller) {
        this.sidebarController = controller;
    }

    // ============ CHAT MANAGEMENT ============

    private void selectChat(UserViewModel user) {
        if (user == null) return;

        currentSelectedUser = user;

        // Update UI state
        hideWelcomeState();
        showChatArea();
        updateChatHeader(user);
        loadMessages(user);
        enableChatControls();

        // Clear notifications
        user.clearUnreadCount();

        // Update right panel if visible
        if (isRightPanelVisible) {
            updateRightPanel(user);
        }

        // Animate selection
        animateChatSelection();
    }

    private void updateChatHeader(UserViewModel user) {
        // Update chat title
        chatTitleLabel.setText(user.getUserName());

        // Update subtitle based on user state
        updateChatSubtitle(user);

        // Update avatar
        updateHeaderAvatar(user);

        // Update badges and indicators
        verifiedBadge.setVisible(user.isVerified());
        mutedIcon.setVisible(user.isMuted());
        onlineIndicator.setVisible(user.isOnline() && user.getType() == UserType.USER);

        // Update members count for groups
        if (user.getType() == UserType.GROUP || user.getType() == UserType.SUPERGROUP) {
            membersCountLabel.setText(user.getMembersCount() + " members");
            membersCountLabel.setVisible(true);
        } else {
            membersCountLabel.setVisible(false);
        }

        // Start online status animation if user is online
        if (user.isOnline() && onlineStatusTimeline != null) {
            onlineStatusTimeline.play();
        }
    }

    private void updateChatSubtitle(UserViewModel user) {
        if (user.isTyping()) {
            chatSubtitleLabel.setText("typing...");
            chatSubtitleLabel.getStyleClass().setAll("chat-subtitle", "typing-indicator");
            // showTypingIndicator(user.getUserName()); // TODO: optional
        } else if (user.isOnline() && user.getType() == UserType.USER) {
            chatSubtitleLabel.setText("online");
            chatSubtitleLabel.getStyleClass().setAll("chat-subtitle");
            // hideTypingIndicator(); // TODO: optional
        } else if (user.getLastSeen() != null && !user.getLastSeen().isEmpty()) {
            chatSubtitleLabel.setText(user.getLastSeen());
            chatSubtitleLabel.getStyleClass().setAll("chat-subtitle");
            // hideTypingIndicator(); // TODO: optional
        } else {
            chatSubtitleLabel.setText("offline");
            chatSubtitleLabel.getStyleClass().setAll("chat-subtitle");
            // hideTypingIndicator();
        }
    }

    private void updateHeaderAvatar(UserViewModel user) {
        if (user.getAvatarPath() != null && !user.getAvatarPath().isEmpty()) {
            try {
                Image avatar = new Image(user.getAvatarPath());
                headerAvatarImage.setImage(avatar);
            } catch (Exception e) {
                loadDefaultHeaderAvatar();
            }
        } else {
            loadDefaultHeaderAvatar();
        }
    }

    private void loadMessages(UserViewModel user) {
        messagesContainer.getChildren().clear();

        if (user.getType() == UserType.USER) {
            loadUserMessages(user);
        } else if (user.getType() == UserType.GROUP || user.getType() == UserType.SUPERGROUP) {
            loadGroupMessages(user);
        } else if (user.getType() == UserType.CHANNEL) {
            loadChannelMessages(user);
        }

        // Scroll to bottom after loading
        Platform.runLater(this::scrollToBottom);
    }

    // TODO: Replace with real data
    // TODO: This section will be deleted (This is an example)
    private void loadUserMessages(UserViewModel user) {
        // Sample private chat messages
        addMessageBubble("Hey! How's your day going?", false, "10:30", "read", user.getUserName());
        addMessageBubble("Pretty good! Just finished a big project at work 🎉", true, "10:32", "read", null);
        addMessageBubble("That's awesome! What was the project about?", false, "10:33", "read", user.getUserName());
        addMessageBubble("It was a new mobile app for our company. Took 3 months to complete!", true, "10:35", "delivered", null);

        if (user.hasUnreadMessages()) {
            addMessageBubble("Congratulations! 🎊 Would love to hear more about it", false, getCurrentTime(), "delivered", user.getUserName());
        }
    }

    // TODO: Replace with real data
    // TODO: This section will be deleted (This is an example)
    private void loadGroupMessages(UserViewModel group) {
        // Sample group chat messages
        addMessageBubble("Hey everyone! Meeting at 3 PM today", false, "09:15", "read", "John");
        addMessageBubble("Sounds good! I'll be there", true, "09:16", "read", null);
        addMessageBubble("Can we move it to 3:30? I'm running a bit late", false, "09:18", "read", "Sarah");
        addMessageBubble("Sure, no problem! 3:30 it is", false, "09:20", "read", "Mike");
        addMessageBubble("Perfect! See you all then 👍", true, "09:21", "delivered", null);
    }

    // TODO: Replace with real data
    // TODO: This section will be deleted (This is an example)
    private void loadChannelMessages(UserViewModel channel) {
        // Sample channel messages
        addMessageBubble("📢 New update available! Check out the latest features in version 2.1", false, "08:00", "read", channel.getUserName());
        addMessageBubble("🔥 Hot topic: AI advances in 2025", false, "07:30", "read", channel.getUserName());
        addMessageBubble("💡 Tip of the day: Use keyboard shortcuts to boost productivity", false, "06:45", "read", channel.getUserName());
    }

    // ============ MESSAGE HANDLING ============

    private void addMessageBubble(String text, boolean isOutgoing, String time, String status, String senderName) {
        HBox messageContainer = new HBox();
        messageContainer.setSpacing(12);
        messageContainer.setPadding(new Insets(4, 0, 4, 0));

        if (isOutgoing) {
            messageContainer.setAlignment(Pos.CENTER_RIGHT);
            VBox bubble = createMessageBubble(text, time, status, true, null);
            messageContainer.getChildren().add(bubble);
        } else {
            messageContainer.setAlignment(Pos.CENTER_LEFT);

            // Add sender avatar for group chats
            if (currentSelectedUser != null &&
                    (currentSelectedUser.getType() == UserType.GROUP || currentSelectedUser.getType() == UserType.SUPERGROUP)) {
                ImageView senderAvatar = createSenderAvatar(senderName);
                messageContainer.getChildren().add(senderAvatar);
            }

            VBox bubble = createMessageBubble(text, time, status, false, senderName);
            messageContainer.getChildren().add(bubble);
        }

        messagesContainer.getChildren().add(messageContainer);

        // Animate new message if it's being added in real-time
        TelegramCellUtils.animateNewMessage(messageContainer);
    }

    private VBox createMessageBubble(String text, String time, String status, boolean isOutgoing, String senderName) {
        VBox bubble = new VBox();
        bubble.setSpacing(4);
        bubble.getStyleClass().addAll("message-bubble", isOutgoing ? "outgoing" : "incoming");
        bubble.setMaxWidth(420);

        // Add sender name for incoming group messages
        if (!isOutgoing && senderName != null && currentSelectedUser != null &&
                (currentSelectedUser.getType() == UserType.GROUP || currentSelectedUser.getType() == UserType.SUPERGROUP)) {
            Label senderLabel = new Label(senderName);
            senderLabel.getStyleClass().add("sender-name");
            bubble.getChildren().add(senderLabel);
        }

        // Message text
        Label messageText = new Label(text);
        messageText.getStyleClass().addAll("message-text", isOutgoing ? "outgoing" : "incoming");
        messageText.setWrapText(true);

        // Time and status container
        HBox timeContainer = new HBox();
        timeContainer.setSpacing(4);
        timeContainer.setAlignment(Pos.CENTER_RIGHT);

        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().addAll("message-time", isOutgoing ? "outgoing" : "incoming");
        timeContainer.getChildren().add(timeLabel);

        // Add status for outgoing messages
        if (isOutgoing && status != null) {
            Label statusLabel = new Label(getStatusIcon(status));
            statusLabel.getStyleClass().addAll("message-status", status);
            timeContainer.getChildren().add(statusLabel);
        }

        bubble.getChildren().addAll(messageText, timeContainer);

        // Add click handler for message options
        bubble.setOnMouseClicked(this::handleMessageClick);

        return bubble;
    }

    private ImageView createSenderAvatar(String senderName) {
        ImageView avatar = new ImageView();
        avatar.setFitWidth(32);
        avatar.setFitHeight(32);
        avatar.setPreserveRatio(true);

        // Create circular clip
        Circle clip = new Circle(16, 16, 16);
        avatar.setClip(clip);

        // Load default or sender-specific avatar
        try {
            // In a real app, you'd load the actual sender's avatar
            Image defaultAvatar = new Image(Objects.requireNonNull(getClass().getResource("/Client/images/11Devs-black.png")).toExternalForm());
            avatar.setImage(defaultAvatar);
        } catch (Exception e) {
            System.err.println("Failed to load sender avatar for: " + senderName);
        }

        avatar.getStyleClass().add("sender-avatar");
        return avatar;
    }

    private void handleMessageClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            // Double-click to reply
            VBox bubble = (VBox) event.getSource();
            showReplyPreview(bubble);
        } else if (event.isSecondaryButtonDown()) {
            // Right-click for context menu
            showMessageContextMenu(event);
        }
    }

    private void showReplyPreview(VBox messageBubble) {
        if (replyPreviewContainer == null) return;

        // Extract message info
        Label messageText = (Label) messageBubble.getChildren().get(
                messageBubble.getChildren().size() == 3 ? 1 : 0 // Account for sender name
        );

        replyToLabel.setText(currentSelectedUser != null ? currentSelectedUser.getUserName() : "User");
        replyMessageLabel.setText(messageText.getText().length() > 50 ?
                messageText.getText().substring(0, 47) + "..." : messageText.getText());

        replyPreviewContainer.setVisible(true);

        // Animate reply preview
        replyPreviewContainer.setTranslateY(-30);
        replyPreviewContainer.setOpacity(0);

        TranslateTransition slideDown = new TranslateTransition(Duration.millis(200), replyPreviewContainer);
        slideDown.setToY(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), replyPreviewContainer);
        fadeIn.setToValue(1.0);

        ParallelTransition showReply = new ParallelTransition(slideDown, fadeIn);
        showReply.play();

        // Focus message input
        messageInputField.requestFocus();
    }

    @FXML
    private void closeReplyPreview() {
        if (replyPreviewContainer == null || !replyPreviewContainer.isVisible()) return;

        TranslateTransition slideUp = new TranslateTransition(Duration.millis(150), replyPreviewContainer);
        slideUp.setToY(-30);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), replyPreviewContainer);
        fadeOut.setToValue(0);

        ParallelTransition hideReply = new ParallelTransition(slideUp, fadeOut);
        hideReply.setOnFinished(e -> {
            replyPreviewContainer.setVisible(false);
            replyPreviewContainer.setManaged(false);
            replyToMessage = null;
        });
        hideReply.play();
    }

    // ============ FILTER AND SEARCH ============

    private void performSearch(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return;
        }

        ObservableList<UserViewModel> searchResults = FXCollections.observableArrayList();
        String lowerCaseSearch = searchText.toLowerCase();

        for (UserViewModel user : allChatUsers) {
            if (user.getUserName().toLowerCase().contains(lowerCaseSearch) ||
                    (user.getLastMessage() != null && user.getLastMessage().toLowerCase().contains(lowerCaseSearch))) {
                searchResults.add(user);
            }
        }

        filteredChatUsers.setAll(searchResults);
    }

    // ============ MESSAGE INPUT HANDLING ============

    // TODO
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (event.isShiftDown()) {
                // Allow new line with Shift+Enter
                return;
            } else {
                // Send message with Enter
                event.consume();
                sendMessage();
            }
        } else if (event.getCode() == KeyCode.ESCAPE) {
            if (replyPreviewContainer.isVisible()) {
                closeReplyPreview();
            }
        } else if (event.getCode() == KeyCode.UP && event.isControlDown()) {
            // Ctrl+Up to edit last message
            editLastMessage();
        }
    }

    // TODO
    private void handleSendAction() {
        String text = messageInputField.getText().trim();
        if (!text.isEmpty()) {
            sendMessage();
        } else {
            // Handle voice message recording
            startVoiceRecording();
        }
    }

    private void sendMessage() {
        String text = messageInputField.getText().trim();
        if (text.isEmpty() || currentSelectedUser == null) return;

        // Create message with reply if present
        String messageText = text;
        if (replyPreviewContainer.isVisible()) {
            messageText = "↪ " + replyMessageLabel.getText() + "\n\n" + text;
            closeReplyPreview();
        }

        // Add message to chat
        addMessageBubble(messageText, true, getCurrentTime(), "sent", null);

        // Clear input
        messageInputField.clear();
        updateSendButtonState();

        // Update chat list
        currentSelectedUser.setLastMessage(text);
        currentSelectedUser.setTime(getCurrentTime());
        refreshChatList();

        // Simulate message delivery
        simulateMessageDelivery();

        // Focus input for next message
        Platform.runLater(() -> messageInputField.requestFocus());
    }

    private void simulateMessageDelivery() {
        // Simulate delivered status after 1 second
        Timeline deliveredTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            updateLastMessageStatus("delivered");
        }));

        // Simulate read status after 2.5 seconds
        Timeline readTimeline = new Timeline(new KeyFrame(Duration.seconds(2.5), e -> {
            updateLastMessageStatus("read");
        }));

        deliveredTimeline.play();
        readTimeline.play();
    }

    private void updateLastMessageStatus(String status) {
        if (messagesContainer.getChildren().isEmpty()) return;

        HBox lastMessageContainer = (HBox) messagesContainer.getChildren().getLast();
        VBox bubble = (VBox) lastMessageContainer.getChildren().getLast();
        HBox timeContainer = (HBox) bubble.getChildren().getLast();

        if (timeContainer.getChildren().size() > 1) {
            Label statusLabel = (Label) timeContainer.getChildren().get(1);
            statusLabel.setText(getStatusIcon(status));
            statusLabel.getStyleClass().removeAll("sent", "delivered", "read");
            statusLabel.getStyleClass().add(status);
        }
    }

    private void updateSendButtonState() {
        String text = messageInputField.getText().trim();

        try {
            if (text.isEmpty()) {
                // Show microphone for voice messages
                Image micIcon = new Image(Objects.requireNonNull(getClass().getResource("../images/microphone-icon.png")).toExternalForm()); // TODO
                sendButtonIcon.setImage(micIcon);
                sendButton.getStyleClass().add("voice-mode");
            } else {
                // Show send icon for text messages
                Image sendIcon = new Image(Objects.requireNonNull(getClass().getResource("../images/send-icon.png")).toExternalForm()); // TODO
                sendButtonIcon.setImage(sendIcon);
                sendButton.getStyleClass().remove("voice-mode");
            }
        } catch (Exception e) {
            System.err.println("Error updating send button icon: " + e.getMessage());
        }
    }

    private void adjustTextAreaHeight() {
        Platform.runLater(() -> {
            Text measureText = new Text(messageInputField.getText());
            measureText.setFont(messageInputField.getFont());
            measureText.setWrappingWidth(messageInputField.getWidth() - 32); // Account for padding

            double textHeight = measureText.getBoundsInLocal().getHeight();
            double lineHeight = measureText.getFont().getSize() * 1.2;
            double lines = Math.max(1, Math.ceil(textHeight / lineHeight));

            double newHeight = Math.max(40, Math.min(120, lines * lineHeight + 20));
            messageInputField.setPrefHeight(newHeight);

            // Add visual feedback for expansion
            if (newHeight > 40) {
                TelegramCellUtils.addStyleClass(messageInputField, "expanded");
            } else {
                TelegramCellUtils.removeStyleClass(messageInputField, "expanded");
            }
        });
    }

    // TODO (Server)
    private void handleTypingDetection() {
        // Implement typing detection logic
        // This would typically send typing status to server
        if (currentSelectedUser != null && !messageInputField.getText().trim().isEmpty()) {
            // Send typing indicator to other users
            sendTypingStatus(true);
        }
    }

    // ============ UI STATE MANAGEMENT ============

    private void showWelcomeState() {
        if (welcomeStateContainer != null) {
            welcomeStateContainer.setVisible(true);
            emptyChatStateContainer.setVisible(false);
            messagesScrollPane.setVisible(false);

            // Animate welcome state
            TelegramCellUtils.animateNotificationBadge(welcomeStateContainer, true);
        }

        disableChatControls();
        hideRightPanel();
    }

    private void hideWelcomeState() {
        if (welcomeStateContainer != null) {
            welcomeStateContainer.setVisible(false);
        }
    }

    private void showChatArea() {
        messagesScrollPane.setVisible(true);
        emptyChatStateContainer.setVisible(false);
    }

    private void showEmptyChatState() {
        emptyChatStateContainer.setVisible(true);
        messagesScrollPane.setVisible(false);
        welcomeStateContainer.setVisible(false);
    }

    private void enableChatControls() {
        messageInputField.setDisable(false);
        sendButton.setDisable(false);
        attachmentButton.setDisable(false);
        emojiButton.setDisable(false);
        callButton.setDisable(false);
        videoCallButton.setDisable(false);
        searchInChatButton.setDisable(false);
    }

    private void disableChatControls() {
        messageInputField.setDisable(true);
        sendButton.setDisable(true);
        attachmentButton.setDisable(true);
        emojiButton.setDisable(true);
        callButton.setDisable(true);
        videoCallButton.setDisable(true);
        searchInChatButton.setDisable(true);
    }

    // ============ RIGHT PANEL MANAGEMENT ============

    private void toggleRightPanel() {
        if (currentSelectedUser == null) return;

        if (isRightPanelVisible) {
            hideRightPanel();
        } else {
            showRightPanel();
        }
    }

    private void showRightPanel() {
        if (rightPanel == null || currentSelectedUser == null) return;

        updateRightPanel(currentSelectedUser);

        rightPanel.setManaged(true);
        rightPanel.setTranslateX(350);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(250), rightPanel);
        slideIn.setToX(0);
        slideIn.setInterpolator(Interpolator.EASE_OUT);
        slideIn.setOnFinished(e -> isRightPanelVisible = true);
        slideIn.play();
    }

    private void hideRightPanel() {
        if (rightPanel == null) return;

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(250), rightPanel);
        slideOut.setToX(350);
        slideOut.setInterpolator(Interpolator.EASE_IN);
        slideOut.setOnFinished(e -> {
            rightPanel.setManaged(false);
            isRightPanelVisible = false;
        });
        slideOut.play();
    }

    private void updateRightPanel(UserViewModel user) {
        if (user == null) return;

        // Update profile info
        profileNameLabel.setText(user.getUserName());

        if (user.getUserName() != null && !user.getUserId().isEmpty()) {
            profileUsernameLabel.setText("@" + user.getUserId());
            profileUsernameLabel.setVisible(true);
        } else {
            profileUsernameLabel.setVisible(false);
        }

        profileStatusLabel.setText(user.isOnline() ? "online" :
                (user.getLastSeen() != null ? user.getLastSeen() : "offline"));

        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            profilePhoneLabel.setText(user.getPhoneNumber());
            profilePhoneLabel.setVisible(true);
        } else {
            profilePhoneLabel.setVisible(false);
        }

        // Update avatar
        updateProfileAvatar(user);

        // Update badges and indicators
        profileVerifiedBadge.setVisible(user.isVerified());
        profileOnlineIndicator.setVisible(user.isOnline());

        // Update notification status
        notificationStatusLabel.setText(user.isMuted() ? "Disabled" : "Enabled");
        updateNotificationToggle(!user.isMuted());

        // Update media section
        updateMediaSection(user);
    }

    private void updateProfileAvatar(UserViewModel user) {
        if (profileAvatarImage == null) return;

        if (user.getAvatarPath() != null && !user.getAvatarPath().isEmpty()) {
            try {
                Image avatar = new Image(user.getAvatarPath());
                profileAvatarImage.setImage(avatar);
            } catch (Exception e) {
                loadDefaultProfileAvatar();
            }
        } else {
            loadDefaultProfileAvatar();
        }
    }

    // ============ MEDIA SECTION ============

    private void switchMediaFilter(String filterType) {
        currentMediaFilter = filterType;

        // Update tab selection
        ToggleGroup mediaGroup = new ToggleGroup();
        mediaTab.setToggleGroup(mediaGroup);
        filesTab.setToggleGroup(mediaGroup);
        linksTab.setToggleGroup(mediaGroup);
        musicTab.setToggleGroup(mediaGroup);

        switch (filterType) {
            case "media" -> mediaTab.setSelected(true);
            case "files" -> filesTab.setSelected(true);
            case "links" -> linksTab.setSelected(true);
            case "music" -> musicTab.setSelected(true);
        }

        updateMediaGrid(filterType);
    }

    private void updateMediaGrid(String filterType) {
        if (mediaGrid == null) return;

        mediaGrid.getChildren().clear();

        // Simulate loading media items //TODO
        boolean hasMedia = simulateMediaLoading(filterType);

        if (hasMedia) {
            emptyMediaState.setVisible(false);
            mediaScrollPane.setVisible(true);
        } else {
            emptyMediaState.setVisible(true);
            mediaScrollPane.setVisible(false);
        }
    }

    // TODO: (Get count from server)
    private boolean simulateMediaLoading(String filterType) {
        // Simulate different amounts of media for different types
        int itemCount = switch (filterType) {
            case "media" -> 8;
            case "files" -> 3;
            case "links" -> 5;
            case "music" -> 2;
            default -> 0;
        };

        for (int i = 0; i < itemCount; i++) {
            StackPane mediaItem = createMediaItem(filterType, i);
            mediaGrid.add(mediaItem, i % 3, i / 3);
        }

        return itemCount > 0;
    }

    // TODO: replace with real server logic
    private StackPane createMediaItem(String type, int index) {
        StackPane item = new StackPane();
        item.setPrefSize(80, 80);
        item.getStyleClass().add("media-item");

        // Create placeholder content based on type
        ImageView placeholder = new ImageView();
        placeholder.setFitWidth(80);
        placeholder.setFitHeight(80);
        placeholder.setPreserveRatio(true);

        try {
            String imagePath = switch (type) {
                case "media" -> "../images/photo-placeholder.png"; // TODO
                case "files" -> "../images/file-placeholder.png";
                case "links" -> "../images/link-placeholder.png";
                case "music" -> "../images/music-placeholder.png";
                default -> "../images/default-placeholder.png";
            };

            Image image = new Image(Objects.requireNonNull(getClass().getResource(imagePath)).toExternalForm());
            placeholder.setImage(image);
        } catch (Exception e) {
            // Create colored rectangle as fallback
            Region coloredRect = new Region();
            coloredRect.setPrefSize(80, 80);
            coloredRect.getStyleClass().add("media-placeholder");
            item.getChildren().add(coloredRect);
            return item;
        }

        item.getChildren().add(placeholder);

        // Add click handler
        item.setOnMouseClicked(e -> openMediaItem(type, index));

        return item;
    }

    private void updateMediaSection(UserViewModel user) {
        if (user == null) return;

        // Update media grid based on current filter
        updateMediaGrid(currentMediaFilter);
    }

    // ============ SCROLL MANAGEMENT ============

    private void scrollToBottom() {
        Platform.runLater(() -> {
            messagesScrollPane.setVvalue(1.0);
            hideScrollToBottomButton();
        });
    }

    private void updateScrollToBottomVisibility() {
        double scrollValue = messagesScrollPane.getVvalue();
        boolean shouldShow = scrollValue < 0.85 && !messagesContainer.getChildren().isEmpty();

        if (shouldShow && !scrollToBottomContainer.isVisible()) {
            showScrollToBottomButton();
        } else if (!shouldShow && scrollToBottomContainer.isVisible()) {
            hideScrollToBottomButton();
        }
    }

    private void showScrollToBottomButton() {
        scrollToBottomContainer.setVisible(true);

        // Update unread count
        if (unreadScrollCount > 0) {
            unreadBadge.setText(String.valueOf(unreadScrollCount));
            unreadBadge.setVisible(true);
        }

        TelegramCellUtils.animateNotificationBadge(scrollToBottomContainer, true);
    }

    private void hideScrollToBottomButton() {
        unreadScrollCount = 0;
        unreadBadge.setVisible(false);
        TelegramCellUtils.animateNotificationBadge(scrollToBottomContainer, false);
    }

    private void handleScrollPositionChange(double newValue) {
        // Handle lazy loading of messages when scrolling to top // TODO: dev this section
        if (newValue < 0.1) {
            loadMoreMessages();
        }
    }

    // ============ TYPING INDICATOR ============

    // TODO: optional
//    public void showTypingIndicator(String userName) {
//        if (typingIndicatorContainer == null) return;
//
//        typingIndicatorLabel.setText(userName + " is typing...");
//        typingIndicatorContainer.setVisible(true);
//        isTypingIndicatorVisible = true;
//
//        if (typingAnimationTimeline != null) {
//            typingAnimationTimeline.play();
//        }
//
//        // Auto-hide after 5 seconds
//        Timeline autoHide = new Timeline(new KeyFrame(Duration.seconds(5), e -> hideTypingIndicator()));
//        autoHide.play();
//    }
//
//    public void hideTypingIndicator() {
//        if (typingIndicatorContainer == null || !isTypingIndicatorVisible) return;
//
//        typingIndicatorContainer.setVisible(false);
//        isTypingIndicatorVisible = false;
//
//        if (typingAnimationTimeline != null) {
//            typingAnimationTimeline.stop();
//            typingIndicatorLabel.setOpacity(1.0);
//        }
//    }

    // ============ THEME MANAGEMENT ============

    private void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        updateThemeClasses();

        // Animate theme transition
        animateThemeChange();

        // Update night mode button icon
        updateNightModeIcon();
    }

    // TODO: add other themes in the future
    private void updateThemeClasses() {
        mainChatContainer.getStyleClass().removeAll("light-theme", "dark-theme");
        mainChatContainer.getStyleClass().add(isDarkTheme ? "dark-theme" : "light-theme");
    }

    private void updateNightModeIcon() {
        try {
            String iconPath = isDarkTheme ? "../images/sun-icon.png" : "../images/moon-icon.png"; // TODO
            Image themeIcon = new Image(Objects.requireNonNull(getClass().getResource(iconPath)).toExternalForm());
            ((ImageView) nightModeButton.getGraphic()).setImage(themeIcon);
        } catch (Exception e) {
            System.err.println("Error updating night mode icon: " + e.getMessage());
        }
    }

    // ============ CONNECTION STATUS ============

    // TODO: connect to the server
    private void updateConnectionStatus(boolean connected) {
        connectionIndicator.getStyleClass().removeAll("status-online", "status-offline");
        connectionIndicator.getStyleClass().add(connected ? "status-online" : "status-offline");

        connectionLabel.setText(connected ? "Connected" : "Connecting...");

        if (connected && connectionStatusTimeline != null) {
            connectionStatusTimeline.play();
        } else if (connectionStatusTimeline != null) {
            connectionStatusTimeline.stop();
        }
    }

    // ============ ANIMATIONS ============

    private void animateChatSelection() {
        // Animate the transition when selecting a new chat
        Timeline fadeOut = new Timeline(new KeyFrame(Duration.millis(150),
                new KeyValue(messagesContainer.opacityProperty(), 0)));

        fadeOut.setOnFinished(e -> {
            Timeline fadeIn = new Timeline(new KeyFrame(Duration.millis(150),
                    new KeyValue(messagesContainer.opacityProperty(), 1)));
            fadeIn.play();
        });

        fadeOut.play();
    }

    private void animateThemeChange() {
        // Smooth theme transition
        Timeline transition = new Timeline(new KeyFrame(Duration.millis(300),
                new KeyValue(mainChatContainer.opacityProperty(), 0.95)));

        transition.setAutoReverse(true);
        transition.setCycleCount(2);
        transition.play();
    }

    // ============ EVENT HANDLERS ============

    private void showSideBar() {
        if (sidebarController != null) {
            sidebarController.toggleSidebar(true); // Show with Animation
        } else {
            System.out.println("SidebarController is not initialized!");
        }
    }

    private void startVoiceCall() {
        if (currentSelectedUser == null) return;

        System.out.println("Starting voice call with: " + currentSelectedUser.getUserName());
        showCallDialog("Voice Call", currentSelectedUser.getUserName());
    }

    private void startVideoCall() {
        if (currentSelectedUser == null) return;

        System.out.println("Starting video call with: " + currentSelectedUser.getUserName());
        showCallDialog("Video Call", currentSelectedUser.getUserName());
    }

    private void showCallDialog(String callType, String userName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(callType);
        alert.setHeaderText("Calling " + userName);
        alert.setContentText("This would initiate a " + callType.toLowerCase() + " with " + userName);

        ButtonType endCallButton = new ButtonType("End Call", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(endCallButton);

        alert.showAndWait();
    }

    private void showMoreOptions() {
        ContextMenu menu = new ContextMenu();

        MenuItem viewProfileItem = new MenuItem("View Profile");
        viewProfileItem.setOnAction(e -> toggleRightPanel());

        MenuItem searchItem = new MenuItem("Search Messages");
        searchItem.setOnAction(e -> showSearchInChat());

        MenuItem muteItem = new MenuItem(currentSelectedUser != null && currentSelectedUser.isMuted() ? "Unmute" : "Mute");
        muteItem.setOnAction(e -> toggleMute());

        MenuItem pinItem = new MenuItem(currentSelectedUser != null && currentSelectedUser.isPinned() ? "Unpin" : "Pin");
        pinItem.setOnAction(e -> togglePin());

        MenuItem clearHistoryItem = new MenuItem("Clear History");
        clearHistoryItem.setOnAction(e -> clearChatHistory());

        MenuItem blockUserItem = new MenuItem("Block User");
        blockUserItem.setOnAction(e -> blockUser());

        menu.getItems().addAll(
                viewProfileItem, searchItem, new SeparatorMenuItem(),
                muteItem, pinItem, new SeparatorMenuItem(),
                clearHistoryItem, blockUserItem
        );

        menu.show(moreOptionsButton, moreOptionsButton.getLayoutX(), moreOptionsButton.getLayoutY() + moreOptionsButton.getHeight());
    }

    private void showAttachmentOptions() {
        ContextMenu menu = new ContextMenu();

        MenuItem photoItem = new MenuItem("Photo or Video");
        photoItem.setOnAction(e -> attachPhoto());

        MenuItem documentItem = new MenuItem("Document");
        documentItem.setOnAction(e -> attachDocument());

        MenuItem pollItem = new MenuItem("Poll");
        pollItem.setOnAction(e -> createPoll());

        MenuItem contactItem = new MenuItem("Contact");
        contactItem.setOnAction(e -> attachContact());

        MenuItem locationItem = new MenuItem("Location");
        locationItem.setOnAction(e -> attachLocation());

        menu.getItems().addAll(photoItem, documentItem, pollItem, contactItem, locationItem);
        menu.show(attachmentButton, attachmentButton.getLayoutX(), attachmentButton.getLayoutY() - 200);
    }

    private void showEmojiPicker() {
        // Create simple emoji picker for demonstration
        ContextMenu emojiMenu = new ContextMenu();

        String[] emojis = {"😀", "😂", "😍", "😊", "👍", "❤️", "😢", "😮", "😡", "🎉"};

        for (String emoji : emojis) {
            MenuItem emojiItem = new MenuItem(emoji);
            emojiItem.setOnAction(e -> insertEmoji(emoji));
            emojiMenu.getItems().add(emojiItem);
        }

        emojiMenu.show(emojiButton, emojiButton.getLayoutX(), emojiButton.getLayoutY() - 150);
    }

    private void insertEmoji(String emoji) {
        String currentText = messageInputField.getText();
        int caretPosition = messageInputField.getCaretPosition();

        String newText = currentText.substring(0, caretPosition) + emoji + currentText.substring(caretPosition);
        messageInputField.setText(newText);
        messageInputField.positionCaret(caretPosition + emoji.length());
    }

    // ============ UTILITY METHODS ============

    // TODO
    private String getStatusIcon(String status) {
        return switch (status.toLowerCase()) {
            case "sent" -> "✓";
            case "delivered" -> "✓✓";
            case "read" -> "✓✓";
            default -> "";
        };
    }

    private String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private void refreshChatList() {
        Platform.runLater(() -> {
            chatListView.refresh();
        });
    }

    private void handleEscapeKey() {
        if (replyPreviewContainer.isVisible()) {
            closeReplyPreview();
        } else if (isRightPanelVisible) {
            hideRightPanel();
        } else if (currentSelectedUser != null) {
            goBackToWelcomeState();
        }
    }

    // ============ PLACEHOLDER METHODS ============

    private void goBackToWelcomeState() {
        currentSelectedUser = null;
        chatListView.getSelectionModel().clearSelection();
        showWelcomeState();
        // hideTypingIndicator(); // TODO
        closeReplyPreview();
    }

    private void closeCurrentChat() {
        goBackToWelcomeState();
    }

    private void showSearchInChat() {
        System.out.println("Showing search in chat");
        // TODO: Implement search in chat functionality
    }

    private void createNewGroup() {
        System.out.println("Creating new group");
        // TODO: Implement new group creation
    }

    private void createNewChannel() {
        System.out.println("Creating new channel");
        // TODO: Implement new channel creation
    }

    private void openContacts() {
        System.out.println("Opening contacts");
        // TODO: Implement contacts window
    }

    private void openSavedMessages() {
        System.out.println("Opening saved messages");
        // TODO: Implement saved messages
    }

    private void openSettings() {
        System.out.println("Opening settings");
        // TODO: Implement settings dialog
    }

    private void toggleNotifications() {
        if (currentSelectedUser == null) return;

        boolean newMuteState = !currentSelectedUser.isMuted();
        currentSelectedUser.setMuted(newMuteState);

        updateNotificationToggle(!newMuteState);
        notificationStatusLabel.setText(newMuteState ? "Disabled" : "Enabled");

        // Update header
        mutedIcon.setVisible(newMuteState);

        // Visual feedback
        String message = (newMuteState ? "Muted" : "Unmuted") + " " + currentSelectedUser.getUserName();
        showTemporaryNotification(message);
    }

    private void updateNotificationToggle(boolean enabled) {
        if (notificationsToggle == null) return;

        notificationsToggle.getStyleClass().removeAll("off");
        if (!enabled) {
            notificationsToggle.getStyleClass().add("off");
        }
    }

    private void toggleMute() {
        toggleNotifications();
    }

    private void togglePin() {
        if (currentSelectedUser == null) return;

        boolean newPinState = !currentSelectedUser.isPinned();
        currentSelectedUser.setPinned(newPinState);

        String message = (newPinState ? "Pinned" : "Unpinned") + " " + currentSelectedUser.getUserName();
        showTemporaryNotification(message);

        refreshChatList();
    }

    private void clearChatHistory() {
        if (currentSelectedUser == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); // TODO
        alert.setTitle("Clear Chat History");
        alert.setHeaderText("Clear history with " + currentSelectedUser.getUserName() + "?");
        alert.setContentText("This will permanently delete all messages in this chat.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                messagesContainer.getChildren().clear();
                showEmptyChatState();

                // Update last message in chat list
                currentSelectedUser.setLastMessage("");
                refreshChatList();

                showTemporaryNotification("Chat history cleared");
            }
        });
    }

    private void blockUser() {
        if (currentSelectedUser == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); // TODO
        alert.setTitle("Block User");
        alert.setHeaderText("Block " + currentSelectedUser.getUserName() + "?");
        alert.setContentText("You will no longer receive messages from this user.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String userName = currentSelectedUser.getUserName();
                allChatUsers.remove(currentSelectedUser);
                filteredChatUsers.remove(currentSelectedUser);

                goBackToWelcomeState();
                showTemporaryNotification("Blocked " + userName);
            }
        });
    }

    private void startVoiceRecording() {
        System.out.println("Starting voice recording");
        // TODO: Implement voice recording functionality

        // Visual feedback
        sendButton.getStyleClass().add("recording");
        showTemporaryNotification("Voice recording started");
    }

    private void editLastMessage() {
        // Find last outgoing message and allow editing
        for (int i = messagesContainer.getChildren().size() - 1; i >= 0; i--) {
            HBox messageContainer = (HBox) messagesContainer.getChildren().get(i);
            if (messageContainer.getAlignment() == Pos.CENTER_RIGHT) {
                // This is an outgoing message
                VBox bubble = (VBox) messageContainer.getChildren().getFirst();
                Label messageText = (Label) bubble.getChildren().getFirst();

                // Put text in input field for editing
                messageInputField.setText(messageText.getText());
                messageInputField.selectAll();
                messageInputField.requestFocus();

                // Remove the message from display
                messagesContainer.getChildren().remove(i);
                break;
            }
        }
    }

    private void loadMoreMessages() {
        // TODO: Implement lazy loading of older messages
        System.out.println("Loading more messages...");
    }

    private void openMediaItem(String type, int index) {
        System.out.println("Opening " + type + " item " + index);
        // TODO: Implement media viewer
    }

    private void showAllMedia() {
        System.out.println("Showing all media");
        // TODO: Implement full media browser
    }

    private void showMessageContextMenu(MouseEvent event) {
        ContextMenu menu = new ContextMenu();

        MenuItem replyItem = new MenuItem("Reply");
        replyItem.setOnAction(e -> showReplyPreview((VBox) event.getSource()));

        MenuItem forwardItem = new MenuItem("Forward");
        forwardItem.setOnAction(e -> forwardMessage());

        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(e -> editMessage());

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> deleteMessage());

        MenuItem copyItem = new MenuItem("Copy Text");
        copyItem.setOnAction(e -> copyMessageText());

        menu.getItems().addAll(replyItem, forwardItem, editItem, new SeparatorMenuItem(), copyItem, deleteItem);
        menu.show(mainChatContainer, event.getScreenX(), event.getScreenY());
    }

    // ============ ATTACHMENT METHODS ============

    private void attachPhoto() {
        System.out.println("Attaching photo/video");
        // TODO: Implement file chooser for media
    }

    private void attachDocument() {
        System.out.println("Attaching document");
        // TODO: Implement file chooser for documents
    }

    private void createPoll() {
        System.out.println("Creating poll");
        // TODO: Implement poll creation dialog
    }

    private void attachContact() {
        System.out.println("Attaching contact");
        // TODO: Implement contact picker
    }

    private void attachLocation() {
        System.out.println("Attaching location");
        // TODO: Implement location picker
    }

    // ============ MESSAGE ACTIONS ============

    private void forwardMessage() {
        System.out.println("Forwarding message");
        // TODO: Implement message forwarding
    }

    private void editMessage() {
        System.out.println("Editing message");
        // TODO: Implement message editing
    }

    private void deleteMessage() {
        System.out.println("Deleting message");
        // TODO: Implement message deletion
    }

    private void copyMessageText() {
        System.out.println("Copying message text");
        // TODO: Implement text copying to clipboard
    }

    // ============ UTILITY METHODS ============

    // TODO(Server)
    private void loadDefaultHeaderAvatar() {
        try {
            Image defaultAvatar = new Image(Objects.requireNonNull(getClass().getResource("/Client/images/11Devs-white.png")).toExternalForm());
            headerAvatarImage.setImage(defaultAvatar);
        } catch (Exception e) {
            System.err.println("Error loading default header avatar: " + e.getMessage());
        }
    }

    // TODO(Server)
    private void loadDefaultProfileAvatar() {
        try {
            Image defaultAvatar = new Image(Objects.requireNonNull(getClass().getResource("/Client/images/11Devs-white.png")).toExternalForm());
            profileAvatarImage.setImage(defaultAvatar);
        } catch (Exception e) {
            System.err.println("Error loading default profile avatar: " + e.getMessage());
        }
    }

    // TODO: dev more
    private void showTemporaryNotification(String message) {
        // Create temporary notification overlay
        Label notification = new Label(message);
        notification.getStyleClass().add("temporary-notification");

        // Position at top of screen
        StackPane.setAlignment(notification, Pos.TOP_CENTER);
        StackPane.setMargin(notification, new Insets(20, 0, 0, 0));

        // Add to main container
        if (mainChatContainer.getChildren().isEmpty()) {
            StackPane overlay = new StackPane();
            overlay.getChildren().add(notification);
            overlay.setMouseTransparent(true);
            mainChatContainer.getChildren().add(overlay);
        }

        // Auto-hide after 3 seconds
        Timeline autoHide = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            TelegramCellUtils.animateNotificationBadge(notification, false);
        }));
        autoHide.play();
    }

    // TODO (Server)
    private void sendTypingStatus(boolean isTyping) {
        // In a real implementation, this would send typing status to server
        System.out.println("Typing status: " + isTyping);
    }

    // ============ PUBLIC API METHODS ============

    // TODO
    public void addNewMessage(String text, boolean isOutgoing, String senderName) {
        Platform.runLater(() -> {
            addMessageBubble(text, isOutgoing, getCurrentTime(), isOutgoing ? "sent" : "received", senderName);

            if (!isOutgoing) {
                unreadScrollCount++;
                updateScrollToBottomVisibility();
            }

            if (messagesScrollPane.getVvalue() > 0.9) {
                scrollToBottom();
            }
        });
    }

    // TODO
    public void updateUserOnlineStatus(String userName, boolean isOnline) {
        Platform.runLater(() -> {
            for (UserViewModel user : allChatUsers) {
                if (user.getUserName().equals(userName)) {
                    user.setOnline(isOnline);
                    user.setLastSeen(isOnline ? "online" : "last seen just now");

                    if (user == currentSelectedUser) {
                        updateChatHeader(user);
                        if (isRightPanelVisible) {
                            updateRightPanel(user);
                        }
                    }

                    refreshChatList();
                    break;
                }
            }
        });
    }

    // TODO
    public void addUser(UserViewModel user) {
        Platform.runLater(() -> {
            allChatUsers.add(user);
        });
    }

    // TODO
    public void removeUser(UserViewModel user) {
        Platform.runLater(() -> {
            allChatUsers.remove(user);
            filteredChatUsers.remove(user);

            if (currentSelectedUser == user) {
                goBackToWelcomeState();
            }
        });
    }

    public void selectUserByName(String userName) {
        for (UserViewModel user : allChatUsers) {
            if (user.getUserName().equals(userName)) {
                Platform.runLater(() -> {
                    chatListView.getSelectionModel().select(user);
                });
                break;
            }
        }
    }

    public UserViewModel getCurrentSelectedUser() {
        return currentSelectedUser;
    }

    // TODO
    public void setTheme(boolean darkTheme) {
        this.isDarkTheme = darkTheme;
        updateThemeClasses();
        updateNightModeIcon();
    }

    // ============ CLEANUP ============

    public void cleanup() {
        // Stop all animations
        if (typingAnimationTimeline != null) {
            typingAnimationTimeline.stop();
        }
        if (onlineStatusTimeline != null) {
            onlineStatusTimeline.stop();
        }
        if (connectionStatusTimeline != null) {
            connectionStatusTimeline.stop();
        }

        // Clear data
        allChatUsers.clear();
        filteredChatUsers.clear();
        currentMessages.clear();
        messagesContainer.getChildren().clear();
    }
}