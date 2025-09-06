package Client.Controllers;

import Shared.Models.*;
//import Shared.Utils.SidebarUtil;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.jfoenix.controls.JFXToggleButton;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.shape.SVGPath;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.ResourceBundle;

import static JSocket2.Utils.FileUtil.getFileExtension;
import static Shared.Utils.DialogUtil.showNotificationDialog;
import static Shared.Utils.FileUtil.*;

/**
 * The MainChatController class manages the main chat interface of the application.
 * It handles UI initialization, chat selection, message handling, and user interactions.
 * This controller is tied to the main chat FXML layout.
 */
public class MainChatController implements Initializable {

    // ============ FXML INJECTED COMPONENTS ============

    /**
     * The main container for the chat interface, a BorderPane.
     */
    @FXML private BorderPane mainChatContainer;

    // Sidebar elements
    /**
     * The left sidebar containing the chat list and controls.
     */
    @FXML private VBox leftSidebar;
    /**
     * The SplitPane that holds the left, center, and right panels.
     */
    private SplitPane splitPane;
    /**
     * Button to toggle the sidebar menu.
     */
    @FXML private Button menuButton;
    /**
     * Container for the search field in the sidebar.
     */
    @FXML private HBox searchContainer;
    /**
     * Text field for searching chats.
     */
    @FXML private TextField searchField;
    /**
     * ListView displaying the chat list with UserViewModel items.
     */
    @FXML private ListView<UserViewModel> chatListView;
    /**
     * Button to open settings.
     */
    @FXML private Button settingsButton;
    /**
     * Circle indicator for connection status.
     */
    @FXML private Circle connectionIndicator;
    /**
     * Label showing the connection status text.
     */
    @FXML private Label connectionLabel;

    // Chat header elements
    /**
     * ImageView for the user's avatar in the chat header.
     */
    @FXML private ImageView headerAvatarImage;
    /**
     * Circle indicating the online status of the user in the chat header.
     */
    @FXML private Circle onlineIndicator;
    /**
     * Label displaying the chat title (user or group name).
     */
    @FXML private Label chatTitleLabel;
    /**
     * Label displaying the chat subtitle (e.g., online status or last seen).
     */
    @FXML private Label chatSubtitleLabel;
    /**
     * Label showing the number of members in a group chat.
     */
    @FXML private Label membersCountLabel;
    /**
     * ImageView for the muted icon in the chat header.
     */
    @FXML private ImageView mutedIcon;
    /**
     * Button to search within the current chat.
     */
    @FXML private Button searchInChatButton;
    /**
     * Button to initiate a voice call.
     */
    @FXML private Button callButton;
    /**
     * Button to initiate a video call.
     */
    @FXML private Button videoCallButton;
    /**
     * Button to show more options for the chat.
     */
    @FXML private Button moreOptionsButton;

    // Messages area
    /**
     * ScrollPane containing the messages list.
     */
    @FXML private ScrollPane messagesScrollPane;
    /**
     * VBox containing all message bubbles.
     */
    @FXML private VBox messagesContainer;
    /**
     * VBox for the welcome state when no chat is selected.
     */
    @FXML private VBox welcomeStateContainer;
    /**
     * VBox for the empty chat state when no messages are present.
     */
    @FXML private VBox emptyChatStateContainer;
    /**
     * StackPane containing the scroll-to-bottom button.
     */
    @FXML private StackPane scrollToBottomContainer;
    /**
     * Button to scroll to the bottom of the messages list.
     */
    @FXML private Button scrollToBottomButton;

    // Reply preview
    /**
     * HBox containing the reply preview area.
     */
    @FXML private HBox replyPreviewContainer;
    /**
     * Label showing the user or entity being replied to.
     */
    @FXML private Label replyToLabel;
    /**
     * Label showing the text of the message being replied to.
     */
    @FXML private Label replyMessageLabel;
    /**
     * Button to close the reply preview.
     */
    @FXML private Button closeReplyButton;

    // Message input area
    /**
     * Button to open attachment options.
     */
    @FXML private Button attachmentButton;
    /**
     * TextArea for composing messages.
     */
    @FXML private TextArea messageInputField;
    /**
     * Button to send messages or start voice recording.
     */
    @FXML private Button sendButton;
    /**
     * ImageView for the send button icon (e.g., send or microphone).
     */
    @FXML private ImageView sendButtonIcon;

    // Right panel elements
    /**
     * VBox containing the right panel for user profile and media.
     */
    @FXML private VBox rightPanel;
    /**
     * ImageView for the user's avatar in the right panel.
     */
    @FXML private ImageView profileAvatarImage;
    /**
     * Label showing the user's name in the right panel.
     */
    @FXML private Label profileNameLabel;
    /**
     * Label showing the user's username in the right panel.
     */
    @FXML private Label profileUsernameLabel;
    /**
     * Label showing the user's status (e.g., online or last seen).
     */
    @FXML private Label profileStatusLabel;
    /**
     * Label showing the user's phone number in the right panel.
     */
    @FXML private Label profilePhoneLabel;
    /**
     * Button to initiate a voice call from the right panel.
     */
    @FXML private Button profileCallButton;
    /**
     * Button to initiate a video call from the right panel.
     */
    @FXML private Button profileVideoButton;
    /**
     * Button to search within the chat from the right panel.
     */
    @FXML private Button profileSearchButton;
    /**
     * Label showing the notification status in the right panel.
     */
    @FXML private Label notificationStatusLabel;
    /**
     * Button to toggle notifications in the right panel.
     */
    @FXML private JFXToggleButton notificationsToggle;
    /**
     * Handle about HBox click
     */
    @FXML private HBox MoreHBox;
    /**
     * Handle right panel
     */
    @FXML private Button closeRightPanelButton;

    // ============ DATA AND STATE ============

    /**
     * ObservableList of all chat users.
     */
    private ObservableList<UserViewModel> allChatUsers;
    /**
     * ObservableList of filtered chat users based on search.
     */
    private ObservableList<UserViewModel> filteredChatUsers;
    /**
     * The currently selected user or chat.
     */
    private UserViewModel currentSelectedUser;
    /**
     * ObservableList of current messages in the chat.
     */
    private ObservableList<MessageViewModel> currentMessages;
    /**
     * The message being replied to, if any.
     */
    private MessageViewModel replyToMessage;
    /**
     * activeMessageContextMenu.
     */
    private ContextMenu activeMessageContextMenu;
    /**
     * isEditing boolean.
     */
    private boolean isEditing = false;
    /**
     * editingMessageBubble VBox.
     */
    private VBox editingMessageBubble = null;

    // Animation timelines
    /**
     * Timeline for typing animation.
     */
    private Timeline typingAnimationTimeline;
    /**
     * Timeline for online status animation.
     */
    private Timeline onlineStatusTimeline;
    /**
     * Timeline for connection status animation.
     */
    private Timeline connectionStatusTimeline;
    /**
     * ParallelTransition for reply animation.
     */
    private ParallelTransition replyPreviewAnimation;

    // State flags
    /**
     * Flag indicating if dark theme is active.
     */
    private boolean isDarkTheme = true;
    /**
     * Flag indicating if the right panel is visible.
     */
    private boolean isRightPanelVisible = false;
    /**
     * Flag indicating if the typing indicator is visible.
     */
    private boolean isTypingIndicatorVisible = false;
    /**
     * Current media filter type (e.g., "media", "files").
     */
    private String currentMediaFilter = "media";
    /**
     * Count of unread messages when scrolled up.
     */
    private int unreadScrollCount = 0;

    // Sidebars settings
    /**
     * Initial X position of the left sidebar.
     */
    private double leftInitialX;
    /**
     * Initial X position of the right sidebar.
     */
    private double rightInitialX;

    // Sidebar Menu
    /**
     * Controller for the sidebar menu.
     */
    private SidebarMenuController sidebarController;

    /**
     * Initializes the controller after the FXML file is loaded.
     * Sets up the UI components, data, event handlers, and initial state.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeSidebarsSplitPane();
        initializeData();
        setupChatList();
        setupMessageInput();
        setupEventHandlers();
        setupAnimations();
        // TODO: Implement keyboard shortcut setup for enhanced navigation.
        loadInitialState();
    }

    /**
     * Initializes the sidebar layout by creating a SplitPane and setting it as the center of the BorderPane.
     * The right panel is initially removed to be shown on demand.
     */
    private void initializeSidebarsSplitPane() {
        splitPane = new SplitPane();
        VBox chatArea = (VBox) mainChatContainer.getCenter();
        splitPane.getItems().addAll(leftSidebar, chatArea);
        mainChatContainer.setCenter(splitPane);

        // Setting minimum and maximum widths for panels
        SplitPane.setResizableWithParent(leftSidebar, Boolean.FALSE); // Prevent resizing of the parent
        SplitPane.setResizableWithParent(rightPanel, Boolean.FALSE); // Prevent resizing of the parent
        leftSidebar.setMinWidth(300.0);
        leftSidebar.setMaxWidth(420.0);
        rightPanel.setMinWidth(300.0);
        rightPanel.setMaxWidth(420.0);

        // We will set divider positions dynamically when showing/hiding panels
    }

    /**
     * Initializes the data structures for chats, messages, and users.
     * Currently loads sample data.
     */
    private void initializeData() {
        allChatUsers = FXCollections.observableArrayList();
        filteredChatUsers = FXCollections.observableArrayList();
        currentMessages = FXCollections.observableArrayList();

        // Load sample data for demonstration
        loadSampleChats();
        // TODO: Implement fetching data from the server or database.
    }

    /**
     * Loads sample chat data for demonstration purposes.
     * This method will be replaced with real data fetching.
     */
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
                .type(UserType.USER.name()).bio("Hi im Alice")
                .phoneNumber("+981111111111")
                .userId("Alice")
                .build();

        UserViewModel user2 = new UserViewModelBuilder()
                .userName("Bob Smith")
                .lastMessage("Let's meet tomorrow")
                .time("13:00")
                .isOnline(false)
                .lastSeen("last seen 2 hours ago")
                .type(UserType.USER.name()).bio("Hi im Bob")
                .phoneNumber("+982222222222")
                .userId("Bob")
                .build();

        UserViewModel group1 = new UserViewModelBuilder()
                .userName("Development Team")
                .lastMessage("John: Great work on the project!")
                .time("12:20")
                .notificationsNumber("12")
                .isPinned(true)
                .type(UserType.GROUP.name())
                .bio("Hi we are Development Team")
                .userId("Development Team")
                .build();

        UserViewModel channel1 = new UserViewModelBuilder()
                .userName("Tech News")
                .lastMessage("Latest updates in technology")
                .time("11:15")
                .isMuted(true)
                .type(UserType.CHANNEL.name())
                .bio("Hi we are Tech News")
                .userId("Tech News")
                .build();

        allChatUsers.addAll(user1, user2, group1, channel1);
        filteredChatUsers.setAll(allChatUsers);
    }

    /**
     * Sets up the chat list with a custom cell factory and selection listener.
     */
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

    /**
     * Sets up the message input area with auto-resize, key handling, and focus listeners.
     */
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

    /**
     * Sets up event handlers for all interactive UI components.
     */
    private void setupEventHandlers() {
        // Sidebar buttons
        menuButton.setOnAction(e -> showSideBar());
        settingsButton.setOnAction(e -> openSettings());
        // nightModeButton.setOnAction(e -> toggleTheme()); TODO UI

        // Search functionality
        searchField.textProperty().addListener((obs, oldText, newText) -> performSearch(newText));

        // Header buttons
        searchInChatButton.setOnAction(e -> showSearchInChat());
        callButton.setOnAction(e -> startVoiceCall());
        videoCallButton.setOnAction(e -> startVideoCall());
        moreOptionsButton.setOnAction(e -> showMoreOptions());

        // Message input buttons
        attachmentButton.setOnAction(e -> attachDocument());
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
        MoreHBox.setOnMouseClicked(e -> handleAbout());

        // Scroll listener for messages
        messagesScrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            updateScrollToBottomVisibility();
            handleScrollPositionChange(newVal.doubleValue());
        });
    }

    /**
     * Sets up animations for connection status and online indicators.
     * TODO: Connect to the server to fetch real-time status updates.
     */
    private void setupAnimations() {
        // Connection status pulse
        connectionStatusTimeline = TelegramCellUtils.createOnlineStatusPulse(connectionIndicator);
        connectionStatusTimeline.play();

        // Online status animation
        onlineStatusTimeline = TelegramCellUtils.createOnlineStatusPulse(onlineIndicator);
    }

    /**
     * Sets up keyboard shortcuts for navigation and actions.
     * TODO: Develop this section in the future for enhanced usability.
     */
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

    /**
     * Loads the initial state of the UI, including theme and connection status.
     */
    private void loadInitialState() {
        // Set initial theme
        // updateThemeClasses(); TODO UI

        // hide reply preview
        replyPreviewContainer.setVisible(false);
        replyPreviewContainer.setManaged(false);

        // Update connection status
        updateConnectionStatus(true); // TODO: Connect to the server for real status.

        // Show welcome state
        showWelcomeState();

        rightPanel.setVisible(false);
        rightPanel.setManaged(false);

        // Set the initial position of the first divider after the scene is shown
        Platform.runLater(() -> splitPane.setDividerPosition(0, 0.25));
    }

    /**
     * Sets the SidebarMenuController for sidebar management.
     *
     * @param controller The SidebarMenuController instance.
     */
    public void setSidebarController(SidebarMenuController controller) {
        this.sidebarController = controller;
    }

    // ============ CHAT MANAGEMENT ============

    /**
     * Selects a chat based on the provided UserViewModel and updates the UI.
     *
     * @param user The UserViewModel representing the selected chat.
     */
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

    /**
     * Updates the chat header with the selected user's information.
     *
     * @param user The UserViewModel to update the header with.
     */
    private void updateChatHeader(UserViewModel user) {
        // Update chat title
        chatTitleLabel.setText(user.getUserName());

        // Update subtitle based on user state
        updateChatSubtitle(user);

        // Update avatar
        updateHeaderAvatar(user);

        // Update badges and indicators
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

    /**
     * Updates the chat subtitle based on the user's online status or typing state.
     *
     * @param user The UserViewModel to update the subtitle for.
     */
    private void updateChatSubtitle(UserViewModel user) {
        if (user.isTyping()) {
            chatSubtitleLabel.setText("typing...");
            chatSubtitleLabel.getStyleClass().setAll("chat-subtitle", "typing-indicator");
            // TODO: Optional - Implement showing typing indicator with user name.
            // showTypingIndicator(user.getUserName());
        } else if (user.isOnline() && user.getType() == UserType.USER) {
            chatSubtitleLabel.setText("online");
            chatSubtitleLabel.getStyleClass().setAll("chat-subtitle");
            // TODO: Optional - Implement hiding typing indicator.
            // hideTypingIndicator();
        } else if (user.getLastSeen() != null && !user.getLastSeen().isEmpty()) {
            chatSubtitleLabel.setText(user.getLastSeen());
            chatSubtitleLabel.getStyleClass().setAll("chat-subtitle");
            // TODO: Optional - Implement hiding typing indicator.
            // hideTypingIndicator();
        } else {
            chatSubtitleLabel.setText("offline");
            chatSubtitleLabel.getStyleClass().setAll("chat-subtitle");
            // TODO: Optional - Implement hiding typing indicator.
            // hideTypingIndicator();
        }
    }

    /**
     * Updates the header avatar with the user's image or a default one.
     *
     * @param user The UserViewModel to update the avatar for.
     */
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

    /**
     * Loads messages for the selected user based on their type.
     *
     * @param user The UserViewModel to load messages for.
     */
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

    /**
     * Loads sample user messages for demonstration.
     * TODO: Replace with real data from the server.
     * TODO: This section will be deleted (This is an example).
     *
     * @param user The UserViewModel for the user chat.
     */
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

    /**
     * Loads sample group messages for demonstration.
     * TODO: Replace with real data from the server.
     * TODO: This section will be deleted (This is an example).
     *
     * @param group The UserViewModel for the group chat.
     */
    private void loadGroupMessages(UserViewModel group) {
        // Sample group chat messages
        addMessageBubble("Hey everyone! Meeting at 3 PM today", false, "09:15", "read", "John");
        addMessageBubble("Sounds good! I'll be there", true, "09:16", "read", null);
        addMessageBubble("Can we move it to 3:30? I'm running a bit late", false, "09:18", "read", "Sarah");
        addMessageBubble("Sure, no problem! 3:30 it is", false, "09:20", "read", "Mike");
        addMessageBubble("Perfect! See you all then 👍", true, "09:21", "delivered", null);
    }

    /**
     * Loads sample channel messages for demonstration.
     * TODO: Replace with real data from the server.
     * TODO: This section will be deleted (This is an example).
     *
     * @param channel The UserViewModel for the channel.
     */
    private void loadChannelMessages(UserViewModel channel) {
        // Sample channel messages
        addMessageBubble("📢 New update available! Check out the latest features in version 2.1", false, "08:00", "read", channel.getUserName());
        addMessageBubble("🔥 Hot topic: AI advances in 2025", false, "07:30", "read", channel.getUserName());
        addMessageBubble("💡 Tip of the day: Use keyboard shortcuts to boost productivity", false, "06:45", "read", channel.getUserName());
    }

    // ============ MESSAGE HANDLING ============

    /**
     * Adds a message bubble to the messages container with the specified details.
     *
     * @param text       The message text.
     * @param isOutgoing True if the message is outgoing, false if incoming.
     * @param time       The time of the message.
     * @param status     The delivery status (e.g., "sent", "delivered", "read").
     * @param senderName The name of the sender (null for outgoing).
     */
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

    /**
     * Creates a message bubble VBox with text, time, and status.
     *
     * @param text       The message text.
     * @param time       The time of the message.
     * @param status     The delivery status.
     * @param isOutgoing True if the message is outgoing.
     * @param senderName The name of the sender (null for outgoing).
     * @return The constructed VBox for the message bubble.
     */
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

    /**
     * Creates an ImageView for a sender's avatar with a circular clip.
     *
     * @param senderName The name of the sender (used for avatar lookup).
     * @return The ImageView for the sender's avatar.
     */
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
            // TODO: In a real app, load the actual sender's avatar from the server.
            Image defaultAvatar = new Image(Objects.requireNonNull(getClass().getResource("/Client/images/11Devs-black.png")).toExternalForm());
            avatar.setImage(defaultAvatar);
        } catch (Exception e) {
            System.err.println("Failed to load sender avatar for: " + senderName);
        }

        avatar.getStyleClass().add("sender-avatar");
        return avatar;
    }

    /**
     * Handles mouse clicks on a message bubble.
     * Differentiates between primary (left) and secondary (right) clicks.
     * - Double-click with the primary button triggers a reply.
     * - A single click with the secondary button shows the context menu.
     *
     * @param event The MouseEvent triggering the action.
     */
    private void handleMessageClick(MouseEvent event) {
        // (Secondary Button)
        if (event.getButton() == MouseButton.SECONDARY) {
            showMessageContextMenu(event);
            event.consume();
            return;
        }

        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            VBox bubble = (VBox) event.getSource();
            showReplyPreview(bubble);
            event.consume();
        }
    }

    /**
     * Displays a reply preview for the selected message bubble.
     * Stops any previous animation before starting the new one.
     *
     * @param messageBubble The VBox representing the message to reply to.
     */
    private void showReplyPreview(VBox messageBubble) {
        if (replyPreviewContainer == null) return;

        resetReplyEditState();

        // Stop any ongoing animation on the container
        if (replyPreviewAnimation != null) {
            replyPreviewAnimation.stop();
        }

        // Reset states
        isEditing = false;
        editingMessageBubble = null;
        replyToLabel.setText("");
        replyMessageLabel.setText("");

        // Extract message info
        Label messageText = (Label) messageBubble.getChildren().get(
                messageBubble.getChildren().size() == 3 ? 1 : 0 // Account for sender name
        );

        replyToLabel.setText(currentSelectedUser != null ? currentSelectedUser.getUserName() : "User");
        replyMessageLabel.setText(messageText.getText().length() > 100 ?
                messageText.getText().substring(0, 97) + "..." : messageText.getText());

        // Make it visible and managed before animating
        replyPreviewContainer.setVisible(true);
        replyPreviewContainer.setManaged(true);

        // Animate reply preview
        replyPreviewContainer.setTranslateY(-30);
        replyPreviewContainer.setOpacity(0);

        TranslateTransition slideDown = new TranslateTransition(Duration.millis(200), replyPreviewContainer);
        slideDown.setToY(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), replyPreviewContainer);
        fadeIn.setToValue(1.0);

        replyPreviewAnimation = new ParallelTransition(slideDown, fadeIn);
        replyPreviewAnimation.play();

        // Focus message input
        messageInputField.requestFocus();
    }

    /**
     * Closes the reply preview with an animation.
     * Stops any previous animation before starting the new one.
     */
    @FXML
    private void closeReplyPreview() {
        if (replyPreviewContainer == null || !replyPreviewContainer.isVisible()) return;

        if (replyPreviewAnimation != null) {
            replyPreviewAnimation.stop();
        }

        TranslateTransition slideUp = new TranslateTransition(Duration.millis(150), replyPreviewContainer);
        slideUp.setToY(-30);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), replyPreviewContainer);
        fadeOut.setToValue(0);

        replyPreviewAnimation = new ParallelTransition(slideUp, fadeOut);
        replyPreviewAnimation.setOnFinished(e -> {
            resetReplyEditState();
        });
        replyPreviewAnimation.play();
    }

    /**
     * Resets the reply/edit state completely.
     * Clears the preview panel and resets state flags.
     */
    private void resetReplyEditState() {
        isEditing = false;
        editingMessageBubble = null;
        replyToMessage = null;

        replyPreviewContainer.setVisible(false);
        replyPreviewContainer.setManaged(false);

        replyToLabel.setText("");
        replyMessageLabel.setText("");
        messageInputField.clear();
    }

    // ============ DOCUMENT MESSAGE BUBBLE IMPLEMENTATION ============

    /**
     * Enhanced methods to add to your MainChatController class for document handling
     */
            // ============ DOCUMENT ATTACHMENT METHODS ============

    /**
     * Enhanced attachDocument method with file chooser integration
     */
    private void attachDocument() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Document");

        // Add common document filters
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("PDF Documents", "*.pdf"),
                new FileChooser.ExtensionFilter("Word Documents", "*.doc", "*.docx"),
                new FileChooser.ExtensionFilter("Excel Files", "*.xls", "*.xlsx"),
                new FileChooser.ExtensionFilter("PowerPoint Files", "*.ppt", "*.pptx"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("Archives", "*.zip", "*.rar", "*.7z"),
                new FileChooser.ExtensionFilter("Code", "java", "py", "js", "cpp", "c", "html", "css", "fxml")
        );

        Stage currentStage = (Stage) attachmentButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(currentStage);

        if (selectedFile != null) {
            // Validate file size (e.g., max 100MB)
            long maxFileSize = 100 * 1024 * 1024; // 100MB in bytes
            if (selectedFile.length() > maxFileSize) {
                showTemporaryNotification("File Too Large\nThe selected file is too large. Maximum file size is 100MB.\n");
                return;
            }

            // Process the file attachment
            processDocumentAttachment(selectedFile);
        }
    }

    /**
     * Processes the selected document and adds it to the chat
     */
    private void processDocumentAttachment(File file) {
        try {
            // Create document info object
            DocumentInfo docInfo = new DocumentInfo(
                    file.getName(),
                    file.length(),
                    getFileExtension(file),
                    file.getAbsolutePath()
            );

            //TODO: connect to the data base
            // Copy file to app's document directory for persistence
            String documentsDir = "/Client/documents"; // TODO: it has bug
            ensureDataDirectoryExists(documentsDir);

            String newFileName = System.currentTimeMillis() + "_" + file.getName();
            Path targetPath = Path.of(documentsDir + newFileName);

            // Copy file
            Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            docInfo.setStoredPath(targetPath.toString());

            // Add document message bubble
            addDocumentMessageBubble(docInfo, true, getCurrentTime(), "sent");

            // Update chat list
            if (currentSelectedUser != null) {
                currentSelectedUser.setLastMessage("📄 " + file.getName());
                currentSelectedUser.setTime(getCurrentTime());
                refreshChatList();
            }

            // Simulate upload progress and delivery
            simulateDocumentUpload(docInfo);

        } catch (Exception e) {
            System.err.println("Error processing document: " + e.getMessage());
            showTemporaryNotification("Upload Error\nFailed to process the selected document.\n");
        }
    }

    /**
     * Creates a document message bubble with file info and controls
     */
    private void addDocumentMessageBubble(DocumentInfo docInfo, boolean isOutgoing,
                                          String time, String status) {
        HBox messageContainer = new HBox();
        messageContainer.setSpacing(12);
        messageContainer.setPadding(new Insets(4, 0, 4, 0));

        if (isOutgoing) {
            messageContainer.setAlignment(Pos.CENTER_RIGHT);
            VBox bubble = createDocumentBubble(docInfo, time, status, true);
            messageContainer.getChildren().add(bubble);
        } else {
            messageContainer.setAlignment(Pos.CENTER_LEFT);

            // Add sender avatar for group chats
            if (currentSelectedUser != null &&
                    (currentSelectedUser.getType() == UserType.GROUP ||
                            currentSelectedUser.getType() == UserType.SUPERGROUP)) {
                ImageView senderAvatar = createSenderAvatar(docInfo.getSenderName());
                messageContainer.getChildren().add(senderAvatar);
            }

            VBox bubble = createDocumentBubble(docInfo, time, status, false);
            messageContainer.getChildren().add(bubble);
        }

        messagesContainer.getChildren().add(messageContainer);
        TelegramCellUtils.animateNewMessage(messageContainer);

        Platform.runLater(this::scrollToBottom);
    }

    /**
     * Creates the document bubble UI with file icon, info, and open button
     */
    private VBox createDocumentBubble(DocumentInfo docInfo, String time, String status, boolean isOutgoing) {
        VBox bubble = new VBox();
        bubble.setSpacing(8);
        bubble.getStyleClass().addAll("message-bubble", "document-bubble", isOutgoing ? "outgoing" : "incoming");
        bubble.setMaxWidth(350);
        bubble.setPadding(new Insets(12));

        // Document content container
        HBox docContainer = new HBox();
        docContainer.setSpacing(12);
        docContainer.setAlignment(Pos.CENTER_LEFT);

        // File icon
        VBox iconContainer = new VBox();
        iconContainer.setAlignment(Pos.CENTER);
        iconContainer.setPrefSize(48, 48);
        iconContainer.getStyleClass().add("document-icon-container");

        // Create icon based on file type
        ImageView fileIcon = createFileTypeIcon(docInfo.getFileExtension());
        fileIcon.setFitWidth(70);
        fileIcon.setFitHeight(70);
        fileIcon.setPreserveRatio(true);

        iconContainer.getChildren().add(fileIcon);

        // File info
        VBox fileInfo = new VBox();
        fileInfo.setSpacing(4);
        fileInfo.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(fileInfo, Priority.ALWAYS);

        // File name
        Label fileName = new Label(docInfo.getFileName());
        fileName.getStyleClass().addAll("document-name", isOutgoing ? "outgoing" : "incoming");
        fileName.setWrapText(true);
        fileName.setMaxWidth(200);

        // File size and type
        Label fileDetails = new Label(formatFileSize(docInfo.getFileSize()) + " • " +
                docInfo.getFileExtension().toUpperCase());
        fileDetails.getStyleClass().addAll("document-details", isOutgoing ? "outgoing" : "incoming");

        fileInfo.getChildren().addAll(fileName, fileDetails);

        // Action buttons container
        VBox actionsContainer = new VBox();
        actionsContainer.setSpacing(4);
        actionsContainer.setAlignment(Pos.CENTER);

        // Open button
        Button openButton = new Button("Open");
        openButton.getStyleClass().addAll("document-action-button", isOutgoing ? "outgoing" : "incoming");
        openButton.setPrefWidth(100);
        openButton.setOnAction(e -> openDocument(docInfo));

        // Download/Save button (for received files)
        Button saveButton = null;
        if (!isOutgoing) {
            saveButton = new Button("Save");
            saveButton.getStyleClass().addAll("document-action-button", "secondary");
            saveButton.setPrefWidth(60);
            saveButton.setOnAction(e -> saveDocument(docInfo));
        }

        actionsContainer.getChildren().add(openButton);
        if (saveButton != null) {
            actionsContainer.getChildren().add(saveButton);
        }

        docContainer.getChildren().addAll(iconContainer, fileInfo, actionsContainer);

        // Time and status container
        HBox timeContainer = new HBox();
        timeContainer.setSpacing(4);
        timeContainer.setAlignment(Pos.CENTER_RIGHT);
        timeContainer.setPadding(new Insets(4, 0, 0, 0));

        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().addAll("message-time", isOutgoing ? "outgoing" : "incoming");
        timeContainer.getChildren().add(timeLabel);

        // Add status for outgoing messages
        if (isOutgoing && status != null) {
            Label statusLabel = new Label(getStatusIcon(status));
            statusLabel.getStyleClass().addAll("message-status", status);
            timeContainer.getChildren().add(statusLabel);
        }

        bubble.getChildren().addAll(docContainer, timeContainer);

        // Add click handler for message options
        bubble.setOnMouseClicked(this::handleMessageClick);

        return bubble;
    }

    /**
     * Creates appropriate file type icon based on extension
     */
    private ImageView createFileTypeIcon(String extension) {
        ImageView icon = new ImageView();
        String iconPath;

        try {
            switch (extension.toLowerCase()) {
                case "pdf" -> iconPath = "/Client/images/file-icons/pdf-icon.png";
                case "doc", "docx" -> iconPath = "/Client/images/file-icons/word-icon.png";
                case "xls", "xlsx" -> iconPath = "/Client/images/file-icons/excel-icon.png";
                case "ppt", "pptx" -> iconPath = "/Client/images/file-icons/powerpoint-icon.png";
                case "txt" -> iconPath = "/Client/images/file-icons/text-icon.png";
                case "zip", "rar", "7z" -> iconPath = "/Client/images/file-icons/archive-icon.png";
                case "png", "jpg", "jpeg", "gif", "bmp" -> iconPath = "/Client/images/file-icons/image-icon.png";
                case "mp3", "wav", "flac" -> iconPath = "/Client/images/file-icons/audio-icon.png";
                case "mp4", "avi", "mkv" -> iconPath = "/Client/images/file-icons/video-icon.png";
                case "java", "py", "js", "cpp", "c", "html", "css", "fxml" -> iconPath = "/Client/images/file-icons/code-icon.png";
                default -> iconPath = "/Client/images/file-icons/document-icon.png";
            }

            Image image = new Image(Objects.requireNonNull(getClass().getResource(iconPath)).toExternalForm());
            icon.setImage(image);
        } catch (Exception e) {
            // Fallback to default icon
            try {
                Image defaultImage = new Image(Objects.requireNonNull(
                        getClass().getResource("/Client/images/file-icons/document-icon.png")).toExternalForm());
                icon.setImage(defaultImage);
            } catch (Exception ex) {
                // Create a simple colored rectangle as final fallback
                System.err.println("Failed to load file icons: " + ex.getMessage());
            }
        }

        return icon;
    }

    /**
     * Opens a document using the system's default application
     */
    private void openDocument(DocumentInfo docInfo) {
        try {
            File file = new File(docInfo.getStoredPath());
            if (!file.exists()) {
                showTemporaryNotification("File Not Found\nThe document file could not be found.\n");
                return;
            }

            // Check if Desktop is supported
            if (!Desktop.isDesktopSupported()) {
                showTemporaryNotification("Not Supported\nOpening files is not supported on this system.\n");
                return;
            }

            Desktop desktop = Desktop.getDesktop();

            // Check if the open action is supported
            if (!desktop.isSupported(Desktop.Action.OPEN)) {
                showTemporaryNotification("Not Supported\nOpening files is not supported on this system.\n");
                return;
            }

            // Open the file with the system's default application
            desktop.open(file);

            System.out.println("Opening document: " + docInfo.getFileName());

        } catch (Exception e) {
            System.err.println("Error opening document: " + e.getMessage());
            showTemporaryNotification("Open Error\nFailed to open the document: " + e.getMessage() + "\n");
        }
    }

    /**
     * Saves a received document to a user-selected location
     */
    private void saveDocument(DocumentInfo docInfo) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Document");
        fileChooser.setInitialFileName(docInfo.getFileName());

        Stage currentStage = (Stage) messagesContainer.getScene().getWindow();
        File saveLocation = fileChooser.showSaveDialog(currentStage);

        if (saveLocation != null) {
            try {
                File sourceFile = new File(docInfo.getStoredPath());
                Files.copy(sourceFile.toPath(), saveLocation.toPath(), StandardCopyOption.REPLACE_EXISTING);

                showTemporaryNotification("Document saved to " + saveLocation.getName());

            } catch (Exception e) {
                System.err.println("Error saving document: " + e.getMessage());
                showTemporaryNotification("Save Error\nFailed to save the document: " + e.getMessage() + "\n");
            }
        }
    }

    /**
     * Simulates document upload progress
     */
    private void simulateDocumentUpload(DocumentInfo docInfo) {
        //TODO: In a real implementation, this would handle actual file upload to server

        // Simulate upload progress
        Timeline uploadProgress = new Timeline(
                new KeyFrame(Duration.seconds(0.5), e -> updateDocumentStatus(docInfo, "uploading")),
                new KeyFrame(Duration.seconds(1.5), e -> updateDocumentStatus(docInfo, "delivered")),
                new KeyFrame(Duration.seconds(3), e -> updateDocumentStatus(docInfo, "read"))
        );

        uploadProgress.play();
    }

    /**
     * Updates the status of a document message
     */
    private void updateDocumentStatus(DocumentInfo docInfo, String status) {
        // Find and update the document message status // TODO
        // This would typically update the UI status indicator
        System.out.println("Document " + docInfo.getFileName() + " status: " + status);
    }

    // ============ FILTER AND SEARCH ============

    /**
     * Performs a search on the chat list based on the input text.
     *
     * @param searchText The text to search for.
     */
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

    /**
     * Handles key press events in the message input field.
     * TODO: Implement full keyboard functionality.
     *
     * @param event The KeyEvent triggering the action.
     */
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

    /**
     * Handles the send button action based on input state.
     * TODO: Implement full send action logic.
     */
    private void handleSendAction() {
        String text = messageInputField.getText().trim();
        if (!text.isEmpty()) {
            sendMessage();
        } else {
            // Handle voice message recording
            startVoiceRecording();
        }
    }

    /**
     * Sends the current message and updates the UI.
     */
    private void sendMessage() {
        String text = messageInputField.getText().trim();
        if (text.isEmpty() || currentSelectedUser == null) return;

        if (isEditing && editingMessageBubble != null) {
            updateMessage(editingMessageBubble, text);
            closeReplyPreview();
        } else {
            String messageText = text;
            if (replyPreviewContainer.isVisible()) {
                messageText = "↪ " + replyMessageLabel.getText() + "\n\n" + text;
                closeReplyPreview();
            }

            addMessageBubble(messageText, true, getCurrentTime(), "sent", null);

            // Update chat list for new message
            currentSelectedUser.setLastMessage(text);
            currentSelectedUser.setTime(getCurrentTime());
            refreshChatList();

            simulateMessageDelivery();
        }

        messageInputField.clear();
        updateSendButtonState();
        Platform.runLater(() -> messageInputField.requestFocus());
    }

    /**
     * Simulates the delivery process of a sent message with timed status updates.
     */
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

    /**
     * Updates the status of the last sent message.
     *
     * @param status The new status (e.g., "delivered", "read").
     */
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

    /**
     * Updates the content of an existing message bubble after editing.
     *
     * @param bubble   The VBox of the message to update.
     * @param newText  The new text for the message.
     */
    private void updateMessage(VBox bubble, String newText) {
        Label messageTextLabel = (Label) bubble.getChildren().stream()
                .filter(node -> node instanceof Label && node.getStyleClass().contains("message-text"))
                .findFirst().orElse(null);

        HBox timeContainer = (HBox) bubble.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .findFirst().orElse(null);

        if (messageTextLabel == null || timeContainer == null) return;

        messageTextLabel.setText(newText);

        Label editedLabel = (Label) timeContainer.getChildren().stream()
                .filter(node -> node instanceof Label && "edited-label".equals(node.getId()))
                .findFirst().orElse(null);

        if (editedLabel == null) {
            editedLabel = new Label("edited");
            editedLabel.setId("edited-label");
            editedLabel.getStyleClass().add("message-time");
            timeContainer.getChildren().add(timeContainer.getChildren().size() - 2, editedLabel);
        }

        Label timeLabel = (Label) timeContainer.getChildren().stream()
                .filter(node -> node.getStyleClass().contains("message-time") && !"edited-label".equals(node.getId()))
                .findFirst().orElse(null);

        if (timeLabel != null) {
            timeLabel.setText(getCurrentTime());
        }

        // TODO: Server
    }

    /**
     * Updates the send button icon based on the input state (send or voice mode).
     */
    private void updateSendButtonState() {
        String text = messageInputField.getText().trim();

        try {
            if (text.isEmpty()) {
                // Show microphone for voice messages
                Image micIcon = new Image(Objects.requireNonNull(getClass().getResource("/Client/images/microphone-icon.png")).toExternalForm());
                // TODO: Verify image path and handle server-side icon loading if needed.
                sendButtonIcon.setImage(micIcon);
                sendButton.getStyleClass().add("voice-mode");
            } else {
                // Show send icon for text messages
                Image sendIcon = new Image(Objects.requireNonNull(getClass().getResource("/Client/images/send-icon.png")).toExternalForm());
                // TODO: Verify image path and handle server-side icon loading if needed.
                sendButtonIcon.setImage(sendIcon);
                sendButton.getStyleClass().remove("voice-mode");
            }
        } catch (Exception e) {
            System.err.println("Error updating send button icon: " + e.getMessage());
        }
    }

    /**
     * Adjusts the height of the message input TextArea based on content.
     */
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

    /**
     * Handles typing detection and sends status updates.
     * TODO (Server): Implement server-side typing status transmission.
     */
    private void handleTypingDetection() {
        // Implement typing detection logic
        // This would typically send typing status to server
        if (currentSelectedUser != null && !messageInputField.getText().trim().isEmpty()) {
            // TODO (Server): Send typing indicator to other users via server.
            sendTypingStatus(true);
        }
    }

    // ============ UI STATE MANAGEMENT ============

    /**
     * Displays the welcome state when no chat is selected.
     */
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

    /**
     * Hides the welcome state.
     */
    private void hideWelcomeState() {
        if (welcomeStateContainer != null) {
            welcomeStateContainer.setVisible(false);
        }
    }

    /**
     * Shows the chat area when a chat is selected.
     */
    private void showChatArea() {
        messagesScrollPane.setVisible(true);
        emptyChatStateContainer.setVisible(false);
    }

    /**
     * Shows the empty chat state when no messages are present.
     */
    private void showEmptyChatState() {
        emptyChatStateContainer.setVisible(true);
        messagesScrollPane.setVisible(false);
        welcomeStateContainer.setVisible(false);
    }

    /**
     * Enables chat controls when a chat is selected.
     */
    private void enableChatControls() {
        messageInputField.setDisable(false);
        sendButton.setDisable(false);
        attachmentButton.setDisable(false);
        callButton.setDisable(false);
        videoCallButton.setDisable(false);
        searchInChatButton.setDisable(false);
        moreOptionsButton.setDisable(false);
    }

    /**
     * Disables chat controls when no chat is selected.
     */
    private void disableChatControls() {
        messageInputField.setDisable(true);
        sendButton.setDisable(true);
        attachmentButton.setDisable(true);
        callButton.setDisable(true);
        videoCallButton.setDisable(true);
        searchInChatButton.setDisable(true);
        moreOptionsButton.setDisable(true);
    }

    // ============ RIGHT PANEL MANAGEMENT ============

    /**
     * Toggles the visibility of the right panel.
     */
    private void toggleRightPanel() {
        if (currentSelectedUser == null) return;

        if (isRightPanelVisible) {
            hideRightPanel();
        } else {
            showRightPanel();
        }
    }

    /**
     * Shows the right panel by adding it to the SplitPane and animating the divider positions
     * to maintain the main chat area as the largest component.
     */
    private void showRightPanel() {
        if (rightPanel == null || currentSelectedUser == null || splitPane.getItems().contains(rightPanel)) {
            return;
        }

        updateRightPanel(currentSelectedUser);
        rightPanel.setManaged(true);
        rightPanel.setVisible(true);

        // Get the current position of the first divider
        double firstDividerPosition = splitPane.getDividers().get(0).getPosition();

        // Add the right panel to the SplitPane
        splitPane.getItems().add(rightPanel);

        // === CRITICAL FIX: Set initial divider positions BEFORE animating ===
        // Set the starting positions for the animation to prevent the "jump".
        // The second divider starts at the far right.
        splitPane.setDividerPositions(firstDividerPosition, 1.0);

        // Define the target positions for the dividers
        double targetLeftPanelPercentage = 0.25;
        double targetRightPanelPercentage = 0.25;
        double targetSecondDividerPosition = 1.0 - targetRightPanelPercentage;

        // Animate the dividers to their new target positions
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(1),
                        new KeyValue(splitPane.getDividers().get(0).positionProperty(), targetLeftPanelPercentage, Interpolator.EASE_OUT),
                        new KeyValue(splitPane.getDividers().get(1).positionProperty(), targetSecondDividerPosition, Interpolator.EASE_OUT)
                )
        );

        timeline.setOnFinished(e -> {
            isRightPanelVisible = true;
        });
        timeline.play();
    }


    /**
     * Hides the right panel by animating the second divider to the edge
     * and then removing the panel from the SplitPane, allowing the chat area to expand.
     */
    @FXML
    private void hideRightPanel() {
        if (rightPanel == null || !splitPane.getItems().contains(rightPanel)) {
            return;
        }

        // Animate only the second divider sliding out
        final SplitPane.Divider secondDivider = splitPane.getDividers().get(1);
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(1),
                        new KeyValue(secondDivider.positionProperty(), 1.0, Interpolator.EASE_IN)
                )
        );

        // After the animation finishes, remove the panel
        timeline.setOnFinished(e -> {
            splitPane.getItems().remove(rightPanel);
            // The first divider's position will be automatically preserved
            isRightPanelVisible = false;
            rightPanel.setManaged(false);
            rightPanel.setVisible(false);
        });

        timeline.play();
    }

    /**
     * Updates the right panel with the selected user's information.
     *
     * @param user The UserViewModel to update the panel with.
     */
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

        // Update notification status
        notificationStatusLabel.setText(user.isMuted() ? "Disabled" : "Enabled");
        updateNotificationToggle(!user.isMuted());
    }

    /**
     * Updates the profile avatar with the user's image or a default one.
     *
     * @param user The UserViewModel to update the avatar for.
     */
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

    // ============ SCROLL MANAGEMENT ============

    /**
     * Scrolls the messages list to the bottom.
     */
    private void scrollToBottom() {
        Platform.runLater(() -> {
            messagesScrollPane.setVvalue(1.0);
            hideScrollToBottomButton();
        });
    }

    /**
     * Updates the visibility of the scroll-to-bottom button based on scroll position.
     */
    private void updateScrollToBottomVisibility() {
        double scrollValue = messagesScrollPane.getVvalue();
        boolean shouldShow = scrollValue < 0.85 && !messagesContainer.getChildren().isEmpty();

        if (shouldShow && !scrollToBottomContainer.isVisible()) {
            showScrollToBottomButton();
        } else if (!shouldShow && scrollToBottomContainer.isVisible()) {
            hideScrollToBottomButton();
        }
    }

    /**
     * Shows the scroll-to-bottom button with animation and updates unread count.
     */
    private void showScrollToBottomButton() {
        scrollToBottomContainer.setVisible(true);
        TelegramCellUtils.animateNotificationBadge(scrollToBottomContainer, true);
    }

    /**
     * Hides the scroll-to-bottom button and resets unread count.
     */
    private void hideScrollToBottomButton() {
        unreadScrollCount = 0;
        TelegramCellUtils.animateNotificationBadge(scrollToBottomContainer, false);
    }

    /**
     * Handles scroll position changes, triggering lazy loading if near the top.
     *
     * @param newValue The new scroll value.
     */
    private void handleScrollPositionChange(double newValue) {
        // Handle lazy loading of messages when scrolling to top
        // TODO UI: Develop this section for lazy loading implementation.
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

    // ============ THEME MANAGEMENT ============ // TODO UI

//    /**
//     * Toggles between dark and light themes.
//     */
//    private void toggleTheme() {
//        isDarkTheme = !isDarkTheme;
//        updateThemeClasses();
//
//        // Animate theme transition
//        animateThemeChange();
//
//        // Update night mode button icon
//        updateNightModeIcon();
//    }
//
//    /**
//     * Updates the theme classes on the main container.
//     * TODO: Add other themes in the future for more variety.
//     */
//    private void updateThemeClasses() {
//        mainChatContainer.getStyleClass().removeAll("light-theme", "dark-theme");
//        mainChatContainer.getStyleClass().add(isDarkTheme ? "dark-theme" : "light-theme");
//    }
//
//    /**
//     * Updates the night mode button icon based on the current theme.
//     */
//    private void updateNightModeIcon() {
//        try {
//            String iconPath = isDarkTheme ? "../images/sun-icon.png" : "../images/moon-icon.png"; // TODO UI
//            // TODO: Verify image paths and handle server-side icon loading if needed.
//            Image themeIcon = new Image(Objects.requireNonNull(getClass().getResource(iconPath)).toExternalForm());
//            ((ImageView) nightModeButton.getGraphic()).setImage(themeIcon);
//        } catch (Exception e) {
//            System.err.println("Error updating night mode icon: " + e.getMessage());
//        }
//    }

    // ============ CONNECTION STATUS ============

    /**
     * Updates the connection status UI.
     * TODO: Connect to the server for real-time status updates.
     *
     * @param connected True if connected, false otherwise.
     */
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

    /**
     * Animates the transition when selecting a new chat.
     */
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

    /**
     * Animates the theme change transition.
     */
    private void animateThemeChange() {
        // Smooth theme transition
        Timeline transition = new Timeline(new KeyFrame(Duration.millis(300),
                new KeyValue(mainChatContainer.opacityProperty(), 0.95)));

        transition.setAutoReverse(true);
        transition.setCycleCount(2);
        transition.play();
    }

    // ============ EVENT HANDLERS ============

    /**
     * Shows the sidebar if the controller is initialized.
     */
    private void showSideBar() {
        if (sidebarController != null) {
            Stage parentStage = (Stage) menuButton.getScene().getWindow();
            //SidebarUtil.showSidebarDialog(parentStage, "/Client/fxml/sidebarMenu.fxml", this);
        } else {
            System.out.println("SidebarController is not initialized!");
        }
    }

    /**
     * Initiates a voice call with the current selected user.
     */
    private void startVoiceCall() {
        if (currentSelectedUser == null) return;
        showTemporaryNotification("This feature will be implemented in the future.\n");
    }

    /**
     * Initiates a video call with the current selected user.
     */
    private void startVideoCall() {
        if (currentSelectedUser == null) return;
        showTemporaryNotification("This feature will be implemented in the future.\n");
    }

    /**
     * Shows a context menu with options for the current chat.
     */
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

    // ============ UTILITY METHODS ============

    /**
     * Returns a status icon based on the message status.
     * TODO: Implement more status icons or server-side logic.
     *
     * @param status The status to get an icon for (e.g., "sent", "delivered", "read").
     * @return The corresponding icon text.
     */
    private String getStatusIcon(String status) { // TODO UI
        return switch (status.toLowerCase()) {
            case "sent" -> "✓";
            case "delivered" -> "✓✓";
            case "read" -> "✓✓";
            default -> "";
        };
    }

    /**
     * Returns the current time in "HH:mm" format.
     *
     * @return The formatted current time.
     */
    private String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * Refreshes the chat list UI.
     */
    private void refreshChatList() {
        Platform.runLater(() -> {
            chatListView.refresh();
        });
    }

    /**
     * Handles the ESCAPE key press for various UI actions.
     */
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

    /**
     * Returns to the welcome state, clearing the current selection.
     */
    private void goBackToWelcomeState() {
        currentSelectedUser = null;
        chatListView.getSelectionModel().clearSelection();
        showWelcomeState();
        // TODO: Implement hiding typing indicator if implemented.
        // hideTypingIndicator(); // TODO UI
        closeReplyPreview();
    }

    /**
     * Closes the current chat and returns to the welcome state.
     */
    private void closeCurrentChat() {
        goBackToWelcomeState();
    }

    /**
     * Shows the search-in-chat interface (placeholder).
     */
    private void showSearchInChat() {
        System.out.println("Showing search in chat");
        // TODO: Implement search in chat functionality (UI: Design search interface, Server: Fetch search results).
    }

    /**
     * Creates a new group chat (placeholder).
     */
    private void createNewGroup() {
        System.out.println("Creating new group");
        // TODO: Implement new group creation (UI: Design group creation dialog, Server: Create group on server).
    }

    /**
     * Creates a new channel (placeholder).
     */
    private void createNewChannel() {
        System.out.println("Creating new channel");
        // TODO: Implement new channel creation (UI: Design channel creation dialog, Server: Create channel on server).
    }

    /**
     * Opens the contacts window (placeholder).
     */
    private void openContacts() {
        System.out.println("Opening contacts");
        // TODO: Implement contacts window (UI: Design contacts UI, Server: Fetch contact list).
    }

    /**
     * Opens the saved messages view (placeholder).
     */
    private void openSavedMessages() {
        System.out.println("Opening saved messages");
        // TODO: Implement saved messages (UI: Design saved messages UI, Server: Fetch saved messages).
    }

    /**
     * Opens the settings dialog (placeholder).
     */
    private void openSettings() {
        System.out.println("Opening settings");
        // TODO: Implement settings dialog (UI: Design settings UI, Server: Fetch user settings).
    }

    /**
     * Toggles the notification mute state for the current user.
     * This method now listens to the toggle button's action.
     */
    private void toggleNotifications() {
        if (currentSelectedUser == null) return;

        boolean isEnabled = notificationsToggle.isSelected();
        currentSelectedUser.setMuted(!isEnabled);

        notificationStatusLabel.setText(isEnabled ? "Enabled" : "Disabled");
        mutedIcon.setVisible(!isEnabled);

        String message = (isEnabled ? "Unmuted" : "Muted") + " " + currentSelectedUser.getUserName();
        showTemporaryNotification(message);
    }

    /**
     * Handle about HBox click
     */
    @FXML
    private void handleAbout() {
        System.out.println("About clicked");
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/11-devs/Telegram-clone"));
        } catch (Exception e) {
            System.out.println("Failed to open link: " + e.getMessage());
        }
    }

    /**
     * Updates the notification toggle button's state based on user data.
     * This is called when the right panel is shown or the user changes.
     *
     * @param enabled True if notifications are enabled, false if disabled (muted).
     */
    private void updateNotificationToggle(boolean enabled) {
        if (notificationsToggle == null) return;
        notificationsToggle.setSelected(enabled);
    }

    /**
     * Toggles the mute state of the current chat.
     */
    private void toggleMute() {
        toggleNotifications();
    }

    /**
     * Toggles the pinned state of the current chat.
     */
    private void togglePin() {
        if (currentSelectedUser == null) return;

        boolean newPinState = !currentSelectedUser.isPinned();
        currentSelectedUser.setPinned(newPinState);

        String message = (newPinState ? "Pinned" : "Unpinned") + " " + currentSelectedUser.getUserName();
        showTemporaryNotification(message);

        refreshChatList();
    }

    /**
     * Clears the chat history with a confirmation dialog.
     */
    private void clearChatHistory() {
        if (currentSelectedUser == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); // TODO UI
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

    /**
     * Blocks the current user with a confirmation dialog.
     */
    private void blockUser() {
        if (currentSelectedUser == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); // TODO UI
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

    /**
     * Starts voice recording (placeholder).
     */
    private void startVoiceRecording() {
        System.out.println("Starting voice recording");
        // TODO: Implement voice recording functionality (UI: Show recording UI, Server: Handle audio upload).
        // Visual feedback
        sendButton.getStyleClass().add("recording");
        showTemporaryNotification("Voice recording started");
    }

    /**
     * Edits the last outgoing message by loading it into the input field.
     */
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

    /**
     * Loads more messages when scrolling to the top (placeholder).
     */
    private void loadMoreMessages() {
        // TODO: Implement lazy loading of older messages (Server: Fetch older messages, UI: Append to messagesContainer).
        System.out.println("Loading more messages...");
    }

    /**
     * Opens a media item for viewing (placeholder).
     *
     * @param type  The type of media.
     * @param index The index of the media item.
     */
    private void openMediaItem(String type, int index) {
        System.out.println("Opening " + type + " item " + index);
        // TODO: Implement media viewer (UI: Design media viewer, Server: Fetch media content).
    }

    /**
     * Shows a context menu for a message with appropriate actions
     * based on whether the message is incoming or outgoing.
     *
     * @param event The MouseEvent triggering the menu.
     */
    private void showMessageContextMenu(MouseEvent event) {
        if (activeMessageContextMenu != null && activeMessageContextMenu.isShowing()) {
            activeMessageContextMenu.hide();
        }

        ContextMenu newMenu = new ContextMenu();
        newMenu.setAutoHide(true);

        VBox messageBubble = (VBox) event.getSource();
        boolean isOutgoing = messageBubble.getStyleClass().contains("outgoing");

        MenuItem replyItem = createIconMenuItem("Reply", "/Client/images/context-menu/reply.png");
        replyItem.setOnAction(e -> showReplyPreview(messageBubble));

        MenuItem forwardItem = createIconMenuItem("Forward", "/Client/images/context-menu/forward.png");
        forwardItem.setOnAction(e -> forwardMessage());

        MenuItem copyItem = createIconMenuItem("Copy Text", "/Client/images/context-menu/copy.png");
        copyItem.setOnAction(e -> copyMessageText(messageBubble));

        MenuItem deleteItem = createIconMenuItem("Delete", "/Client/images/context-menu/delete.png");
        deleteItem.setOnAction(e -> deleteMessage(messageBubble));

        newMenu.getItems().addAll(replyItem, forwardItem);

        if (isOutgoing) {
            MenuItem editItem = createIconMenuItem("Edit", "/Client/images/context-menu/edit.png");
            editItem.setOnAction(e -> editMessage(messageBubble));
            newMenu.getItems().add(1, editItem);
        }

        newMenu.getItems().addAll(copyItem, deleteItem);

        newMenu.show(messageBubble, event.getScreenX(), event.getScreenY());
        activeMessageContextMenu = newMenu;
    }

    /**
     * Helper method to create a MenuItem with a custom Image icon.
     *
     * @param text     The text of the menu item.
     * @param imageUrl The path to the icon image (relative to the resources folder).
     * @return A configured MenuItem with an icon.
     */
    private MenuItem createIconMenuItem(String text, String imageUrl) {
        ImageView icon = new ImageView();
        try {
            Image image = new Image(Objects.requireNonNull(getClass().getResource(imageUrl)).toExternalForm());
            icon.setImage(image);
        } catch (Exception e) {
            System.err.println("Error loading icon for MenuItem: " + imageUrl);
            // In case of error, an empty icon is created.
        }

        icon.setFitWidth(20);
        icon.setFitHeight(20);
        icon.setPreserveRatio(true);

        icon.getStyleClass().add("context-menu-icon-image");

        MenuItem menuItem = new MenuItem(text);
        menuItem.setGraphic(icon);

        return menuItem;
    }

    // ============ ATTACHMENT METHODS ============

    /**
     * Attaches a photo or video (placeholder).
     */
    private void attachPhoto() {
        System.out.println("Attaching photo/video");
        // TODO: Implement file chooser for media (UI: Design file chooser, Server: Upload media).
    }

    /**
     * Creates a poll (placeholder).
     */
    private void createPoll() {
        System.out.println("Creating poll");
        // TODO: Implement poll creation dialog (UI: Design poll creation UI, Server: Create poll on server).
    }

    /**
     * Attaches a contact (placeholder).
     */
    private void attachContact() {
        System.out.println("Attaching contact");
        // TODO: Implement contact picker (UI: Design contact picker, Server: Fetch contacts).
    }

    /**
     * Attaches a location (placeholder).
     */
    private void attachLocation() {
        System.out.println("Attaching location");
        // TODO: Implement location picker (UI: Design location picker, Server: Handle location data).
    }

    // ============ MESSAGE ACTIONS ============

    /**
     * Forwards a message (placeholder).
     */
    private void forwardMessage() {
        System.out.println("Forwarding message");
        // TODO: Implement message forwarding (Server: Send message to new chat, UI: Update UI).
    }

    /**
     * Puts the UI into editing mode for the selected message.
     * It uses the reply preview container to display the editing state.
     *
     * @param messageBubble The VBox of the message to be edited.
     */
    private void editMessage(VBox messageBubble) {
        if (messageBubble == null) return;

        resetReplyEditState();

        isEditing = true;
        editingMessageBubble = messageBubble;
        replyToMessage = null;

        Label messageTextLabel = (Label) messageBubble.getChildren().stream()
                .filter(node -> node instanceof Label && node.getStyleClass().contains("message-text"))
                .findFirst()
                .orElse(null);

        if (messageTextLabel == null) {
            isEditing = false;
            editingMessageBubble = null;
            return;
        }

        replyToLabel.setText("Edit Message");
        replyMessageLabel.setText(messageTextLabel.getText());
        messageInputField.setText(messageTextLabel.getText());

        replyPreviewContainer.setVisible(true);
        replyPreviewContainer.setManaged(true);

        messageInputField.requestFocus();
        messageInputField.positionCaret(messageTextLabel.getText().length());
    }

    /**
     * Deletes a message from the UI and optionally from the backend.
     *
     * @param messageBubble The VBox of the message to be deleted.
     */
    private void deleteMessage(VBox messageBubble) {
        if (messageBubble == null) return;

        // Remove the HBox that contains the VBox message bubble
        if (messageBubble.getParent() instanceof HBox) {
            ((HBox) messageBubble.getParent()).getChildren().remove(messageBubble); // remove bubble from HBox
            messagesContainer.getChildren().remove(messageBubble.getParent()); // remove HBox from messagesContainer
        } else {
            messagesContainer.getChildren().remove(messageBubble); // Fallback: remove VBox directly
        }

        // TODO: (Server) Send delete request to the server
        System.out.println("Message deleted from UI. (Implement server deletion)");

        // Check if the chat is now empty
        if (messagesContainer.getChildren().isEmpty()) {
            showEmptyChatState();
            // TODO: Update currentSelectedUser.lastMessage accordingly
        }
    }

    /**
     * Copies the text of a message to the system clipboard.
     *
     * @param messageBubble The VBox of the message from which to copy the text.
     */
    private void copyMessageText(VBox messageBubble) {
        if (messageBubble == null) return;

        Label messageTextLabel = (Label) messageBubble.getChildren().stream()
                .filter(node -> node instanceof Label && node.getStyleClass().contains("message-text"))
                .findFirst()
                .orElse(null);

        if (messageTextLabel != null) {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(messageTextLabel.getText());
            clipboard.setContent(content);
            showTemporaryNotification("Message text copied to clipboard!");
            System.out.println("Text copied: " + messageTextLabel.getText());
        } else {
            System.err.println("Could not find message text label in the bubble to copy.");
        }
    }

    // ============ UTILITY METHODS ============

    /**
     * Loads the default header avatar image.
     * TODO (Server): Fetch default avatar from server if needed.
     */
    private void loadDefaultHeaderAvatar() {
        try {
            Image defaultAvatar = new Image(Objects.requireNonNull(getClass().getResource("/Client/images/11Devs-white.png")).toExternalForm());
            headerAvatarImage.setImage(defaultAvatar);
        } catch (Exception e) {
            System.err.println("Error loading default header avatar: " + e.getMessage());
        }
    }

    /**
     * Loads the default profile avatar image.
     * TODO (Server): Fetch default avatar from server if needed or provide a dynamic default image.
     */
    private void loadDefaultProfileAvatar() {
        try {
            // Attempt to load the default avatar image from resources
            Image defaultAvatar = new Image(Objects.requireNonNull(getClass().getResource("/Client/images/11Devs-white.png")).toExternalForm());
            profileAvatarImage.setImage(defaultAvatar);
        } catch (Exception e) {
            // Log error if loading fails
            System.err.println("Error loading default profile avatar: " + e.getMessage());
            // TODO (Server): Notify server of failure and request fallback avatar if available.
            // TODO (UI): Display a placeholder or error icon to the user.
        }
    }

    /**
     * Displays a temporary notification overlay at the top of the screen.
     * @param message The message to display in the notification.
     */
    private void showTemporaryNotification(String message) {
        Stage parentStage = (Stage) mainChatContainer.getScene().getWindow();
        showNotificationDialog(parentStage, message);
    }

    /**
     * Sends the typing status to the server.
     * TODO (Server): Implement server-side communication to update other users' interfaces.
     *
     * @param isTyping True if the user is typing, false otherwise.
     */
    private void sendTypingStatus(boolean isTyping) {
        // In a real implementation, this would send typing status to server
        System.out.println("Typing status: " + isTyping);
        // TODO (Server): Establish a WebSocket or REST call to send typing status to the server.
        // TODO (UI): Update local UI to reflect typing state if needed (e.g., show indicator).
    }

    // ============ PUBLIC API METHODS ============

    /**
     * Adds a new message to the chat interface.
     * TODO: Enhance with server synchronization and error handling.
     *
     * @param text       The text content of the message.
     * @param isOutgoing True if the message is from the current user, false otherwise.
     * @param senderName The name of the sender (null for outgoing messages).
     */
    public void addNewMessage(String text, boolean isOutgoing, String senderName) {
        Platform.runLater(() -> {
            // Add the message bubble to the UI
            addMessageBubble(text, isOutgoing, getCurrentTime(), isOutgoing ? "sent" : "received", senderName);

            if (!isOutgoing) {
                // Increment unread count for incoming messages
                unreadScrollCount++;
                updateScrollToBottomVisibility();
            }

            // Auto-scroll if near the bottom
            if (messagesScrollPane.getVvalue() > 0.9) {
                scrollToBottom();
            }
            // TODO: Synchronize with server to ensure message persistence.
            // TODO (UI): Add animation or visual cue for new messages.
        });
    }

    /**
     * Updates the online status of a user in the chat list and UI.
     * TODO: Implement server-driven status updates.
     *
     * @param userName The name of the user whose status is updated.
     * @param isOnline True if the user is online, false otherwise.
     */
    public void updateUserOnlineStatus(String userName, boolean isOnline) {
        Platform.runLater(() -> {
            for (UserViewModel user : allChatUsers) {
                if (user.getUserName().equals(userName)) {
                    user.setOnline(isOnline);
                    user.setLastSeen(isOnline ? "online" : "last seen just now");

                    if (user == currentSelectedUser) {
                        // Update chat header and right panel if visible
                        updateChatHeader(user);
                        if (isRightPanelVisible) {
                            updateRightPanel(user);
                        }
                    }

                    refreshChatList();
                    break;
                }
            }
            // TODO (Server): Fetch real-time status from server instead of local update.
            // TODO (UI): Add visual feedback for status change (e.g., animation).
        });
    }

    /**
     * Adds a new user to the chat list.
     * TODO: Synchronize with server to add user globally.
     *
     * @param user The UserViewModel to add.
     */
    public void addUser(UserViewModel user) {
        Platform.runLater(() -> {
            allChatUsers.add(user);
            // TODO (Server): Notify server to add user to the chat or group.
            // TODO (UI): Update UI to reflect new user (e.g., sort or notify).
        });
    }

    /**
     * Removes a user from the chat list.
     * TODO: Synchronize with server to remove user globally.
     *
     * @param user The UserViewModel to remove.
     */
    public void removeUser(UserViewModel user) {
        Platform.runLater(() -> {
            allChatUsers.remove(user);
            filteredChatUsers.remove(user);

            if (currentSelectedUser == user) {
                goBackToWelcomeState();
            }
            // TODO (Server): Notify server to remove user from the chat or group.
            // TODO (UI): Add confirmation dialog or notification for removal.
        });
    }

    /**
     * Selects a user by name from the chat list.
     *
     * @param userName The name of the user to select.
     */
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

    /**
     * Returns the currently selected user.
     *
     * @return The current UserViewModel, or null if none selected.
     */
    public UserViewModel getCurrentSelectedUser() {
        return currentSelectedUser;
    }

    /**
     * Sets the theme of the application.
     * TODO: Synchronize theme change with server if persistent.
     *
     * @param darkTheme True for dark theme, false for light theme.
     */
    public void setTheme(boolean darkTheme) {
        this.isDarkTheme = darkTheme;
        // updateThemeClasses(); TODO UI
        // updateNightModeIcon(); TODO UI
        // TODO (Server): Send theme preference to server for persistence.
        // TODO (UI): Add transition animation for theme switch.
    }

    // ============ CLEANUP ============

    /**
     * Cleans up resources by stopping animations and clearing data.
     */
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
        // TODO (UI): Ensure all event listeners are removed to prevent memory leaks.
        // TODO (Server): Notify server of cleanup or session end if applicable.
    }
}