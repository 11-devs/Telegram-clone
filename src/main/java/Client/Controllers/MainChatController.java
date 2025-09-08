package Client.Controllers;

import Client.AppConnectionManager;
import Client.RpcCaller;
import Client.Services.ChatService;
import Client.Services.FileDownloadService;
import Client.Services.UI.ChatUIService;
import Client.Tasks.DownloadTask;
import Client.Tasks.UploadTask;
import JSocket2.Core.Client.ConnectionManager;
import JSocket2.Protocol.Rpc.RpcResponse;
import JSocket2.Protocol.StatusCode;
import JSocket2.Protocol.Transfer.FileInfoModel;
import JSocket2.Protocol.Transfer.IProgressListener;
import JSocket2.Protocol.Transfer.TransferInfo;
import Shared.Api.Models.ChatController.GetChatInfoOutputModel;
import Shared.Api.Models.ContactController.ContactInfo;
import Shared.Api.Models.MediaController.CreateMediaInputModel;
import Shared.Api.Models.MessageController.GetMessageOutputModel;
import Shared.Api.Models.MessageController.SendMessageInputModel;
import Shared.Api.Models.MessageController.SendMessageOutputModel;
import Shared.Events.Models.*;
import Shared.Models.*;
import Shared.Utils.SceneUtil;
import Shared.Utils.SidebarUtil;
import Shared.Models.Message.MessageType;
import Shared.Utils.*;
import com.google.gson.Gson;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.Duration;
import com.jfoenix.controls.JFXToggleButton;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.geometry.Point2D;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.beans.Observable;
import javafx.scene.Node;

import static JSocket2.Utils.FileUtil.getFileExtension;
import static Shared.Utils.DialogUtil.showNotificationDialog;
import static Shared.Utils.FileUtil.*;

/**
 * The MainChatController class manages the main chat interface of the application.
 * It handles UI initialization, chat selection, message handling, and user interactions.
 * This controller is tied to the main chat FXML layout.
 */
public class MainChatController implements Initializable {

    private static final String BOLD_MARKER_PREFIX = "**";
    private static final String BOLD_MARKER_SUFFIX = "**";
    private static final String ITALIC_MARKER_PREFIX = "__";
    private static final String ITALIC_MARKER_SUFFIX = "__";
    private static final String UNDERLINE_MARKER_PREFIX = "++";
    private static final String UNDERLINE_MARKER_SUFFIX = "++";
    private static final String SPOILER_MARKER_PREFIX = "||";
    private static final String SPOILER_MARKER_SUFFIX = "||";
    private final int MIN_PUBLIC_SEARCH_LENGTH = 5;
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

    public ListView<ChatViewModel> getChatListView() {
        return chatListView;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    private String currentUserId;
    /**
     * ListView displaying the chat list with ChatViewModel items.
     */
    @FXML private ListView<ChatViewModel> chatListView;
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
    @FXML private HBox DeleteContactBox;
    /**
     * Handle right panel
     */
    @FXML private Button closeRightPanelButton;

    // ============ DATA AND STATE ============
    /**
     * Default users name.
     */
    private String ownUsername = "Me";

    public ObservableList<ChatViewModel> getAllChatUsers() {
        return allChatUsers;
    }

    /**
     * ObservableList of all chat users.
     */
    private ObservableList<ChatViewModel> allChatUsers;
    /**
     * ObservableList of filtered chat users based on search.
     */
    private ObservableList<ChatViewModel> filteredChatUsers;
    /**
     * The currently selected user or chat.
     */
    private ChatViewModel currentSelectedChat;
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
    private ReplyInfo activeReplyInfo = null;

    @FXML private StackPane messageInputWrapper;
    @FXML private VBox messageInputContainer;
    @FXML private Button channelMuteToggleButton;

    // Sidebar Menu
    /**
     * Controller for the sidebar menu.
     */
    private SidebarMenuController sidebarController;
    private Timer typingTimer;
    private TimerTask typingStopTask;
    private boolean isCurrentlyTyping = false;
    private String originalEditText;
    private final Map<String, DownloadTask> activeDownloadTasks = new ConcurrentHashMap<>();
    private final Map<String, HBox> temporaryMessageNodes = new ConcurrentHashMap<>();
    /**
     * Initializes the controller after the FXML file is loaded.
     * Sets up the UI components, data, event handlers, and initial state.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    private ConnectionManager connectionManager;
    private RpcCaller rpcCaller;
    private ChatService chatService;
    private ChatUIService chatUIService;
    private FileDownloadService fileDownloadService;
    //private UserIdentity currentUser;
    private final Gson gson = new Gson();
public ChatService getChatService() {
    return chatService;
}


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connectionManager = AppConnectionManager.getInstance().getConnectionManager();
        rpcCaller = AppConnectionManager.getInstance().getRpcCaller();
        chatService = new ChatService(rpcCaller);
        fileDownloadService = FileDownloadService.getInstance();
        fileDownloadService.initialize();;
        chatUIService = connectionManager.getClient().getServiceProvider().GetService(ChatUIService.class);
        chatUIService.setActiveChatController(this);
        typingTimer = new Timer(true);
        searchTimer = new Timer(true);
        //currentUser = connectionManager.getClient().getUserIdentity();
        initializeSidebarsSplitPane();
        initializeData();
        setupChatList();
        setupMessageInput();
        setupMessageInputFormatting();
        setupEventHandlers();
        setupAnimations();
        // TODO: Implement keyboard shortcut setup for enhanced navigation.
        loadInitialState();
    }
    public void handleIncomingMessage(NewMessageEventModel message) {
        // Check if the message belongs to the currently opened chat
        if (currentSelectedChat != null && currentSelectedChat.getChatId().equals(message.getChatId().toString())) {
            String formattedTime = LocalDateTime.parse(message.getTimestamp()).format(DateTimeFormatter.ofPattern("HH:mm"));

            if (message.getMessageType() == MessageType.TEXT) {
                addMessageBubble(message.getTextContent(), false, formattedTime, "received", message.getSenderName(),
                        false, message.getRepliedToSenderName(), message.getRepliedToMessageContent(), message.getForwardedFromSenderName());
            } else if (message.getMessageType() == MessageType.MEDIA) {
                DocumentInfo docInfo = new DocumentInfo(message);
                docInfo.setSenderName(message.getSenderName());
                // For media, the bubble needs reply/forward info too.
                HBox messageNode = addDocumentMessageBubble(docInfo, false, formattedTime, "received",
                        message.getRepliedToSenderName(), message.getRepliedToMessageContent(), message.getForwardedFromSenderName());
            }

            if (messagesScrollPane.getVvalue() > 0.9) {
                scrollToBottom();
            }
            Task<Void> markAsReadTask = chatService.markChatAsRead(UUID.fromString(currentSelectedChat.getChatId()));
            new Thread(markAsReadTask).start();
        }

        // Update the corresponding chat item in the list
        allChatUsers.stream()
                .filter(user -> user.getChatId().equals(message.getChatId().toString()))
                .findFirst()
                .ifPresent(user -> {
                    String notificationMessage = message.getTextContent() != null ? message.getTextContent() : "Media";
                    user.setLastMessage(message.getTextContent() != null ? message.getTextContent() : "Media");
                    user.setTime(message.getTimestamp());

                    // Increment unread count only if the chat is not currently active
                    if (currentSelectedChat == null || !currentSelectedChat.getChatId().equals(user.getChatId())) {
                        try {
                            int currentCount = Integer.parseInt(user.getNotificationsNumber());
                            user.setNotificationsNumber(String.valueOf(currentCount + 1));
                        } catch (NumberFormatException e) {
                            user.setNotificationsNumber("1");
                        }
                    }
                    if (!user.isMuted()) {
                        SystemNotificationUtil.showNotification(user.getDisplayName(), notificationMessage);
                    }
                    sortAndRefreshChatList();
                });
    }


    private Label findStatusLabelInMessageNode(HBox messageNode) {
        if (messageNode == null || messageNode.getChildren().isEmpty() || !(messageNode.getChildren().getFirst() instanceof VBox bubble)) {
            return null;
        }
        if (bubble.getChildren().isEmpty() || !(bubble.getChildren().getLast() instanceof HBox timeContainer)) {
            return null;
        }
        for (var node : timeContainer.getChildren()) {
            if (node.getStyleClass().contains("message-status")) {
                return (Label) node;
            }
        }
        return null;
    }

    public void handleMessageEdited(MessageEditedEventModel eventModel) {
        if (currentSelectedChat != null && currentSelectedChat.getChatId().equals(eventModel.getChatId().toString())) {
            Platform.runLater(() -> {
                messagesContainer.getChildren().stream()
                        .filter(node -> node instanceof HBox)
                        .map(node -> (HBox) node)
                        .flatMap(hBox -> hBox.getChildren().stream())
                        .filter(node -> node instanceof VBox && node.getProperties().containsKey("messageId") && node.getProperties().get("messageId").equals(eventModel.getMessageId()))
                        .map(node -> (VBox) node)
                        .findFirst() // Use findFirst as messageId should be unique
                        .ifPresent(bubble -> {
                            // 1. Update the raw_text property for future actions like copy/edit
                            updateMessage(bubble,eventModel.getNewContent());
                        });
            });
        }
        // Update chat list item last message if it was the edited one
        allChatUsers.stream()
                .filter(user -> user.getChatId().equals(eventModel.getChatId().toString()))
                .findFirst()
                .ifPresent(user -> {
                    // Update with the clean new content. The cell controller will format it.
                    user.setLastMessage(eventModel.getNewContent());
                    user.setTime(eventModel.getTimestamp());
                    sortAndRefreshChatList();
                });
    }

    public void handleMessageDeleted(MessageDeletedEventModel eventModel) {
        if (currentSelectedChat != null && currentSelectedChat.getChatId().equals(eventModel.getChatId().toString())) {
            Platform.runLater(() -> {
                // Find the message bubble by its ID and remove it
                boolean removed = messagesContainer.getChildren().removeIf(node ->
                        node instanceof HBox && ((HBox) node).getChildren().stream()
                                .anyMatch(child -> child instanceof VBox && child.getProperties().containsKey("messageId") && child.getProperties().get("messageId").equals(eventModel.getMessageId()))
                );
                if (removed) {
                    messagesContainer.requestLayout();
                }
                if (messagesContainer.getChildren().isEmpty()) {
                    showEmptyChatState();
                }
            });
        }
        // Potentially update the chat list if the deleted message was the last one
        // This would require fetching the new last message for the chat.
        allChatUsers.stream()
                .filter(user -> user.getChatId().equals(eventModel.getChatId().toString()))
                .findFirst()
                .ifPresent(user -> {
                    if (eventModel.isLastMessageDeleted()) {
                        user.setLastMessage(eventModel.getNewLastMessageContent());
                        user.setTime(eventModel.getNewLastMessageTimestamp());
                    }
                    sortAndRefreshChatList();
                });
    }


    public void handleMessageRead(MessageReadEventModel eventModel) {
        if (currentSelectedChat != null && currentSelectedChat.getChatId().equals(eventModel.getChatId().toString())) {
            Platform.runLater(() -> {
                // Find all outgoing messages before or at this timestamp and mark them as read
                messagesContainer.getChildren().stream()
                        .filter(node -> node instanceof HBox && node.getStyleClass().contains("outgoing"))
                        .map(node -> (HBox) node)
                        .filter(hBox -> hBox.getChildren().getFirst() instanceof VBox bubble && bubble.getProperties().containsKey("messageTimestamp"))
                        .forEach(hBox -> {
                            VBox bubble = (VBox) hBox.getChildren().getFirst();
                            LocalDateTime messageTime = (LocalDateTime) bubble.getProperties().get("messageTimestamp");
                            LocalDateTime readTime = LocalDateTime.parse(eventModel.getReadTimestamp());
                            if (!messageTime.isAfter(readTime)) {
                                updateMessageStatus(hBox, "read", null);
                            }
                        });
            });
        }

        // This event means OUR messages have been read by the other party.
        // We need to update the status icon in the chat list, not our unread count.
        allChatUsers.stream()
                .filter(user -> user.getChatId().equals(eventModel.getChatId().toString()))
                .findFirst()
                .ifPresent(user -> {
                    // CORRECT: Update the message status to 'read' (double tick)
                    user.setMessageStatus("read");
                    sortAndRefreshChatList();
                });
    }

    public void handleUserTyping(UserIsTypingEventModel eventModel) {
        if (currentSelectedChat != null && currentSelectedChat.getChatId().equals(eventModel.getChatId().toString())) {
            // Update the subtitle in the chat header to show typing status
            Platform.runLater(() -> {
                if (eventModel.isTyping()) {
                    showTypingIndicator(eventModel.getSenderName());
                } else {
                    hideTypingIndicator();
                }
            });
        }
        // Also update the typing status in the chat list preview
        allChatUsers.stream()
                .filter(user -> user.getChatId().equals(eventModel.getChatId().toString()))
                .findFirst()
                .ifPresent(user -> Platform.runLater(() -> {
                    user.setTyping(eventModel.isTyping());
                    if (eventModel.isTyping()) {
                        user.setTypingUserName(eventModel.getSenderName());
                    } else {
                        user.setTypingUserName(null);
                    }
                }));
    }


// Add these two new methods anywhere inside the MainChatController class.

    /**
     * Updates the status and timestamp of a specific message bubble.
     * This version is specifically designed to work with IMAGE-BASED status icons (ImageView).
     *
     * @param messageNode The HBox container of the message to update.
     * @param status      The new status ("sending", "delivered", "read", "failed").
     * @param time        The new time string. If null, time is not updated.
     */
    private void updateMessageStatus(HBox messageNode, String status, String time) {
        if (messageNode == null) return;

        // The VBox bubble is the first child for outgoing messages
        if (messageNode.getChildren().isEmpty() || !(messageNode.getChildren().getFirst() instanceof VBox bubble)) {
            return;
        }

        // The HBox timeContainer is usually the last child of the bubble.
        // We find it by searching for the last HBox in the bubble's children.
        Optional<HBox> timeContainerOpt = bubble.getChildren().stream()
                .filter(n -> n instanceof HBox)
                .map(n -> (HBox) n)
                .reduce((first, second) -> second); // Gets the last HBox

        if (timeContainerOpt.isEmpty()) return;
        HBox timeContainer = timeContainerOpt.get();

        // --- 1. Update Time (if provided) ---
        if (time != null) {
            // Find the time Label (which is not a status icon) and update its text
            timeContainer.getChildren().stream()
                    .filter(n -> n instanceof Label && !n.getStyleClass().contains("edited-indicator"))
                    .map(n -> (Label) n)
                    .findFirst()
                    .ifPresent(timeLabel -> timeLabel.setText(time));
        }

        // --- 2. Update Status Icon ---
        // Get the correct image path for the new status
        String imagePath = switch (status.toLowerCase()) {
            case "sending" -> "/Client/images/status/sending.png";
            case "sent", "delivered" -> "/Client/images/status/sent.png";
            case "read" -> "/Client/images/status/read.png";
            default -> null; // For "failed" or other statuses, we show no icon.
        };

        // Find the existing status icon ImageView
        Optional<ImageView> existingIconOpt = timeContainer.getChildren().stream()
                .filter(n -> n instanceof ImageView && n.getStyleClass().contains("status-icon-image"))
                .map(n -> (ImageView) n)
                .findFirst();

        if (imagePath != null) {
            // If the new status has an icon (sending, sent, read)
            try {
                Image newStatusImage = new Image(Objects.requireNonNull(getClass().getResource(imagePath)).toExternalForm());
                if (existingIconOpt.isPresent()) {
                    // If an icon already exists, just update its image
                    existingIconOpt.get().setImage(newStatusImage);
                } else {
                    // This case shouldn't happen if create...Bubble methods work correctly, but as a fallback:
                    // If no icon exists, create a new one and add it.
                    ImageView statusIcon = new ImageView(newStatusImage);
                    statusIcon.setFitHeight(16);
                    statusIcon.setPreserveRatio(true);
                    statusIcon.getStyleClass().add("status-icon-image");
                    timeContainer.getChildren().add(statusIcon);
                }
            } catch (Exception e) {
                System.err.println("Failed to load or update status icon: " + imagePath);
            }
        } else {
            // If the new status has NO icon (e.g., "failed"), remove the existing icon if it's there.
            existingIconOpt.ifPresent(imageView -> timeContainer.getChildren().remove(imageView));
        }
    }

    private void updateLastOutgoingMessageStatus(String status, String timestamp) {
        // Iterate messagesContainer from the end to find the last outgoing message
        for (int i = messagesContainer.getChildren().size() - 1; i >= 0; i--) {
            Node node = messagesContainer.getChildren().get(i);
            if (node instanceof HBox && node.getStyleClass().contains("outgoing")) {
                HBox lastOutgoingMessageHBox = (HBox) node;
                updateMessageStatus(lastOutgoingMessageHBox, status, LocalDateTime.parse(timestamp).format(DateTimeFormatter.ofPattern("HH:mm")));
                break; // Stop after updating the first (most recent) outgoing message
            }
        }
    }

    /**
     * Sorts the master chat list based on the latest message time and then
     * explicitly refreshes the ListView to reflect all changes, including internal
     * property changes like message status.
     */
    private void sortAndRefreshChatList() {
        Platform.runLater(() -> {
            ChatViewModel selected = chatListView.getSelectionModel().getSelectedItem();
            allChatUsers.sort((u1, u2) -> {
                try {
                    LocalDateTime t1 = (u1.getTime() != null && !u1.getTime().isEmpty()) ? LocalDateTime.parse(u1.getTime()) : LocalDateTime.MIN;
                    LocalDateTime t2 = (u2.getTime() != null && !u2.getTime().isEmpty()) ? LocalDateTime.parse(u2.getTime()) : LocalDateTime.MIN;
                    return t2.compareTo(t1);
                } catch (Exception e) {
                    return 0;
                }
            });

            performSearch(searchField.getText());

            if (selected != null) {
                chatListView.getSelectionModel().select(selected);
            }

            chatListView.refresh();

            chatListView.scrollTo(0);
        });
    }
    // ============ INITIALIZATION METHODS ============

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
        Callback<ChatViewModel, Observable[]> extractor = user -> new Observable[]{
                user.isOnlineProperty(),
                user.lastSeenProperty(),
                user.lastMessageProperty(),
                user.timeProperty(),
                user.notificationsNumberProperty(),
                user.isTypingProperty(),
                user.messageStatusProperty()
        };

        allChatUsers = FXCollections.observableArrayList(extractor);
        filteredChatUsers = FXCollections.observableArrayList(extractor);
        currentMessages = FXCollections.observableArrayList();

        // Load sample data for demonstration
        loadUserChatsFromServer();
        // TODO: Implement fetching data from the server or database.
    }
    void loadUserChatsFromServer() {

        Task<RpcResponse<GetChatInfoOutputModel[]>> getChatsTask = chatService.fetchUserChats();

        getChatsTask.setOnSucceeded(event -> {
            RpcResponse<GetChatInfoOutputModel[]> response = getChatsTask.getValue();
            if (response.getStatusCode() == StatusCode.OK && response.getPayload() != null) {
                List<ChatViewModel> chatViewModels = new ArrayList<>();
                for (GetChatInfoOutputModel chat : response.getPayload()) {
                    // --- MODIFIED LOGIC: Use real data from the server ---
                    ChatViewModel uvm = new ChatViewModelBuilder()
                            .chatId(chat.getChatId().toString())
                            .userId(chat.getUserId().toString())
                            .avatarId(chat.getProfilePictureId())
                            .username(chat.getUsername())
                            .bio(chat.getBio())
                            .displayName(chat.getTitle() != null ? chat.getTitle() : "Private Chat")
                            .lastMessage(chat.getLastMessage())
                            .time(chat.getLastMessageTimestamp())
                            .type(chat.getType())
                            .notificationsNumber(String.valueOf(chat.getUnreadCount()))
                            .isMuted(chat.isMuted())
                            .isOnline(chat.isOnline())
                            .lastSeen(chat.getLastSeen())
                            .isContact(chat.isContact())
                            .build();
                    chatViewModels.add(uvm);
                }
                chatViewModels.sort((u1, u2) -> {
                    try {
                        LocalDateTime t1 = (u1.getTime() != null && !u1.getTime().isEmpty()) ? LocalDateTime.parse(u1.getTime()) : LocalDateTime.MIN;
                        LocalDateTime t2 = (u2.getTime() != null && !u2.getTime().isEmpty()) ? LocalDateTime.parse(u2.getTime()) : LocalDateTime.MIN;
                        return t2.compareTo(t1);
                    } catch (Exception e) {
                        return 0;
                    }
                });
                Platform.runLater(() -> {
                    allChatUsers.setAll(chatViewModels);
                    filteredChatUsers.setAll(allChatUsers);
                });
            } else {
                System.err.println("Failed to load chats: " + response.getMessage());
            }
        });

        getChatsTask.setOnFailed(event -> getChatsTask.getException().printStackTrace());
        new Thread(getChatsTask).start();
    }


    /**
     * Sets up the chat list with a custom cell factory and selection listener.
     */
    private Task<RpcResponse<GetChatInfoOutputModel[]>> activeSearchTask = null;
    private Timer searchTimer;
    private TimerTask searchTask;
    private void setupChatList() {
        chatListView.setItems(filteredChatUsers);
        chatListView.setCellFactory(listView -> new UserCustomCell());

        // Handle chat selection
        chatListView.getSelectionModel().selectedItemProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser != null && newUser != currentSelectedChat) {
                // Ignore placeholder clicks
                if ("SEARCHING_PLACEHOLDER".equals(newUser.getChatId())) {
                    Platform.runLater(() -> chatListView.getSelectionModel().clearSelection());
                    return;
                }

                if (newUser.isFromPublicSearch()) {
                    handlePublicSearchResultSelection(newUser);
                } else {
                    selectChat(newUser);
                }
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
    private void setupMessageInputFormatting() {
        ContextMenu formattingMenu = new ContextMenu();

        MenuItem boldItem = new MenuItem("Bold");
        boldItem.setOnAction(e -> applyFormatting(BOLD_MARKER_PREFIX, BOLD_MARKER_SUFFIX));

        MenuItem italicItem = new MenuItem("Italic");
        italicItem.setOnAction(e -> applyFormatting(ITALIC_MARKER_PREFIX, ITALIC_MARKER_SUFFIX));

        MenuItem underlineItem = new MenuItem("Underline");
        underlineItem.setOnAction(e -> applyFormatting(UNDERLINE_MARKER_PREFIX, UNDERLINE_MARKER_SUFFIX));

        MenuItem spoilerItem = new MenuItem("Spoiler");
        spoilerItem.setOnAction(e -> applyFormatting(SPOILER_MARKER_PREFIX, SPOILER_MARKER_SUFFIX));

        formattingMenu.getItems().addAll(boldItem, italicItem, underlineItem, spoilerItem);

        // Show context menu only when text is selected
        formattingMenu.setOnShowing(e -> {
            boolean textSelected = messageInputField.getSelectedText() != null && !messageInputField.getSelectedText().isEmpty();
            boldItem.setDisable(!textSelected);
            italicItem.setDisable(!textSelected);
            underlineItem.setDisable(!textSelected);
            spoilerItem.setDisable(!textSelected);
        });

        messageInputField.setContextMenu(formattingMenu);
    }
    private void applyFormatting(String prefix, String suffix) {
        String selectedText = messageInputField.getSelectedText();
        if (selectedText == null || selectedText.isEmpty()) return;

        IndexRange selection = messageInputField.getSelection();
        String currentText = messageInputField.getText();

        // Check if the selection is already formatted
        boolean isFormatted = selection.getStart() >= prefix.length() &&
                selection.getEnd() <= currentText.length() - suffix.length() &&
                currentText.substring(selection.getStart() - prefix.length(), selection.getStart()).equals(prefix) &&
                currentText.substring(selection.getEnd(), selection.getEnd() + suffix.length()).equals(suffix);

        if (isFormatted) {
            // Un-format: remove the markup
            int newStart = selection.getStart() - prefix.length();
            int newEnd = selection.getEnd() + suffix.length();
            messageInputField.replaceText(newStart, newEnd, selectedText);
            // Reselect the text
            messageInputField.selectRange(newStart, newStart + selectedText.length());
        } else {
            // Format: wrap with markup
            String formattedText = prefix + selectedText + suffix;
            messageInputField.replaceSelection(formattedText);
            // Reselect the original text inside the markup
            messageInputField.selectRange(selection.getStart() + prefix.length(), selection.getEnd() + prefix.length());
        }
    }
    private Node createSpoilerNode(String content, boolean isOutgoing) {
        // The actual text content, which will be covered initially.
        Text revealedText = new Text(content);
        revealedText.getStyleClass().addAll("message-text", isOutgoing ? "outgoing" : "incoming");

        // The opaque overlay that hides the text.
        Region spoilerOverlay = new Region();
        spoilerOverlay.getStyleClass().add("spoiler-overlay");

        // A label to clearly mark the content as a spoiler.
        Label spoilerHintLabel = new Label("Spoiler");
        spoilerHintLabel.getStyleClass().add("spoiler-hint-label");
        spoilerHintLabel.setMouseTransparent(true); // Clicks should go through to the StackPane

        // StackPane layers the text, the overlay, and the hint label.
        StackPane stack = new StackPane(revealedText, spoilerOverlay, spoilerHintLabel);
        stack.getStyleClass().add("spoiler-wrapper");

        // When the stack is clicked, hide the overlay and the hint.
        stack.setOnMouseClicked(e -> {
            spoilerOverlay.setVisible(false);
            spoilerHintLabel.setVisible(false);
            e.consume(); // Prevent the click from triggering other actions (like double-click to reply)
        });

        // Ensure the overlay is exactly the size of the text it's covering.
        spoilerOverlay.prefWidthProperty().bind(revealedText.layoutBoundsProperty().map(bounds -> bounds.getWidth() + 4));
        spoilerOverlay.prefHeightProperty().bind(revealedText.layoutBoundsProperty().map(bounds -> bounds.getHeight()));

        return stack;
    }
    /**
     * Sets up event handlers for all interactive UI components.
     */
    private void setupEventHandlers() {
        // Sidebar buttons
        menuButton.setOnAction(e -> showSideBar());
        settingsButton.setOnAction(e -> openSettings());

        // Search functionality
        searchField.textProperty().addListener((obs, oldText, newText) -> debounceSearch(newText));

        // Header buttons

            headerAvatarImage.setOnMouseClicked(e -> {
                if (currentSelectedChat != null) {
                    showProfileDialog();
                }
            });
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
        DeleteContactBox.setOnMouseClicked(e -> handleDeleteContact());
        // Scroll listener for messages
        messagesScrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            updateScrollToBottomVisibility();
            handleScrollPositionChange(newVal.doubleValue());
        });
    }

    private void handleDeleteContact() {
        if (currentSelectedChat == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Message");
        alert.setHeaderText("Are you sure you want to delete this contact?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Task<RpcResponse<String>> deleteTask = chatService.removeContact(UUID.fromString(currentSelectedChat.getChatId()));

                deleteTask.setOnSucceeded(event -> {
                    RpcResponse<String> result = deleteTask.getValue();
                    if (result.getStatusCode() != StatusCode.OK) {
                        Platform.runLater(() -> showTemporaryNotification("Failed to delete contact: " + result.getMessage() + "\n"));
                    }else{
                        allChatUsers.stream()
                                .filter(user -> user.getChatId().equals(currentSelectedChat.getUserId()))
                                .findFirst()
                                .ifPresent(user -> {
                                    if(!user.getIsContact()) {
                                            user.setDisplayName(result.getPayload());
                                        if (currentSelectedChat != null && currentSelectedChat.getChatId().equals(user.getChatId())) {
                                            Platform.runLater(() -> {
                                                updateChatHeader(user);
                                                if (isRightPanelVisible) {
                                                    updateRightPanel(user);
                                                }
                                            });
                                        }
                                    }
                                });
                    }
                });
                deleteTask.setOnFailed(event -> {
                    deleteTask.getException().printStackTrace();
                    Platform.runLater(() -> showTemporaryNotification("Error deleting contact.\n"));
                });
                new Thread(deleteTask).start();
            }
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

        // Typing animation (re-enabled)
        typingAnimationTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(chatSubtitleLabel.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(500), new KeyValue(chatSubtitleLabel.opacityProperty(), 0.5)),
                new KeyFrame(Duration.millis(1000), new KeyValue(chatSubtitleLabel.opacityProperty(), 1.0))
        );
        typingAnimationTimeline.setCycleCount(Timeline.INDEFINITE);
        typingAnimationTimeline.setAutoReverse(true);
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
     * Selects a chat based on the provided ChatViewModel and updates the UI.
     *
     * @param user The ChatViewModel representing the selected chat.
     */
    private void selectChat(ChatViewModel user) {
        if (user == null) return;

        // *** ADD THIS BLOCK TO FIX THE TYPING INDICATOR ***
        // When switching chats, cancel any pending typing-stop task for the old chat.
        if (typingStopTask != null) {
            typingStopTask.cancel();
        }
        // If the user was marked as typing in the previous chat, send a "stop" event now.
        if (isCurrentlyTyping) {
            sendTypingStatus(false);
            isCurrentlyTyping = false;
        }
        // *** END OF ADDED BLOCK ***

        currentSelectedChat = user;

        // Update UI state
        hideWelcomeState();
        showChatArea();
        updateChatHeader(user);
        loadMessages(user);
        enableChatControls();

        // Clear notifications and mark chat as read
        if (user.hasUnreadMessages()) {
            user.clearUnreadCount();
            Task<Void> markAsReadTask = chatService.markChatAsRead(UUID.fromString(user.getChatId()));
            new Thread(markAsReadTask).start();
        }

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
     * @param user The ChatViewModel to update the header with.
     */
    private void updateChatHeader(ChatViewModel user) {
        // Update chat title
        chatTitleLabel.setText(user.getDisplayName());

        // Update subtitle based on user state
        if (user.getType() == UserType.GROUP || user.getType() == UserType.SUPERGROUP || user.getType() == UserType.CHANNEL) {
            // For groups/channels, the subtitle shows member/subscriber count, not status.
            // We handle this separately, so we clear the main subtitle label.
            chatSubtitleLabel.setText("");
            chatSubtitleLabel.setVisible(false);
            chatSubtitleLabel.setManaged(false);
        } else {
            // For regular users, show the online/last seen status.
            chatSubtitleLabel.setVisible(true);
            chatSubtitleLabel.setManaged(true);
            updateChatSubtitle(user);
        }
        // --------------------------------------------------

        // Update avatar
        updateHeaderAvatar(user);

        // Update badges and indicators
        mutedIcon.setVisible(user.isMuted());
        // Online indicator should only be visible for personal user chats
        onlineIndicator.setVisible(user.isOnline() && user.getType() == UserType.USER);

        // Update members count for groups
        if (user.getType() == UserType.GROUP || user.getType() == UserType.SUPERGROUP) {
            membersCountLabel.setText(user.getMembersCount() + " members");
            membersCountLabel.setVisible(true);
        } else {
            membersCountLabel.setVisible(false);
        }

        // Start online status animation if user is online
        if (user.isOnline() && user.getType() == UserType.USER && onlineStatusTimeline != null) {
            onlineStatusTimeline.play();
        }
    }

    /**
     * Updates the header avatar with the user's image or a default one.
     *
     * @param user The ChatViewModel to update the avatar for.
     */
    private void updateHeaderAvatar(ChatViewModel user) {
        loadDefaultHeaderAvatar(); // Set default avatar immediately

        // Assuming ChatViewModel has getAvatarId()
        String avatarId = user.getAvatarId();

        if (user.getType() == UserType.SAVED_MESSAGES){
            Image defaultSavedMessagesAvatar = new Image(Objects.requireNonNull(getClass().getResource("/Client/images/SavedMessagesProfile.png")).toExternalForm());
            headerAvatarImage.setImage(defaultSavedMessagesAvatar);
        }
        else if (avatarId != null && !avatarId.isEmpty()) {
            fileDownloadService.getFile(avatarId).thenAccept(file -> {
                Platform.runLater(() -> {
                    // Check if the current user is still the one we initiated this download for
                    if (currentSelectedChat != null && avatarId.equals(currentSelectedChat.getAvatarId())) {
                        try {
                            Image avatar = new Image(file.toURI().toString());
                            headerAvatarImage.setImage(avatar);
                        } catch (Exception e) {
                            System.err.println("Failed to load downloaded avatar: " + e.getMessage());
                            loadDefaultHeaderAvatar(); // Fallback to default on error
                        }
                    }
                });
            }).exceptionally(e -> {
                System.err.println("Failed to download avatar " + avatarId + ": " + e.getMessage());
                // The default avatar is already showing, so no UI action needed on failure.
                return null;
            });
        }
    }

    /**
     * Loads messages for the selected user based on their type.
     *
     * @param user The ChatViewModel to load messages for.
     */
    /**
     * Loads messages for the selected user, now with support for media messages.
     *
     * @param user The ChatViewModel to load messages for.
     */

    public void loadMessages(ChatViewModel user) {
        messagesContainer.getChildren().clear();
        if (user == null || user.getChatId() == null) {
            showEmptyChatState();
            return;
        }

        Task<RpcResponse<GetMessageOutputModel[]>> getMessagesTask = chatService.fetchMessagesForChat(UUID.fromString(user.getChatId()));

        getMessagesTask.setOnSucceeded(event -> {
            RpcResponse<GetMessageOutputModel[]> response = getMessagesTask.getValue();
            if (response.getStatusCode() == StatusCode.OK && response.getPayload() != null) {
                Platform.runLater(() -> {
                    messagesContainer.getChildren().clear();
                    if (response.getPayload().length == 0) {
                        showEmptyChatState();
                        return;
                    }
                    List<GetMessageOutputModel> sortedMessages = new ArrayList<>(Arrays.asList(response.getPayload()));
                    sortedMessages.sort(Comparator.comparing(msg -> LocalDateTime.parse(msg.getTimestamp())));
                    for (GetMessageOutputModel msg : sortedMessages) {
                        LocalDateTime timestamp = LocalDateTime.parse(msg.getTimestamp());
                        String formattedTime = timestamp.format(DateTimeFormatter.ofPattern("HH:mm"));
                        String senderName = msg.getOutgoing() ? null : msg.getSenderName();
                        String senderProfilePictureId = msg.getOutgoing() ? null : msg.getSenderProfilePictureId();
                        if (msg.getMessageType() == MessageType.TEXT && msg.getTextContent() != null) {
                            String status = msg.getOutgoing() ? msg.getMessageStatus() : "received";
                            HBox messageNode = addMessageBubble(msg.getTextContent(), msg.getOutgoing(), formattedTime, status, senderName, msg.isEdited(),
                                    msg.getRepliedToSenderName(), msg.getRepliedToMessageContent(), msg.getForwardedFromSenderName(),senderProfilePictureId);
                            // Store messageId and timestamp for later updates
                            messageNode.getChildren().getFirst().getProperties().put("messageId", msg.getMessageId());
                            messageNode.getChildren().getFirst().getProperties().put("messageTimestamp", timestamp);
                        } else if (msg.getMessageType() == MessageType.MEDIA && msg.getMediaId() != null) {
                            DocumentInfo docInfo = new DocumentInfo(msg);
                            docInfo.setSenderName(senderName);
                            String status = msg.getOutgoing() ? msg.getMessageStatus() : "received";
                            HBox messageNode = addDocumentMessageBubble(docInfo, msg.getOutgoing(), formattedTime, status,
                                    msg.getRepliedToSenderName(), msg.getRepliedToMessageContent(), msg.getForwardedFromSenderName(),senderProfilePictureId);
                            // Store messageId and timestamp for later updates
                            ((VBox) messageNode.getChildren().getFirst()).getProperties().put("messageId", msg.getMessageId());
                            ((VBox) messageNode.getChildren().getFirst()).getProperties().put("messageTimestamp", timestamp);
                        }
                    }
                    Platform.runLater(this::scrollToBottom);
                });
            } else {
                System.err.println("Failed to load messages: " + response.getMessage());
                Platform.runLater(this::showEmptyChatState);
            }
        });

        getMessagesTask.setOnFailed(event -> {
            getMessagesTask.getException().printStackTrace();
            Platform.runLater(this::showEmptyChatState);
        });

        new Thread(getMessagesTask).start();
    }

    /**
     * Creates and prepends the reply/forward header to a message bubble if applicable.
     *
     * @param bubble                  The VBox of the message bubble.
     * @param repliedToSenderName     The name of the original message's sender.
     * @param repliedToMessageContent The content of the original message.
     * @param forwardedFromName       The name of the user from whom the message was forwarded.
     */
    private void addReplyAndForwardHeaders(VBox bubble, String repliedToSenderName, String repliedToMessageContent, String forwardedFromName) {
        // --- Handle Forwarded Info ---
        if (forwardedFromName != null && !forwardedFromName.isEmpty()) {
            VBox forwardInfoBox = new VBox();
            forwardInfoBox.getStyleClass().add("forward-info-box");
            forwardInfoBox.setPadding(new Insets(0, 0, 4, 0)); // Add some spacing below

            Label forwardedFromLabel = new Label("Forwarded from " + forwardedFromName);
            forwardedFromLabel.getStyleClass().add("forward-label");


            forwardInfoBox.getChildren().addAll(forwardedFromLabel);
            // Add forwarded info at the very top of the bubble
            bubble.getChildren().add(0, forwardInfoBox);
        }

        // --- Handle Reply Info ---
        if (repliedToSenderName != null && repliedToMessageContent != null) {
            HBox replyInfoBox = new HBox(8); // HBox with spacing
            replyInfoBox.getStyleClass().add("reply-info-box");

            // This is the vertical colored bar
            Region replyBar = new Region();
            replyBar.getStyleClass().add("reply-bar-in-bubble");

            // VBox for the name and message preview
            VBox replyContent = new VBox(2); // VBox with small spacing
            HBox.setHgrow(replyContent, Priority.ALWAYS);

            Label replyToNameLabel = new Label(repliedToSenderName);
            replyToNameLabel.getStyleClass().add("reply-to-name-label");

            // Use the existing utility to create a formatted preview of the replied message
            TextFlow replyMessagePreview = createFormattedTextFlowForPreview(repliedToMessageContent);
            replyMessagePreview.getStyleClass().add("reply-message-preview");
            // Limit the height of the preview to a few lines
            replyMessagePreview.setMaxHeight(40);

            replyContent.getChildren().addAll(replyToNameLabel, replyMessagePreview);
            replyInfoBox.getChildren().addAll(replyBar, replyContent);

            // Add a click handler to jump to the replied message (future feature)
            replyInfoBox.setOnMouseClicked(event -> {
                System.out.println("Jump to replied message clicked.");
                event.consume(); // Prevent click from bubbling up to the main bubble
            });

            // Add reply info at the top (but after forward info if it exists)
            int insertionIndex = bubble.getChildren().stream().anyMatch(n -> n.getStyleClass().contains("forward-info-box")) ? 1 : 0;
            bubble.getChildren().add(insertionIndex, replyInfoBox);
        }
    }

//    private void uploadFileAndSendMessage(File file, DocumentInfo docInfo) {
//        IProgressListener listener = (transferred, total) -> {
//            double progress = (total > 0) ? ((double) transferred / total) * 100 : 0;
//            System.out.printf("Upload Progress: %.2f%%\\n", progress);
//            // TODO: Update UI with progress indicator on the message bubble
//        };
//
//        var app = connectionManager.getClient();
//        ExecutorService backgroundExecutor = app.getBackgroundExecutor();
//        backgroundExecutor.submit(() -> {
//            try {
//                // Step 1: Initiate upload to get FileId. This is a custom protocol message, not RPC.
//                FileInfoModel info = app.getFileTransferManager().initiateUpload(file);
//                String fileId = info.FileId;
//
//                // Step 2: Create the Media DB entry via RPC.
//                CreateMediaInputModel createMediaInput = new CreateMediaInputModel(
//                        UUID.fromString(fileId),
//                        file.getName(),
//                        file.length(),
//                        getFileExtension(file)
//                );
//                RpcResponse<UUID> createMediaResponse = rpcCaller.createMediaEntry(createMediaInput);
//
//                if (createMediaResponse.getStatusCode() != StatusCode.OK) {
//                    System.err.println("Failed to create media entry on server: " + createMediaResponse.getMessage());
//                    Platform.runLater(() -> showTemporaryNotification("Error preparing upload.\n"));
//                    return; // Abort upload
//                }
//
//                // Step 3: Start the actual upload task
//                UploadTask uploadTask = new UploadTask(app.getFileTransferManager(), info, file, listener);
//                app.registerTask(fileId, uploadTask);
//
//                uploadTask.setOnSucceeded(e -> {
//                    app.unregisterTask(fileId);
//                    System.out.println("Upload successful. Media ID: " + fileId);
//
//                    // Step 4: Send the message pointing to the Media ID
//                    SendMessageInputModel messageInput = new SendMessageInputModel();
//                    messageInput.setChatId(UUID.fromString(currentSelectedUser.getUserId()));
//                    messageInput.setMessageType(MessageType.MEDIA);
//                    messageInput.setMediaId(createMediaResponse.getPayload());
//
//                    Task<RpcResponse<SendMessageOutputModel>> sendMessageTask = chatService.sendMessage(messageInput);
//                    sendMessageTask.setOnSucceeded(event -> {
//                        if (sendMessageTask.getValue().getStatusCode() == StatusCode.OK) {
//                            System.out.println("Media message sent successfully.");
//                            HBox messageNode = addDocumentMessageBubble(docInfo, true, getCurrentTime(), "delivered");
//                            // Store the actual messageId and timestamp from server for later event updates
//                            ((VBox) messageNode.getChildren().getFirst()).getProperties().put("messageId", sendMessageTask.getValue().getPayload().getMessageId());
//                            ((VBox) messageNode.getChildren().getFirst()).getProperties().put("messageTimestamp", LocalDateTime.parse(sendMessageTask.getValue().getPayload().getTimestamp()));
//
//                        } else {
//                            Platform.runLater(() -> showTemporaryNotification("Failed to send file message.\n"));
//                        }
//                    });
//                    sendMessageTask.setOnFailed(failEvent -> {
//                        sendMessageTask.getException().printStackTrace();
//                        Platform.runLater(() -> showTemporaryNotification("Error sending file message.\n"));
//                    });
//                    new Thread(sendMessageTask).start();
//                });
//
//                uploadTask.setOnFailed(failEvent -> {
//                    app.unregisterTask(fileId);
//                    uploadTask.getException().printStackTrace();
//                    Platform.runLater(() -> showTemporaryNotification("File upload failed.\n"));
//                });
//
//                backgroundExecutor.submit(uploadTask);
//
//            } catch (Exception ex) {
//                System.err.println("Error initiating file upload or creating media entry: " + ex.getMessage());
//                ex.printStackTrace();
//                Platform.runLater(() -> showTemporaryNotification("Error starting upload.\n"));
//            }
//        });
//        }

    /**
     * Checks if the current logged-in user is an admin in the currently selected chat.
     *
     * @return true if the user is an admin, false otherwise.
     */
    boolean isCurrentUserAdmin() {
        if (currentSelectedChat == null) {
            return false;
        }
        // TODO:Server
        return "ADMIN".equalsIgnoreCase(currentSelectedChat.getUserRole());
    }

    /**
     * Opens the profile dialog for the currently selected user, group, or channel.
     * The dialog's content and title are adjusted based on the chat type.
     */
    private void showProfileDialog() {
        if (currentSelectedChat == null) return;
        try {
            String dialogTitle;
            switch (currentSelectedChat.getType()) {
                case GROUP:
                    dialogTitle = "Group Info";
                    break;
                case CHANNEL:
                case SUPERGROUP:
                    dialogTitle = "Channel Info";
                    break;
                case SAVED_MESSAGES:
                    return;
                default:
                    dialogTitle = "Profile Info";
                    break;
            }

            Stage parentStage = (Stage) mainChatContainer.getScene().getWindow();
            Stage dialogStage = SceneUtil.createDialog(
                    "/Client/fxml/profileSection.fxml",
                    parentStage,
                    this,
                    currentSelectedChat,
                    dialogTitle
            );
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            dialogStage.show();

        } catch (IOException e) {
            System.err.println("Failed to load the profile section dialog.");
            e.printStackTrace();
        }
    }

    // TODO
    /**
     * Checks if the provided ChatViewModel represents the currently logged-in user's profile.
     * This requires knowing the logged-in user's ID.
     * @param user The ChatViewModel to check.
     * @return true if it's the current user's own profile.
     */
//    public boolean isMyOwnProfile(ChatViewModel user) {
//        if (user == null || connectionManager.getClient().getUserIdentity() == null) {
//            return false;
//        }
//        return user.getUserId().equals(connectionManager.getClient().getUserIdentity().getUserId().toString());
//    }
//
//    /**
//     * Provides access to the ChatService for other controllers.
//     * @return The instance of ChatService.
//     */
//    public ChatService getChatService() {
//        return this.chatService;
//    }

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
    private HBox addMessageBubble(String text, boolean isOutgoing, String time, String status, String senderName, boolean isEdited) {
        // Keep old signature for compatibility, delegate to the new one.
        return addMessageBubble(text, isOutgoing, time, status, senderName, isEdited, null, null, null);
    }
    private HBox addMessageBubble(String text, boolean isOutgoing, String time, String status, String senderName, boolean isEdited,  String repliedToSenderName, String repliedToMessageContent, String forwardedFromName) {
        // Keep old signature for compatibility, delegate to the new one.
        return addMessageBubble(text, isOutgoing, time, status, senderName, isEdited, repliedToSenderName, repliedToMessageContent, forwardedFromName);
    }
    private HBox addMessageBubble(String text, boolean isOutgoing, String time, String status, String senderName, boolean isEdited,
                                  String repliedToSenderName, String repliedToMessageContent, String forwardedFromName , String senderProfilePcitureId) {
        HBox messageContainer = new HBox();
        messageContainer.setSpacing(12);
        messageContainer.setPadding(new Insets(4, 0, 4, 0));
        messageContainer.getProperties().put("raw_text", text); // Store raw text on container for easier access


        if (isOutgoing) {
            messageContainer.setAlignment(Pos.CENTER_RIGHT);
            VBox bubble = createMessageBubble(text, time, status, true, null, isEdited,
                    repliedToSenderName, repliedToMessageContent, forwardedFromName);
            messageContainer.getChildren().add(bubble);
        } else {
            messageContainer.setAlignment(Pos.CENTER_LEFT);

            if (currentSelectedChat != null &&
                    (currentSelectedChat.getType() == UserType.GROUP || currentSelectedChat.getType() == UserType.SUPERGROUP)) {
                ImageView senderAvatar = createSenderAvatar(senderProfilePcitureId);
                messageContainer.getChildren().add(senderAvatar);
            }

            VBox bubble = createMessageBubble(text, time, status, false, senderName, isEdited,
                    repliedToSenderName, repliedToMessageContent, forwardedFromName);
            messageContainer.getChildren().add(bubble);
        }

        messagesContainer.getChildren().add(messageContainer);
        TelegramCellUtils.animateNewMessage(messageContainer);

        Platform.runLater(() -> Platform.runLater(this::scrollToBottom));

        return messageContainer;
    }

    private VBox createMessageBubble(String text, String time, String status, boolean isOutgoing, String senderName, boolean isEdited,
                                     String repliedToSenderName, String repliedToMessageContent, String forwardedFromName) {
        VBox bubble = new VBox();
        bubble.setSpacing(4);
        bubble.getStyleClass().addAll("message-bubble", isOutgoing ? "outgoing" : "incoming");
        bubble.setMaxWidth(420);
        bubble.getProperties().put("raw_text", text);

        addReplyAndForwardHeaders(bubble, repliedToSenderName, repliedToMessageContent, forwardedFromName);

        // Add sender name for incoming group messages
        if (!isOutgoing && senderName != null && currentSelectedChat != null &&
                (currentSelectedChat.getType() == UserType.GROUP || currentSelectedChat.getType() == UserType.SUPERGROUP)) {
            Label senderLabel = new Label(senderName);
            senderLabel.getStyleClass().add("sender-name");
            senderLabel.setMouseTransparent(true);
            bubble.getChildren().add(senderLabel);
        }

        TextFlow messageTextFlow = createFormattedTextFlow(text, isOutgoing);
        messageTextFlow.getStyleClass().add("message-text-flow");

        HBox timeContainer = new HBox(4);
        timeContainer.setAlignment(Pos.CENTER_RIGHT);
        timeContainer.setMouseTransparent(true);

        if (isEdited) {
            Label editedLabel = new Label("edited");
            editedLabel.getStyleClass().add("edited-indicator");
            timeContainer.getChildren().add(editedLabel);
        }

        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().addAll("message-time", isOutgoing ? "outgoing" : "incoming");
        timeContainer.getChildren().add(timeLabel);

        if (isOutgoing && status != null) {
            String imagePath = switch (status.toLowerCase()) {
                case "sending" -> "/Client/images/status/sending.png"; // Clock icon
                case "sent", "delivered" -> "/Client/images/status/sent.png";       // One tick
                case "read" -> "/Client/images/status/read.png";       // Blue double tick
                default -> null;
            };

            if (imagePath != null) {
                try {
                    ImageView statusIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResource(imagePath)).toExternalForm()));
                    statusIcon.setFitHeight(16);
                    statusIcon.setPreserveRatio(true);
                    statusIcon.getStyleClass().add("status-icon-image");

                    timeContainer.getChildren().add(statusIcon);
                } catch (Exception e) {
                    System.err.println("Could not load status icon: " + imagePath);
                }
            }
        }

        bubble.getChildren().addAll(messageTextFlow, timeContainer);
        bubble.setOnMouseClicked(this::handleMessageClick);

        return bubble;
    }


    public static TextFlow createFormattedTextFlowForPreview(String text) {
        TextFlow textFlow = new TextFlow();
        if (text == null || text.trim().isEmpty()) {
            textFlow.getChildren().add(new Text(""));
            return textFlow;
        }

        Pattern pattern = Pattern.compile("(\\*\\*.*?\\*\\*)|(__.*?__)|(\\+\\+.*?\\+\\+)|(\\|\\|.*?\\|\\|)|(@[\\w_]+)");
        Matcher matcher = pattern.matcher(text);

        int lastMatchEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastMatchEnd) {
                textFlow.getChildren().add(new Text(text.substring(lastMatchEnd, matcher.start())));
            }

            String match = matcher.group();
            if (match.startsWith(BOLD_MARKER_PREFIX) && match.endsWith(BOLD_MARKER_SUFFIX)) {
                Text formattedText = new Text(match.substring(2, match.length() - 2));
                formattedText.getStyleClass().add("text-bold");
                textFlow.getChildren().add(formattedText);
            } else if (match.startsWith(ITALIC_MARKER_PREFIX) && match.endsWith(ITALIC_MARKER_SUFFIX)) {
                Text formattedText = new Text(match.substring(2, match.length() - 2));
                formattedText.getStyleClass().add("text-italic");
                textFlow.getChildren().add(formattedText);
            } else if (match.startsWith(UNDERLINE_MARKER_PREFIX) && match.endsWith(UNDERLINE_MARKER_SUFFIX)) {
                Text formattedText = new Text(match.substring(2, match.length() - 2));
                formattedText.setUnderline(true);
                textFlow.getChildren().add(formattedText);
            } else if (match.startsWith(SPOILER_MARKER_PREFIX) && match.endsWith(SPOILER_MARKER_SUFFIX)) {
                Text spoilerText = new Text("[Spoiler]");
                spoilerText.getStyleClass().add("text-italic");
                textFlow.getChildren().add(spoilerText);
            } else {
                textFlow.getChildren().add(new Text(match));
            }
            lastMatchEnd = matcher.end();
        }

        if (lastMatchEnd < text.length()) {
            textFlow.getChildren().add(new Text(text.substring(lastMatchEnd)));
        }

        if (textFlow.getChildren().isEmpty()) {
            textFlow.getChildren().add(new Text(text));
        }
        return textFlow;
    }
    // RENAME this method from createTextFlowWithMentions to createFormattedTextFlow and UPDATE its logic
    private TextFlow createFormattedTextFlow(String text, boolean isOutgoing) {
        TextFlow textFlow = new TextFlow();
        // Regex to find all formatting types: bold, italic, underline, spoiler, and mentions.
        Pattern pattern = Pattern.compile("(\\*\\*.*?\\*\\*)|(__.*?__)|(\\+\\+.*?\\+\\+)|(\\|\\|.*?\\|\\|)|(@[\\w_]+)");
        Matcher matcher = pattern.matcher(text);

        int lastMatchEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastMatchEnd) {
                addPlainText(textFlow, text.substring(lastMatchEnd, matcher.start()), isOutgoing);
            }

            String match = matcher.group();
            if (match.startsWith(BOLD_MARKER_PREFIX) && match.endsWith(BOLD_MARKER_SUFFIX)) {
                Text formattedText = new Text(match.substring(2, match.length() - 2));
                formattedText.getStyleClass().addAll("message-text", "text-bold", isOutgoing ? "outgoing" : "incoming");

                formattedText.setMouseTransparent(true);

                textFlow.getChildren().add(formattedText);
            } else if (match.startsWith(ITALIC_MARKER_PREFIX) && match.endsWith(ITALIC_MARKER_SUFFIX)) {
                Text formattedText = new Text(match.substring(2, match.length() - 2));
                formattedText.getStyleClass().addAll("message-text", "text-italic", isOutgoing ? "outgoing" : "incoming");

                formattedText.setMouseTransparent(true);

                textFlow.getChildren().add(formattedText);
            } else if (match.startsWith(UNDERLINE_MARKER_PREFIX) && match.endsWith(UNDERLINE_MARKER_SUFFIX)) {
                Text formattedText = new Text(match.substring(2, match.length() - 2));
                formattedText.setUnderline(true);
                formattedText.getStyleClass().addAll("message-text", isOutgoing ? "outgoing" : "incoming");

                formattedText.setMouseTransparent(true);

                textFlow.getChildren().add(formattedText);
            } else if (match.startsWith(SPOILER_MARKER_PREFIX) && match.endsWith(SPOILER_MARKER_SUFFIX)) {
                // Spoiler node should NOT be transparent, so it can receive clicks.
                Node spoilerNode = createSpoilerNode(match.substring(2, match.length() - 2), isOutgoing);
                textFlow.getChildren().add(spoilerNode);
            } else if (match.startsWith("@")) {
                // Hyperlink should NOT be transparent, so it can be clicked.
                String username = match.substring(1);
                Hyperlink mentionLink = new Hyperlink(match);
                mentionLink.setOnAction(e -> handleMentionClick(username));
                mentionLink.getStyleClass().addAll("mention-hyperlink", isOutgoing ? "outgoing" : "incoming");
                textFlow.getChildren().add(mentionLink);
            }

            lastMatchEnd = matcher.end();
        }

        if (lastMatchEnd < text.length()) {
            addPlainText(textFlow, text.substring(lastMatchEnd), isOutgoing);
        }

        if (textFlow.getChildren().isEmpty()) {
            addPlainText(textFlow, text, isOutgoing);
        }

        return textFlow;
    }
    private void addPlainText(TextFlow textFlow, String content, boolean isOutgoing) {
        Text plainText = new Text(content);
        plainText.getStyleClass().addAll("message-text", isOutgoing ? "outgoing" : "incoming");
        plainText.setMouseTransparent(true);
        textFlow.getChildren().add(plainText);
    }
    private TextFlow createTextFlowWithMentions(String text, boolean isOutgoing) {
        TextFlow textFlow = new TextFlow();
        // Regex to find @username patterns. It matches @ followed by word characters.
        Pattern pattern = Pattern.compile("(@[\\w_]+)");
        Matcher matcher = pattern.matcher(text);

        int lastMatchEnd = 0;
        while (matcher.find()) {
            // Add the plain text part before the mention
            if (matcher.start() > lastMatchEnd) {
                Text plainText = new Text(text.substring(lastMatchEnd, matcher.start()));
                // Apply the original style classes to the Text node itself
                plainText.getStyleClass().addAll("message-text", isOutgoing ? "outgoing" : "incoming");
                textFlow.getChildren().add(plainText);
            }

            // Add the mention as a clickable hyperlink
            String mention = matcher.group(1);
            String username = mention.substring(1); // remove '@'
            Hyperlink mentionLink = new Hyperlink(mention);
            mentionLink.setOnAction(e -> handleMentionClick(username));
            mentionLink.getStyleClass().add("mention-hyperlink");
            mentionLink.getStyleClass().add(isOutgoing ? "outgoing" : "incoming");
            textFlow.getChildren().add(mentionLink);

            lastMatchEnd = matcher.end();
        }

        // Add any remaining text after the last mention
        if (lastMatchEnd < text.length()) {
            Text remainingText = new Text(text.substring(lastMatchEnd));
            // Apply the original style classes to the Text node itself
            remainingText.getStyleClass().addAll("message-text", isOutgoing ? "outgoing" : "incoming");
            textFlow.getChildren().add(remainingText);
        }

        // If no mentions were found, just add the full text as a single styled Text node.
        if (textFlow.getChildren().isEmpty()) {
            Text fullText = new Text(text);
            fullText.getStyleClass().addAll("message-text", isOutgoing ? "outgoing" : "incoming");
            textFlow.getChildren().add(fullText);
        }

        return textFlow;
    }
    private void handleMentionClick(String username) {
        Task<RpcResponse<GetChatInfoOutputModel>> findChatTask = chatService.findChatByUsername(username);

        findChatTask.setOnSucceeded(event -> {
            RpcResponse<GetChatInfoOutputModel> response = findChatTask.getValue();
            if (response.getStatusCode() == StatusCode.OK && response.getPayload() != null) {
                GetChatInfoOutputModel chatInfo = response.getPayload();
                Platform.runLater(() -> {
                    // Check if this user is already in the local chat list
                    Optional<ChatViewModel> existingUser = allChatUsers.stream()
                            .filter(uvm -> uvm.getChatId().equals(chatInfo.getChatId().toString()))
                            .findFirst();

                    ChatViewModel userToSelect;
                    if (existingUser.isPresent()) {
                        userToSelect = existingUser.get();
                    } else {
                        // If not present, create a new ChatViewModel and add it to the list
                        ChatViewModel uvm = new ChatViewModelBuilder()
                                .chatId(chatInfo.getChatId().toString())
                                .avatarId(chatInfo.getProfilePictureId())
                                .displayName(chatInfo.getTitle())
                                .lastMessage(chatInfo.getLastMessage())
                                .time(chatInfo.getLastMessageTimestamp())
                                .type(chatInfo.getType())
                                .notificationsNumber(String.valueOf(chatInfo.getUnreadCount()))
                                .build();
                        allChatUsers.add(0, uvm); // Add to the top of the master list
                        performSearch(searchField.getText()); // Re-apply current filter
                        userToSelect = uvm;
                    }

                    // Select the user in the ListView, which will open the chat
                    chatListView.getSelectionModel().select(userToSelect);
                    chatListView.scrollTo(userToSelect);
                });
            } else {
                Platform.runLater(() -> showTemporaryNotification("User @" + username + " not found.\n"));
                System.err.println("Failed to find user by username: " + response.getMessage());
            }
        });

        findChatTask.setOnFailed(event -> {
            findChatTask.getException().printStackTrace();
            Platform.runLater(() -> showTemporaryNotification("Error finding user.\n"));
        });

        new Thread(findChatTask).start();
    }

    private ImageView createSenderAvatar(String avatarId) {
        ImageView avatar = new ImageView();
        avatar.setFitWidth(32);
        avatar.setFitHeight(32);
        avatar.setPreserveRatio(true);

        // Create circular clip
        Circle clip = new Circle(16, 16, 16);
        avatar.setClip(clip);

        // Load default or sender-specific avatar
        try {
            if (avatarId != null && !avatarId.isBlank()) {
                fileDownloadService.getImage(avatarId).thenAccept(image -> {
                    if (image != null) {
                        Platform.runLater(() -> avatar.setImage(image));
                    } else {
                        loadSenderDefaultProfilePicture(avatar);
                    }
                }).exceptionally(e -> {
                    e.printStackTrace();
                    loadSenderDefaultProfilePicture(avatar);
                    return null;
                });
            } else {
                loadSenderDefaultProfilePicture(avatar);
            }
        } catch (Exception e) {
            System.err.println("Failed to load sender avatar for: " + avatarId);
        }

        avatar.getStyleClass().add("sender-avatar");
        return avatar;
    }
    private void loadSenderDefaultProfilePicture(ImageView avatar) {
        try {
            Image profileImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Client/images/11Devs-white.png")));
            Platform.runLater(() -> avatar.setImage(profileImage));
        } catch (Exception e) {
            System.err.println("Could not load default profile picture: " + e.getMessage());
        }
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
     * Displays a reply preview. It resets any prior editing state and configures
     * the preview panel for a reply action.
     *
     * @param messageBubble The VBox representing the message to reply to.
     */
    private void showReplyPreview(VBox messageBubble) {
        if (replyPreviewContainer == null || messageBubble == null) return;

        // 1. Reset any previous state (edit or reply)
        resetReplyEditState();
        if (replyPreviewAnimation != null) replyPreviewAnimation.stop();

        // 2. Get the message ID
        UUID messageId = (UUID) messageBubble.getProperties().get("messageId");
        if (messageId == null) {
            System.err.println("Cannot reply: Message ID not found on bubble.");
            return;
        }

        // 3. Determine the name of the person being replied to
        boolean isOwnMessage = messageBubble.getStyleClass().contains("outgoing");
        String replyToName = isOwnMessage ? this.ownUsername : (currentSelectedChat != null ? currentSelectedChat.getDisplayName() : "User");

        // 4. Get the raw text of the original message
        String originalMessageText = (String) messageBubble.getProperties().get("raw_text");
        if (originalMessageText == null) {
            // Fallback for document bubbles which store info in UserData
            if (messageBubble.getUserData() instanceof DocumentInfo) {
                originalMessageText = ((DocumentInfo) messageBubble.getUserData()).getFileName();
            } else {
                System.err.println("Could not find 'raw_text' property on message bubble for reply.");
                return;
            }
        }

        // 5. Store the active reply information
        activeReplyInfo = new ReplyInfo(messageId, replyToName, originalMessageText);


        // First, create a clean, unformatted version for the preview label
        String cleanPreviewText = TextUtil.stripFormattingForCopying(originalMessageText);

        // Then, truncate it if it's too long
        String truncatedText = cleanPreviewText.length() > 45 ?
                cleanPreviewText.substring(0, 42) + "..." :
                cleanPreviewText;

        // Update the UI labels
        replyToLabel.setText("Reply to " + replyToName);
        replyMessageLabel.setText(truncatedText); // Use the clean and truncated text
        messageInputField.clear(); // Ensure input field is empty for the new reply

        // 7. Show the preview panel with animation if it's hidden
        if (!replyPreviewContainer.isVisible()) {
            replyPreviewContainer.setVisible(true);
            replyPreviewContainer.setManaged(true);
            replyPreviewContainer.setTranslateY(-30);
            replyPreviewContainer.setOpacity(0);
            TranslateTransition slideDown = new TranslateTransition(Duration.millis(200), replyPreviewContainer);
            slideDown.setToY(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), replyPreviewContainer);
            fadeIn.setToValue(1.0);
            replyPreviewAnimation = new ParallelTransition(slideDown, fadeIn);
            replyPreviewAnimation.play();
        }

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
            // Hide the panel
            replyPreviewContainer.setVisible(false);
            replyPreviewContainer.setManaged(false);

            // Reset the state data AND clear the input field
            resetReplyEditState();
            messageInputField.clear();
        });
        replyPreviewAnimation.play();
    }

    /**
     * Resets only the data and state flags for reply/edit.
     * Does NOT hide the UI container, allowing for smooth transitions between states.
     */
    private void resetReplyEditState() {
        isEditing = false;
        editingMessageBubble = null;
        activeReplyInfo = null;
        replyToLabel.setText("");
        replyMessageLabel.setText("");
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
    /**
     * Processes the selected document and adds it to the chat
     */
    private void processDocumentAttachment(File file) {
        if (currentSelectedChat == null) return;

        DocumentInfo docInfo = new DocumentInfo(
                file.getName(),
                file.length(),
                getFileExtension(file),
                file.getAbsolutePath()
        );

        String tempId = UUID.randomUUID().toString();
        String repliedToSenderName = (activeReplyInfo != null) ? activeReplyInfo.senderName : null;
        String repliedToMessageContent = (activeReplyInfo != null) ? activeReplyInfo.content : null;

        HBox messageNode = addDocumentMessageBubble(docInfo, true, getCurrentTime(), "sending", repliedToSenderName, repliedToMessageContent, null);
        VBox bubble = (VBox) messageNode.getChildren().getFirst();
        temporaryMessageNodes.put(tempId, messageNode);

        ProgressIndicator progressIndicator = (ProgressIndicator) bubble.getProperties().get("progressIndicator");

        var app = connectionManager.getClient();
        ExecutorService backgroundExecutor = app.getBackgroundExecutor();

        backgroundExecutor.submit(() -> {
            try {
                FileInfoModel info = app.getFileTransferManager().initiateUpload(file);
                String fileId = info.FileId;

                CreateMediaInputModel createMediaInput = new CreateMediaInputModel(
                        UUID.fromString(fileId),
                        file.getName(),
                        file.length(),
                        getFileExtension(file)
                );
                RpcResponse<UUID> createMediaResponse = rpcCaller.createMediaEntry(createMediaInput);

                if (createMediaResponse.getStatusCode() != StatusCode.OK) {
                    Platform.runLater(() -> updateMessageStatus(temporaryMessageNodes.remove(tempId), "failed", null));
                    return;
                }
                UUID mediaId = createMediaResponse.getPayload();

                IProgressListener listener = (transferred, total) -> {
                    double progress = (total > 0) ? ((double) transferred / total) : 0;
                    Platform.runLater(() -> {
                        if (progressIndicator != null) progressIndicator.setProgress(progress);
                    });
                };

                UploadTask uploadTask = new UploadTask(app.getFileTransferManager(), info, file, listener);
                app.registerTask(fileId, uploadTask);

                uploadTask.setOnSucceeded(e -> {
                    app.unregisterTask(fileId);

                    SendMessageInputModel messageInput = new SendMessageInputModel();
                    messageInput.setChatId(UUID.fromString(currentSelectedChat.getChatId()));
                    messageInput.setMessageType(MessageType.MEDIA);
                    messageInput.setMediaId(mediaId);

                    if (activeReplyInfo != null) {
                        messageInput.setRepliedToMessageId(activeReplyInfo.messageId);
                    }
                    closeReplyPreview();

                    Task<RpcResponse<SendMessageOutputModel>> sendMessageTask = chatService.sendMessage(messageInput);
                    sendMessageTask.setOnSucceeded(event -> {
                        RpcResponse<SendMessageOutputModel> smResponse = sendMessageTask.getValue();
                        HBox finalMessageNode = temporaryMessageNodes.remove(tempId);
                        if (finalMessageNode != null) {
                            if (smResponse.getStatusCode() == StatusCode.OK) {
                                VBox finalBubble = (VBox) finalMessageNode.getChildren().getFirst();
                                Platform.runLater(() -> {
                                    updateMessageStatus(finalMessageNode, "sent", LocalDateTime.parse(smResponse.getPayload().getTimestamp()).format(DateTimeFormatter.ofPattern("HH:mm")));
                                    finalBubble.getProperties().put("messageId", smResponse.getPayload().getMessageId());
                                    finalBubble.getProperties().put("messageTimestamp", LocalDateTime.parse(smResponse.getPayload().getTimestamp()));
                                    finalizeDocumentBubbleDisplay(finalBubble);

                                    currentSelectedChat.setLastMessage(docInfo.getFileName());
                                    currentSelectedChat.setTime(smResponse.getPayload().getTimestamp());
                                    currentSelectedChat.setMessageStatus("sent");
                                    sortAndRefreshChatList();
                                });
                            } else {
                                Platform.runLater(() -> updateMessageStatus(finalMessageNode, "failed", null));
                            }
                        }
                    });
                    sendMessageTask.setOnFailed(failEvent -> Platform.runLater(() -> updateMessageStatus(temporaryMessageNodes.remove(tempId), "failed", null)));
                    new Thread(sendMessageTask).start();
                });

                uploadTask.setOnFailed(failEvent -> {
                    app.unregisterTask(fileId);
                    Platform.runLater(() -> updateMessageStatus(temporaryMessageNodes.remove(tempId), "failed", null));
                });

                backgroundExecutor.submit(uploadTask);

            } catch (Exception ex) {
                Platform.runLater(() -> updateMessageStatus(temporaryMessageNodes.remove(tempId), "failed", null));
                ex.printStackTrace();
            }
        });
    }
    /**
     * Creates the document bubble UI with a dynamic action icon for downloading or opening,
     * and a progress indicator for uploads/downloads.
     */
    private VBox createDocumentBubble(DocumentInfo docInfo, String time, String status, boolean isOutgoing, String repliedToSenderName, String repliedToMessageContent, String forwardedFromName) {
        VBox bubble = new VBox();
        bubble.setSpacing(8);
        bubble.getStyleClass().addAll("message-bubble", "document-bubble", isOutgoing ? "outgoing" : "incoming");
        bubble.setMaxWidth(300);
        bubble.setPadding(new Insets(8));

        addReplyAndForwardHeaders(bubble, repliedToSenderName, repliedToMessageContent, forwardedFromName);

        HBox docContainer = new HBox(12);
        docContainer.setAlignment(Pos.CENTER_LEFT);

        StackPane iconStack = new StackPane();
        iconStack.setAlignment(Pos.CENTER);
        iconStack.setPrefSize(48, 48);

        Region iconBackground = new Region();
        iconBackground.getStyleClass().addAll("document-icon-background", docInfo.getFileExtension().toLowerCase());
        iconBackground.setPrefSize(48, 48);

        ImageView fileIcon = createFileTypeIcon(docInfo.getFileExtension());
        fileIcon.setFitWidth(45);
        fileIcon.setFitHeight(45);
        fileIcon.setPreserveRatio(true);

        ProgressIndicator progressIndicator = new ProgressIndicator(0);
        progressIndicator.setVisible(false);
        progressIndicator.setPrefSize(52, 52);
        progressIndicator.getStyleClass().add("download-progress-indicator");

        Button actionButton = new Button();
        actionButton.getStyleClass().add("document-action-icon");

        iconStack.getChildren().addAll(iconBackground, fileIcon, progressIndicator, actionButton);

        VBox fileInfo = new VBox(4);
        HBox.setHgrow(fileInfo, Priority.ALWAYS);
        Label fileName = new Label(docInfo.getFileName());
        fileName.getStyleClass().addAll("document-name", isOutgoing ? "outgoing" : "incoming");
        fileName.setWrapText(true);
        Label fileDetails = new Label(formatFileSize(docInfo.getFileSize()) + " • " + docInfo.getFileExtension().toUpperCase());
        fileDetails.getStyleClass().addAll("document-details", isOutgoing ? "outgoing" : "incoming");
        fileInfo.getChildren().addAll(fileName, fileDetails);

        bubble.getProperties().put("progressIndicator", progressIndicator);
        bubble.getProperties().put("actionButton", actionButton);
        bubble.getProperties().put("fileIcon", fileIcon);
        bubble.getProperties().put("iconBackground", iconBackground);

        boolean isFileDownloaded;
        if (docInfo.getStoredPath() != null && !docInfo.getStoredPath().isEmpty()) {
            File localFile = new File(docInfo.getStoredPath());
            isFileDownloaded = localFile.exists() && docInfo.getFileSize() == localFile.length();
        } else {
            isFileDownloaded = false;
        }
        // =================================================================================

        if (isOutgoing && "sending".equals(status)) {
            progressIndicator.setVisible(true);
            actionButton.setVisible(false);
            fileIcon.setVisible(false);
            iconBackground.setVisible(false);
        } else if (isFileDownloaded) {
            progressIndicator.setVisible(false);
            actionButton.setVisible(false);
            fileIcon.setVisible(true);
            iconBackground.setVisible(true);
        } else {
            progressIndicator.setVisible(false);
            actionButton.setVisible(true);
            actionButton.setGraphic(createIconView("/Client/images/context-menu/download.png", 24));
            actionButton.setOnAction(e -> startDownload(docInfo, progressIndicator, actionButton, bubble));

            fileIcon.setVisible(false);
            iconBackground.setVisible(false);
        }

        docContainer.getChildren().addAll(iconStack, fileInfo);

        HBox timeContainer = new HBox(4);
        timeContainer.setAlignment(Pos.CENTER_RIGHT);
        timeContainer.setPadding(new Insets(4, 0, 0, 0));
        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().addAll("message-time", isOutgoing ? "outgoing" : "incoming");
        timeContainer.getChildren().add(timeLabel);

        if (isOutgoing && status != null) {
            String imagePath = switch (status.toLowerCase()) {
                case "sending" -> "/Client/images/status/sending.png"; // Clock icon
                case "sent", "delivered" -> "/Client/images/status/sent.png"; // One tick
                case "read" -> "/Client/images/status/read.png"; // Blue double tick
                default -> null;
            };

            if (imagePath != null) {
                try {
                    ImageView statusIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResource(imagePath)).toExternalForm()));
                    statusIcon.setFitHeight(16);
                    statusIcon.setPreserveRatio(true);
                    statusIcon.getStyleClass().add("status-icon-image");

                    timeContainer.getChildren().add(statusIcon);
                } catch (Exception e) {
                    System.err.println("Could not load status icon: " + imagePath);
                }
            }
        }

        bubble.getChildren().addAll(docContainer, timeContainer);

        bubble.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                showMessageContextMenu(event);
            } else if (event.getButton() == MouseButton.PRIMARY) {
                if (docInfo.getStoredPath() != null && !docInfo.getStoredPath().isEmpty()) {
                    File currentFileState = new File(docInfo.getStoredPath());
                    if (currentFileState.exists()) {
                        openDocument(docInfo);
                    }
                }
            }
            event.consume();
        });

        bubble.setUserData(docInfo);
        return bubble;
    }

    /**
     * Helper method to safely create an ImageView for an icon, with a fallback.
     */
    private ImageView createIconView(String resourcePath, int size) {
        ImageView iconView = new ImageView();
        try {
            URL resource = getClass().getResource(resourcePath);
            if (resource == null) {
                throw new IOException("Icon resource not found: " + resourcePath);
            }
            Image iconImage = new Image(resource.toExternalForm());
            iconView.setImage(iconImage);
        } catch (Exception e) {
            System.err.println("Failed to load icon: " + resourcePath + ". Using placeholder. Error: " + e.getMessage());
            try { // Fallback to a known existing icon
                Image fallbackImage = new Image(Objects.requireNonNull(getClass().getResource("/Client/images/context-menu/forward.png")).toExternalForm());
                iconView.setImage(fallbackImage);
            } catch (Exception ex) { /* If even fallback fails, icon will be empty */ }
        }
        iconView.setFitHeight(size);
        iconView.setFitWidth(size);
        iconView.setPreserveRatio(true);
        return iconView;
    }

    /**
     * Updates a document bubble's UI to its final state (after upload or download).
     * It hides progress/action layers and shows the final file type icon.
     *
     * @param bubble The VBox of the document message bubble.
     */
    private void finalizeDocumentBubbleDisplay(VBox bubble) {
        if (bubble == null) return;

        Platform.runLater(() -> {
            // Retrieve UI components from the bubble's properties
            ProgressIndicator progressIndicator = (ProgressIndicator) bubble.getProperties().get("progressIndicator");
            Button actionButton = (Button) bubble.getProperties().get("actionButton");
            ImageView fileIcon = (ImageView) bubble.getProperties().get("fileIcon");
            Region iconBackground = (Region) bubble.getProperties().get("iconBackground");

            // Hide overlays
            if (progressIndicator != null) progressIndicator.setVisible(false);
            if (actionButton != null) actionButton.setVisible(false);

            // Show the final icon and its background
            if (fileIcon != null) fileIcon.setVisible(true);
            if (iconBackground != null) iconBackground.setVisible(true);
        });
    }

    /**
     * Starts the download for a given media file and updates the UI accordingly.
     */
    private void startDownload(DocumentInfo docInfo, ProgressIndicator progressIndicator, Button actionButton, VBox bubble) {
        String fileId = docInfo.getFileId();
        if (fileId == null || activeDownloadTasks.containsKey(fileId)) return;

        actionButton.setGraphic(createIconView("/Client/images/context-menu/delete.png", 24));
        actionButton.setOnAction(e -> cancelDownload(fileId));
        progressIndicator.setProgress(0);
        progressIndicator.setVisible(true);

        actionButton.setGraphic(null);


        var app = connectionManager.getClient();
        var transferManager = app.getFileTransferManager();
        var executor = app.getBackgroundExecutor();

        executor.submit(() -> {
            try {
                String destinationPath = fileDownloadService.getDocumentCacheDir().toString();
                TransferInfo info = transferManager.initiateDownload(fileId, destinationPath);
                if (info == null) {
                    Platform.runLater(() -> resetDownloadUI(progressIndicator, actionButton, docInfo, bubble, "Failed to get file info."));
                    return;
                }
                docInfo.setStoredPath(new File(info.getDestinationPath(), info.getFileName() + "." + info.getFileExtension()).getPath());

                IProgressListener listener = (transferred, total) -> {
                    double progress = (total > 0) ? ((double) transferred / total) : 0;
                    Platform.runLater(() -> progressIndicator.setProgress(progress));
                };

                DownloadTask downloadTask = new DownloadTask(transferManager, info, null, fileId, listener);

                downloadTask.setOnSucceeded(e -> {
                    activeDownloadTasks.remove(fileId);
                    // =========================== REFACTORING ===========================
                    finalizeDocumentBubbleDisplay(bubble);
                    // ===================================================================
                });;

                downloadTask.setOnFailed(e -> Platform.runLater(() -> resetDownloadUI(progressIndicator, actionButton, docInfo, bubble, "Download failed.")));
                downloadTask.setOnCancelled(e -> Platform.runLater(() -> resetDownloadUI(progressIndicator, actionButton, docInfo, bubble, "Download cancelled.")));

                activeDownloadTasks.put(fileId, downloadTask);
                executor.submit(downloadTask);

            } catch (IOException e) {
                Platform.runLater(() -> resetDownloadUI(progressIndicator, actionButton, docInfo, bubble, "Error starting download."));
                e.printStackTrace();
            }
        });
    }
    /**
     * Creates a document message bubble with file info and controls
     */
    private HBox addDocumentMessageBubble(DocumentInfo docInfo, boolean isOutgoing,
                                          String time, String status, String repliedToSenderName, String repliedToMessageContent, String forwardedFromName) {
    return addDocumentMessageBubble(docInfo,isOutgoing,time,  status,  repliedToSenderName,  repliedToMessageContent, forwardedFromName,null);
    }
    private HBox addDocumentMessageBubble(DocumentInfo docInfo, boolean isOutgoing,
                                          String time, String status, String repliedToSenderName, String repliedToMessageContent, String forwardedFromName, String senderProfilePictureId) {
        HBox messageContainer = new HBox();
        messageContainer.setSpacing(12);
        messageContainer.setPadding(new Insets(4, 0, 4, 0));

        if (isOutgoing) {
            messageContainer.setAlignment(Pos.CENTER_RIGHT);
            VBox bubble = createDocumentBubble(docInfo, time, status, true, repliedToSenderName, repliedToMessageContent, forwardedFromName);
            messageContainer.getChildren().add(bubble);
        } else {
            messageContainer.setAlignment(Pos.CENTER_LEFT);

            // Add sender avatar for group chats
            if (currentSelectedChat != null &&
                    (currentSelectedChat.getType() == UserType.GROUP ||
                            currentSelectedChat.getType() == UserType.SUPERGROUP)) {
                ImageView senderAvatar = createSenderAvatar(senderProfilePictureId);
                messageContainer.getChildren().add(senderAvatar);
            }

            VBox bubble = createDocumentBubble(docInfo, time, status, false, repliedToSenderName, repliedToMessageContent, forwardedFromName);
            messageContainer.getChildren().add(bubble);
        }

        messagesContainer.getChildren().add(messageContainer);
        TelegramCellUtils.animateNewMessage(messageContainer);

        Platform.runLater(this::scrollToBottom);

        return messageContainer;
    }


    private void resetDownloadUI(ProgressIndicator progressIndicator, Button actionButton, DocumentInfo docInfo, VBox bubble, String notificationMessage) {
        activeDownloadTasks.remove(docInfo.getFileId());
        Platform.runLater(() -> {
            progressIndicator.setVisible(false);
            actionButton.setGraphic(createIconView("/Client/images/context-menu/download.png", 24));
            actionButton.setOnAction(evt -> startDownload(docInfo, progressIndicator, actionButton, bubble));
            if (notificationMessage != null) showTemporaryNotification(notificationMessage);
        });
    }

    /**
     * Cancels an active download task.
     */
    private void cancelDownload(String fileId) {
        DownloadTask task = activeDownloadTasks.get(fileId);
        if (task != null) {
            task.cancel(true);
        }
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

    private boolean isVideoFile(String extension) {
        if (extension == null) return false;
        return switch (extension.toLowerCase()) {
            case "mp4", "avi", "mkv", "mov", "flv" -> true;
            default -> false;
        };
    }

    /**
     * Loads the video player FXML and displays it in a new stage.
     * @param videoFile The video file to play.
     */
    private void openVideoPlayer(File videoFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client/fxml/videoPlayerDialog.fxml"));
            VideoPlayerController controller = new VideoPlayerController();
            loader.setController(controller);

            Parent root = loader.load();
            controller.setVideoFile(videoFile);

            Stage videoStage = new Stage();
            videoStage.initModality(Modality.APPLICATION_MODAL);
            videoStage.initStyle(StageStyle.DECORATED);
            videoStage.setTitle(videoFile.getName());
            videoStage.setScene(new Scene(root));
            videoStage.setOnCloseRequest(event -> controller.cleanup());

            videoStage.show();
        } catch (IOException e) {
            System.err.println("Failed to open video player: " + e.getMessage());
            e.printStackTrace();
            showTemporaryNotification("Error opening video player.\n");
        }
    }
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

                showTemporaryNotification("Document saved to " + saveLocation.getName() + "\n");

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
    private void debounceSearch(String searchText) {
        if (searchTask != null) {
            searchTask.cancel(); // Cancel the previously scheduled task
        }
        searchTask = new TimerTask() {
            @Override
            public void run() {
                // The actual search logic must run on the JavaFX Application Thread
                Platform.runLater(() -> performSearch(searchText));
            }
        };
        // Schedule the new task to run after a 300ms delay of inactivity
        searchTimer.schedule(searchTask, 300);
    }
    /**
     * Performs a search on the chat list based on the input text.
     * If the search text is empty, it resets the list to show all chats.
     *
     * @param searchText The text to search for.
     */
    private void performSearch(String searchText) {
        // Cancel any previously running search
        if (activeSearchTask != null && activeSearchTask.isRunning()) {
            activeSearchTask.cancel(true);
        }

        // If the search text is empty, show all user chats and cancel any further action.
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredChatUsers.setAll(allChatUsers);
            chatListView.scrollTo(0);
            return;
        }

        String trimmedSearchText = searchText.trim();
        String lowerCaseSearch = trimmedSearchText.toLowerCase();

        // 1. Always perform and display local search results immediately
        List<ChatViewModel> localResults = allChatUsers.stream()
                .filter(user -> user.getDisplayName().toLowerCase().contains(lowerCaseSearch) ||
                        (user.getLastMessage() != null && user.getLastMessage().toLowerCase().contains(lowerCaseSearch)))
                .collect(Collectors.toList());

        // 2. Check if the search text is long enough for a public search
        if (trimmedSearchText.length() < MIN_PUBLIC_SEARCH_LENGTH) {
            // If not, just show the local results and stop.
            filteredChatUsers.setAll(localResults);
            return;
        }

        // 3. If long enough, proceed with public search
        // Add "Searching..." placeholder to the local results
        ChatViewModel searchingPlaceholder = new ChatViewModelBuilder().chatId("SEARCHING_PLACEHOLDER").build();
        ObservableList<ChatViewModel> searchResults = FXCollections.observableArrayList(localResults);
        searchResults.add(searchingPlaceholder);
        filteredChatUsers.setAll(searchResults);

        // Start the server search task
        activeSearchTask = chatService.searchPublic(trimmedSearchText);

        activeSearchTask.setOnSucceeded(event -> {
            RpcResponse<GetChatInfoOutputModel[]> response = activeSearchTask.getValue();
            if (response.getStatusCode() == StatusCode.OK && response.getPayload() != null) {
                Set<String> localResultChatIds = localResults.stream()
                        .map(ChatViewModel::getChatId)
                        .collect(Collectors.toSet());

                List<ChatViewModel> publicResults = new ArrayList<>();
                for (GetChatInfoOutputModel chat : response.getPayload()) {
                    String resultId = chat.getChatId().toString();

                    boolean isChatType = !UserType.fromString(chat.getType()).equals(UserType.USER);

                    // Filter out groups/channels that are already in the local results
                    if (isChatType && localResultChatIds.contains(resultId)) {
                        continue;
                    }

                    ChatViewModel uvm = new ChatViewModelBuilder()
                            .chatId(resultId)
                            .avatarId(chat.getProfilePictureId())
                            .displayName(chat.getTitle())
                            .username(chat.getUsername()) // Store username for user results
                            .subtitle(chat.getLastMessage()) // This will be @username or member count
                            .type(chat.getType())
                            .isFromPublicSearch(true) // Mark as a public result
                            .isOnline(chat.isOnline())
                            .lastSeen(chat.getLastSeen())
                            .build();
                    publicResults.add(uvm);
                }

                Platform.runLater(() -> {
                    searchResults.remove(searchingPlaceholder);
                    searchResults.addAll(publicResults);
                    filteredChatUsers.setAll(searchResults);
                });
            } else {
                Platform.runLater(() -> searchResults.remove(searchingPlaceholder));
                System.err.println("Public search failed: " + response.getMessage());
            }
        });

        activeSearchTask.setOnFailed(event -> {
            if (!activeSearchTask.isCancelled()) {
                Platform.runLater(() -> searchResults.remove(searchingPlaceholder));
                if(activeSearchTask.getException() != null)
                    activeSearchTask.getException().printStackTrace();
            }
        });

        new Thread(activeSearchTask).start();
    }
    private void handlePublicSearchResultSelection(ChatViewModel publicResult) {
        // A public search result was clicked. We need to find or create the chat.
        if (publicResult.getType() == UserType.USER) {
            String username = publicResult.getUsername();
            if (username != null && !username.isEmpty()) {
                // This will find an existing private chat or create a new one,
                // add the ChatViewModel to the list, and select it.
                handleMentionClick(username);
            } else {
                showTemporaryNotification("Cannot open chat: user information is missing.");
            }
        } else {
            // This is a public group or channel. The ID is the Chat ID.
            Optional<ChatViewModel> existingChat = allChatUsers.stream()
                    .filter(u -> u.getChatId().equals(publicResult.getChatId()))
                    .findFirst();

            ChatViewModel chatToSelect;
            if (existingChat.isPresent()) {
                chatToSelect = existingChat.get();
            } else {
                // It's a new chat for us. Add it to our master list.
                publicResult.setFromPublicSearch(false); // It's a local chat now
                allChatUsers.add(0, publicResult);
                chatToSelect = publicResult;
            }

            // Reset the search field, which will repopulate the list with all chats,
            // then select the new/existing chat.
            searchField.clear(); // This will trigger performSearch("")
            Platform.runLater(() -> {
                chatListView.getSelectionModel().select(chatToSelect);
                chatListView.scrollTo(chatToSelect);
            });
        }
    }

    // ============ MESSAGE INPUT HANDLING ============

    /**
     * Handles key press events in the message input field.
     * TODO: Implement full keyboard functionality.
     *
     * @param event The KeyEvent triggering the action.
     */
    private void handleKeyPressed(KeyEvent event) {
        // Formatting Shortcuts
        if (event.isControlDown()) {
            switch (event.getCode()) {
                case B:
                    applyFormatting(BOLD_MARKER_PREFIX, BOLD_MARKER_SUFFIX);
                    event.consume();
                    return;
                case I:
                    applyFormatting(ITALIC_MARKER_PREFIX, ITALIC_MARKER_SUFFIX);
                    event.consume();
                    return;
                case U:
                    applyFormatting(UNDERLINE_MARKER_PREFIX, UNDERLINE_MARKER_SUFFIX);
                    event.consume();
                    return;
                case UP:
                    editLastMessage();
                    event.consume();
                    return;
            }
        }
        if (event.isControlDown() && event.isShiftDown() && event.getCode() == KeyCode.P) {
            applyFormatting(SPOILER_MARKER_PREFIX, SPOILER_MARKER_SUFFIX);
            event.consume();
            return;
        }

        // Other shortcuts and actions
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
        if (editingMessageBubble != null) {
            if (!text.isEmpty() && !text.equals(originalEditText)) {
                UUID messageId = (UUID) editingMessageBubble.getProperties().get("messageId");
                if (messageId != null) {
                    editMessage(messageId, text);
                }
            }
            closeReplyPreview();
        }
        else if (!text.isEmpty()) {
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
        if (text.isEmpty() || currentSelectedChat == null) return;

        if (emptyChatStateContainer.isVisible()) {
            messagesContainer.getChildren().clear();
            showChatArea();
        }

        if (isEditing && editingMessageBubble != null) {
            UUID messageId = (UUID) editingMessageBubble.getProperties().get("messageId");
            editMessage(messageId, text);
            closeReplyPreview();
        } else {
            HBox messageNode = addMessageBubble(text, true, getCurrentTime(), "sending", null, false,
                    (activeReplyInfo != null) ? activeReplyInfo.senderName : null,
                    (activeReplyInfo != null) ? activeReplyInfo.content : null,
                    null);

            SendMessageInputModel input = new SendMessageInputModel();
            input.setChatId(UUID.fromString(currentSelectedChat.getChatId()));
            input.setTextContent(text);
            input.setMessageType(MessageType.TEXT);
            if (activeReplyInfo != null) {
                input.setRepliedToMessageId(activeReplyInfo.messageId);
            }

            closeReplyPreview();

            Task<RpcResponse<SendMessageOutputModel>> sendMessageTask = chatService.sendMessage(input);

            sendMessageTask.setOnSucceeded(event -> {
                RpcResponse<SendMessageOutputModel> response = sendMessageTask.getValue();
                if (response.getStatusCode() == StatusCode.OK) {
                    LocalDateTime serverTimestamp = LocalDateTime.parse(response.getPayload().getTimestamp());
                    String formattedTime = serverTimestamp.format(DateTimeFormatter.ofPattern("HH:mm"));

                    Platform.runLater(() -> {
                        updateMessageStatus(messageNode, "sent", formattedTime);
                        VBox bubble = (VBox) messageNode.getChildren().getFirst();
                        bubble.getProperties().put("messageId", response.getPayload().getMessageId());
                        bubble.getProperties().put("messageTimestamp", serverTimestamp);

                        currentSelectedChat.setLastMessage(text);
                        currentSelectedChat.setTime(response.getPayload().getTimestamp());
                        currentSelectedChat.setMessageStatus("sent");
                        sortAndRefreshChatList();
                    });
                } else {
                    Platform.runLater(() -> {
                        updateMessageStatus(messageNode, "failed", null);
                        showTemporaryNotification("Failed to send message: " + response.getMessage() + "\n");
                    });
                }
            });

            sendMessageTask.setOnFailed(event -> {
                Platform.runLater(() -> {
                    updateMessageStatus(messageNode, "failed", null);
                    showTemporaryNotification("Error sending message.\n");
                });
                sendMessageTask.getException().printStackTrace();
            });

            new Thread(sendMessageTask).start();
        }

        messageInputField.clear();
        updateSendButtonState();
        Platform.runLater(() -> {
            messageInputField.requestFocus();
            scrollToBottom();
        });
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
    private void editMessage(UUID messageId, String newContent) {
        Task<RpcResponse<Object>> editTask = chatService.editMessage(messageId, newContent);

        editTask.setOnSucceeded(event -> {
            RpcResponse<Object> response = editTask.getValue();
            if (response.getStatusCode() != StatusCode.OK) {
                Platform.runLater(() -> showTemporaryNotification("Failed to edit message: " + response.getMessage() + "\n"));
            }
        });
        editTask.setOnFailed(event -> {
            editTask.getException().printStackTrace();
            Platform.runLater(() -> showTemporaryNotification("Error editing message.\n"));
        });
        new Thread(editTask).start();
    }

    /**
     * Updates the content of an existing message bubble after editing.
     *
     * @param bubble   The VBox of the message to update.
     * @param newText  The new text for the message.
     */
    private void updateMessage(VBox bubble, String newText) {
        if (bubble == null) return;

        boolean isOutgoing = bubble.getStyleClass().contains("outgoing");

        // 1. Find and replace the TextFlow content
        Optional<Node> oldTextFlowOpt = bubble.getChildren().stream()
                .filter(node -> node.getStyleClass().contains("message-text-flow"))
                .findFirst();

        if (oldTextFlowOpt.isPresent()) {
            int index = bubble.getChildren().indexOf(oldTextFlowOpt.get());
            bubble.getChildren().remove(index);

            TextFlow newTextFlow = createFormattedTextFlow(newText, isOutgoing);
            newTextFlow.getStyleClass().add("message-text-flow");
            bubble.getChildren().add(index, newTextFlow);
        }

        // 2. Update the raw_text property
        bubble.getProperties().put("raw_text", newText);

        // 3. Find time container and add "edited" label if it doesn't exist
        HBox timeContainer = (HBox) bubble.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .findFirst().orElse(null);

        if (timeContainer == null) return;

        boolean hasEditedLabel = timeContainer.getChildren().stream()
                .anyMatch(node -> node.getStyleClass().contains("edited-label"));

        if (!hasEditedLabel) {
            Label editedLabel = new Label("edited");
            editedLabel.getStyleClass().addAll("message-time", "edited-label", isOutgoing ? "outgoing" : "incoming");
            // Insert before the time label
            timeContainer.getChildren().add(0, editedLabel);
        }

        // 4. Update the timestamp
        Optional<Label> timeLabelOpt = timeContainer.getChildren().stream()
                .filter(node -> node instanceof Label && !node.getStyleClass().contains("edited-label") && !node.getStyleClass().contains("message-status"))
                .map(node -> (Label) node)
                .findFirst();

        timeLabelOpt.ifPresent(label -> label.setText(getCurrentTime()));
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
        if (currentSelectedChat == null) return;

        // If not already marked as typing, send the "is typing" status immediately
        if (!isCurrentlyTyping) {
            isCurrentlyTyping = true;
            sendTypingStatus(true);
        }

        // Cancel the previously scheduled "stop typing" task
        if (typingStopTask != null) {
            typingStopTask.cancel();
        }

        // Create and schedule a new task to send "stopped typing" after 2 seconds of inactivity
        typingStopTask = new TimerTask() {
            @Override
            public void run() {
                // This check is important in case the state changes elsewhere
                if (isCurrentlyTyping) {
                    isCurrentlyTyping = false;
                    sendTypingStatus(false);
                }
            }
        };
        typingTimer.schedule(typingStopTask, 1000); // 2-second delay
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
     * Enables or disables all chat-related controls based on whether a chat is selected
     * and what the user's permissions are within that chat.
     */
// A conceptual rewrite of enableChatControls()
    private void enableChatControls() {
        boolean canSendMessage = currentSelectedChat != null && (currentSelectedChat.getType() != UserType.CHANNEL || isCurrentUserAdmin());

        messageInputContainer.setVisible(canSendMessage);
        messageInputContainer.setManaged(canSendMessage);

        channelMuteToggleButton.setVisible(!canSendMessage);
        channelMuteToggleButton.setManaged(!canSendMessage);

        messageInputField.setDisable(!canSendMessage);
        sendButton.setDisable(!canSendMessage);
        attachmentButton.setDisable(!canSendMessage);

        if (!canSendMessage && currentSelectedChat != null) {
            updateChannelMuteButtonText();
        }

        callButton.setDisable(currentSelectedChat == null);
        videoCallButton.setDisable(currentSelectedChat == null);
        searchInChatButton.setDisable(currentSelectedChat == null);
        moreOptionsButton.setDisable(currentSelectedChat == null);
    }

//    private void enableChatControls() {
//        if (currentSelectedUser == null) {
//            disableChatControls(); // Should not happen but good practice
//            return;
//        }
//
//        boolean isChannel = currentSelectedUser.getType() == UserType.CHANNEL;
//        Node inputBar = messageInputField.getParent();
//
//        if (isChannel) {
//            // --- Channel-Specific Logic ---
//            String role = currentSelectedUser.getUserMembershipType(); // e.g., "OWNER", "ADMIN", "MEMBER"
//
//            // Default channel state: no calls, input disabled
//            callButton.setDisable(true);
//            videoCallButton.setDisable(true);
//
//            boolean canPost = "OWNER".equals(role) || "ADMIN".equals(role);
//
//            // Show/hide the entire input bar based on posting rights
//            if (inputBar != null) {
//                inputBar.setVisible(canPost);
//                inputBar.setManaged(canPost);
//            }
//            messageInputField.setDisable(!canPost);
//            sendButton.setDisable(!canPost);
//            attachmentButton.setDisable(!canPost);
//
//        } else {
//            // --- Existing Logic for Groups and Private Chats ---
//            if (inputBar != null) {
//                inputBar.setVisible(true);
//                inputBar.setManaged(true);
//            }
//            messageInputField.setDisable(false);
//            sendButton.setDisable(false);
//            attachmentButton.setDisable(false);
//
//            boolean isUser = currentSelectedUser.getType() == UserType.USER;
//            callButton.setDisable(!isUser);
//            videoCallButton.setDisable(!isUser);
//        }
//
//        // These are generally always enabled when a chat is selected
//        searchInChatButton.setDisable(false);
//        moreOptionsButton.setDisable(false);
//    }

    /**
     * Disables all chat controls when no chat is selected (welcome state).
     * This method is now simpler and delegates most logic to enableChatControls.
     */
    private void disableChatControls() {
        // FIX: When no chat is selected, both containers must be hidden.
        messageInputContainer.setVisible(false);
        messageInputContainer.setManaged(false);

        channelMuteToggleButton.setVisible(false);
        channelMuteToggleButton.setManaged(false);

        // Also disable the individual components for safety
        messageInputField.setDisable(true);
        sendButton.setDisable(true);
        attachmentButton.setDisable(true);

        // Disable header buttons
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
        if (currentSelectedChat == null) return;

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
        if (rightPanel == null || currentSelectedChat == null || splitPane.getItems().contains(rightPanel)) {
            return;
        }

        updateRightPanel(currentSelectedChat);
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
     * @param user The ChatViewModel to update the panel with.
     */
    private void updateRightPanel(ChatViewModel user) {
        if (user == null) return;

        // Update profile info
        profileNameLabel.setText(user.getDisplayName());
        if(user.getIsContact()){
            DeleteContactBox.setVisible(true);
        }else{
            DeleteContactBox.setVisible(false);
        }
        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            profileUsernameLabel.setText("@" + user.getUsername());
            profileUsernameLabel.setVisible(true);
        } else {
            profileUsernameLabel.setVisible(false);
        }

        if (user.getType() == UserType.GROUP || user.getType() == UserType.SUPERGROUP || user.getType() == UserType.CHANNEL) {
            // Hide the status label as it's not applicable
            profileStatusLabel.setVisible(false);
            profileStatusLabel.setManaged(false);
        } else {
            // For regular users, show the status
            profileStatusLabel.setVisible(true);
            profileStatusLabel.setManaged(true);
            if (user.isOnline()) {
                profileStatusLabel.setText("online");
            } else {
                profileStatusLabel.setText(TextUtil.formatLastSeen(user.getLastSeen()));
            }
        }
        // ------------------------------------------------------

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
        notificationsToggle.setSelected(!user.isMuted());
        updateNotificationToggle(!user.isMuted());
    }

    /**
     * Updates the profile avatar with the user's image or a default one.
     *
     * @param user The ChatViewModel to update the avatar for.
     */
    private void updateProfileAvatar(ChatViewModel user) {
        if (profileAvatarImage == null) return;
        loadDefaultProfileAvatar(); // Set default avatar immediately

        // Assuming ChatViewModel has getAvatarId()
        String avatarId = user.getAvatarId();

        if (avatarId != null && !avatarId.isEmpty()) {
            fileDownloadService.getFile(avatarId).thenAccept(file -> {
                Platform.runLater(() -> {
                    // Check if the right panel is still visible and for the same user
                    if (isRightPanelVisible && currentSelectedChat != null && avatarId.equals(currentSelectedChat.getAvatarId())) {
                        try {
                            Image avatar = new Image(file.toURI().toString());
                            profileAvatarImage.setImage(avatar);
                        } catch (Exception e) {
                            System.err.println("Failed to load downloaded profile avatar: " + e.getMessage());
                            loadDefaultProfileAvatar();
                        }
                    }
                });
            }).exceptionally(e -> {
                System.err.println("Failed to download profile avatar " + avatarId + ": " + e.getMessage());
                return null;
            });
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

    public void showTypingIndicator(String userName) {
        if (chatSubtitleLabel == null) return;

        // Ensure subtitle is set to typing and style is applied
        chatSubtitleLabel.setText(userName + " is typing...");
        chatSubtitleLabel.getStyleClass().setAll("chat-subtitle", "typing-indicator");

        if (!isTypingIndicatorVisible) {
            isTypingIndicatorVisible = true;
            if (typingAnimationTimeline != null) {
                typingAnimationTimeline.play();
            }
        }
    }

    private void updateChatSubtitle(ChatViewModel user) {
        // FIX for Bug 2 (Part 1): Add a guard. This method should NOT run if the
        // typing indicator is active. The `hideTypingIndicator` method will call
        // this when it's time to restore the normal subtitle.
        if (isTypingIndicatorVisible) {
            return;
        }
        if (user.getType() == UserType.SAVED_MESSAGES) {
            chatSubtitleLabel.setText("");
            chatSubtitleLabel.getStyleClass().setAll("chat-subtitle", "online");
        }
        else if (user.isOnline() && user.getType() == UserType.USER) {
            chatSubtitleLabel.setText("online");
            chatSubtitleLabel.getStyleClass().setAll("chat-subtitle", "online");
        } else if (user.getLastSeen() != null && !user.getLastSeen().isEmpty() && user.getType() == UserType.USER) {
            chatSubtitleLabel.setText(TextUtil.formatLastSeen(user.getLastSeen()));
            chatSubtitleLabel.getStyleClass().setAll("chat-subtitle");
        } else if (user.getType() == UserType.GROUP || user.getType() == UserType.SUPERGROUP) {
            chatSubtitleLabel.setText("");
            chatSubtitleLabel.getStyleClass().setAll("chat-subtitle");
        } else {
            chatSubtitleLabel.setText("offline");
            chatSubtitleLabel.getStyleClass().setAll("chat-subtitle");
        }
    }

    public void hideTypingIndicator() {
        if (chatSubtitleLabel == null || !isTypingIndicatorVisible) return;

        isTypingIndicatorVisible = false;
        if (typingAnimationTimeline != null) {
            typingAnimationTimeline.stop();
            chatSubtitleLabel.setOpacity(1.0); // Reset opacity
        }

        // FIX for Bug 2 (Part 2): Restore the original subtitle after stopping the animation.
        // This call is now safe because the new updateChatSubtitle has a guard.
        if (currentSelectedChat != null) {
            updateChatSubtitle(currentSelectedChat);
        } else {
            chatSubtitleLabel.setText("");
            chatSubtitleLabel.getStyleClass().setAll("chat-subtitle");
        }
    }

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
     * Shows the sidebar using the SidebarUtil utility.
     */
    private void showSideBar() {
        Stage parentStage = (Stage) menuButton.getScene().getWindow();
        if (parentStage != null) {
            SidebarUtil.showSidebarDialog(parentStage, "/Client/fxml/sidebarMenu.fxml", this);
        } else {
            System.err.println("Could not find parent stage to show sidebar.");
        }
    }

    /**
     * Initiates a voice call with the current selected user.
     */
    private void startVoiceCall() {
        if (currentSelectedChat == null) return;
        showTemporaryNotification("This feature will be implemented in the future.\n");
    }

    /**
     * Initiates a video call with the current selected user.
     */
    private void startVideoCall() {
        if (currentSelectedChat == null) return;
        showTemporaryNotification("This feature will be implemented in the future.\n");
    }

    /**
     * Shows a context menu with options for the current chat, complete with icons.
     * The menu is positioned correctly relative to the 'moreOptionsButton'.
     */
    private void showMoreOptions() {
        if (activeMessageContextMenu != null && activeMessageContextMenu.isShowing()) {
            activeMessageContextMenu.hide();
        }

        ContextMenu menu = new ContextMenu();
        menu.setAutoHide(true);

        MenuItem viewProfileItem = createIconMenuItem("View Profile", "/Client/images/context-menu/profile.png");
        viewProfileItem.setOnAction(e -> toggleRightPanel());

        MenuItem searchItem = createIconMenuItem("Search Messages", "/Client/images/context-menu/search.png");
        searchItem.setOnAction(e -> showSearchInChat());

        String muteText = (currentSelectedChat != null && currentSelectedChat.isMuted()) ? "Unmute" : "Mute";
        String muteIcon = (currentSelectedChat != null && currentSelectedChat.isMuted()) ? "/Client/images/context-menu/unmute.png" : "/Client/images/context-menu/mute.png";
        MenuItem muteItem = createIconMenuItem(muteText, muteIcon);
        muteItem.setOnAction(e -> toggleMute());

        String pinText = (currentSelectedChat != null && currentSelectedChat.isPinned()) ? "Unpin" : "Pin";
        String pinIcon = (currentSelectedChat != null && currentSelectedChat.isPinned()) ? "/Client/images/context-menu/unpin.png" : "/Client/images/context-menu/pin.png";
        MenuItem pinItem = createIconMenuItem(pinText, pinIcon);
        pinItem.setOnAction(e -> togglePin());

        MenuItem clearHistoryItem = createIconMenuItem("Clear History", "/Client/images/context-menu/clear.png");
        clearHistoryItem.setOnAction(e -> clearChatHistory());

        MenuItem blockUserItem = createIconMenuItem("Block User", "/Client/images/context-menu/block.png");
        blockUserItem.setOnAction(e -> blockUser());

        menu.getItems().addAll(
                viewProfileItem,
                searchItem,
                muteItem,
                pinItem,
                clearHistoryItem,
                blockUserItem
        );

        Point2D buttonBottomRight = new Point2D(moreOptionsButton.getWidth(), moreOptionsButton.getHeight());
        Point2D screenBottomRight = moreOptionsButton.localToScreen(buttonBottomRight);

        menu.show(moreOptionsButton, -10000, -10000);
        double menuWidth = menu.getWidth();
        menu.hide();

        double xOffset = 15;
        double yOffset = -5;

        menu.show(
                moreOptionsButton,
                (screenBottomRight.getX() - menuWidth) + xOffset,
                screenBottomRight.getY() + yOffset
        );

        activeMessageContextMenu = menu;
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
            case "sending" -> "🕒"; // Clock icon for sending
            case "sent" -> "✓";
            case "delivered" -> "✓✓";
            case "read" -> "✓✓"; // A different color would be better for 'read' state
            case "failed" -> "!"; // Exclamation mark for failed
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
    void refreshChatList() {
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
        } else if (currentSelectedChat != null) {
            goBackToWelcomeState();
        }
    }

    // ============ PLACEHOLDER METHODS ============

    /**
     * Returns to the welcome state, clearing the current selection.
     */
    private void goBackToWelcomeState() {
        currentSelectedChat = null;
        chatListView.getSelectionModel().clearSelection();
        showWelcomeState();
        // TODO: Implement hiding typing indicator if implemented.
        hideTypingIndicator(); // TODO UI
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
     * Opens the settings dialog
     */
    private void openSettings() {
        System.out.println("Opening settings");
        Stage parentStage = (Stage) mainChatContainer.getScene().getWindow();
        try {
            ColorAdjust dimEffect = new ColorAdjust();
            parentStage.getScene().getRoot().setEffect(dimEffect);

            Timeline fadeIn = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(dimEffect.brightnessProperty(), 0)),
                    new KeyFrame(Duration.millis(300), new KeyValue(dimEffect.brightnessProperty(), -0.3, Interpolator.EASE_BOTH))
            );
            fadeIn.play();

            Stage dialogStage = SceneUtil.createDialog("/Client/fxml/settings.fxml", parentStage, this, null, "Settings");

            dialogStage.showAndWait();

            Timeline fadeOut = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(dimEffect.brightnessProperty(), -0.3, Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.millis(300), new KeyValue(dimEffect.brightnessProperty(), 0))
            );
            fadeOut.setOnFinished(e -> parentStage.getScene().getRoot().setEffect(null));
            fadeOut.play();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading settings dialog: " + e.getMessage());
        }
    }

    /**
     * Toggles the mute state for the current user and updates all related UI components.
     * This is the single source of truth for changing the mute status.
     */
    @FXML
    private void toggleMuteState() {
        if (currentSelectedChat == null) return;

        boolean newMuteState = !currentSelectedChat.isMuted();
        currentSelectedChat.setMuted(newMuteState);
        notificationsToggle.setSelected(!newMuteState);
        notificationStatusLabel.setText(newMuteState ? "Disabled" : "Enabled");
        mutedIcon.setVisible(newMuteState);
        String message = (newMuteState ? "Muted" : "Unmuted") + " " + currentSelectedChat.getDisplayName();
        showTemporaryNotification(message + "\n");

        updateChannelMuteButtonText();

        refreshChatList();
    }

    /**
     * Handles the action from the JFXToggleButton.
     */
    private void toggleNotifications() {
        if (currentSelectedChat == null) return;

        boolean isCurrentlyMuted = currentSelectedChat.isMuted();
        boolean newMuteState = !isCurrentlyMuted;

        // --- Optimistically update UI ---
        currentSelectedChat.setMuted(newMuteState);
        boolean isEnabled = !newMuteState;
        notificationStatusLabel.setText(isEnabled ? "Enabled" : "Disabled");
        mutedIcon.setVisible(newMuteState);
        updateNotificationToggle(isEnabled);
        String message = (newMuteState ? "Muted" : "Unmuted") + " " + currentSelectedChat.getDisplayName();
        showTemporaryNotification(message + "\n");
        // --- End of optimistic update ---

        Task<RpcResponse<Object>> muteTask = chatService.toggleChatMute(UUID.fromString(currentSelectedChat.getChatId()), newMuteState);

        muteTask.setOnSucceeded(event -> {
            RpcResponse<Object> response = muteTask.getValue();
            if (response.getStatusCode() != StatusCode.OK) {
                // Revert UI on failure
                Platform.runLater(() -> {
                    currentSelectedChat.setMuted(isCurrentlyMuted); // Revert model
                    notificationStatusLabel.setText(!isCurrentlyMuted ? "Enabled" : "Disabled");
                    mutedIcon.setVisible(isCurrentlyMuted);
                    updateNotificationToggle(!isCurrentlyMuted);
                    showTemporaryNotification("Failed to update notification settings.\n");
                });
                System.err.println("Failed to toggle mute status: " + response.getMessage());
            }
        });

        muteTask.setOnFailed(event -> {
            // Revert UI on failure
            Platform.runLater(() -> {
                currentSelectedChat.setMuted(isCurrentlyMuted); // Revert model
                notificationStatusLabel.setText(!isCurrentlyMuted ? "Enabled" : "Disabled");
                mutedIcon.setVisible(isCurrentlyMuted);
                updateNotificationToggle(!isCurrentlyMuted);
                showTemporaryNotification("Error updating notification settings.\n");
            });
            muteTask.getException().printStackTrace();
        });

        new Thread(muteTask).start();
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
     * Handles the action from the ContextMenu's "Mute/Unmute" item.
     */
    private void toggleMute() {
        toggleMuteState();
    }

    /**
     * Toggles the pinned state of the current chat.
     */
    private void togglePin() {
        if (currentSelectedChat == null) return;

        boolean newPinState = !currentSelectedChat.isPinned();
        currentSelectedChat.setPinned(newPinState);

        String message = (newPinState ? "Pinned" : "Unpinned") + " " + currentSelectedChat.getDisplayName();
        showTemporaryNotification(message + "\n");

        refreshChatList();
    }

    /**
     * Clears the chat history with a confirmation dialog.
     */
    private void clearChatHistory() {
        if (currentSelectedChat == null) return;

        messagesContainer.getChildren().clear();
        showEmptyChatState();

        // Update last message in chat list
        currentSelectedChat.setLastMessage("");
        refreshChatList();

        showTemporaryNotification("Chat history cleared\n");
    }

    /**
     * Blocks the current user with a confirmation dialog.
     */
    private void blockUser() {
        if (currentSelectedChat == null) return;

        String userName = currentSelectedChat.getDisplayName();
        allChatUsers.remove(currentSelectedChat);
        filteredChatUsers.remove(currentSelectedChat);

        goBackToWelcomeState();
        showTemporaryNotification("Blocked " + userName + "\n");
    }

    /**
     * Starts voice recording (placeholder).
     */
    private void startVoiceRecording() {
        System.out.println("Starting voice recording");
        // TODO: Implement voice recording functionality (UI: Show recording UI, Server: Handle audio upload).
        // Visual feedback
        sendButton.getStyleClass().add("recording");
        showTemporaryNotification("Voice recording started\n");
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
     * Shows a context menu with actions dynamically enabled/disabled based on chat type,
     * message type, sender, and user role (admin/member).
     *
     * @param event The MouseEvent triggering the menu.
     */
    private void showMessageContextMenu(MouseEvent event) {
        if (activeMessageContextMenu != null && activeMessageContextMenu.isShowing()) {
            activeMessageContextMenu.hide();
        }
        String role = currentSelectedChat.getUserMembershipType();
        boolean isChannelAdmin = currentSelectedChat.getType() == UserType.CHANNEL && ("OWNER".equals(role) || "ADMIN".equals(role));
        ContextMenu newMenu = new ContextMenu();
        newMenu.setAutoHide(true);

        VBox messageBubble = (VBox) event.getSource();
        boolean isOutgoing = messageBubble.getStyleClass().contains("outgoing");
        boolean isDocument = messageBubble.getStyleClass().contains("document-bubble");
        boolean isChannel = currentSelectedChat != null && currentSelectedChat.getType() == UserType.CHANNEL;

        MenuItem replyItem = createIconMenuItem("Reply", "/Client/images/context-menu/reply.png");
        replyItem.setOnAction(e -> showReplyPreview(messageBubble));
        if (isChannel && !isCurrentUserAdmin()) {
            replyItem.setDisable(true);
        }

        MenuItem forwardItem = createIconMenuItem("Forward", "/Client/images/context-menu/forward.png");
        forwardItem.setOnAction(e -> forwardMessage(messageBubble));

        MenuItem editItem = createIconMenuItem("Edit", "/Client/images/context-menu/edit.png");
        editItem.setOnAction(e -> editMessage(messageBubble));

        MenuItem downloadItem = createIconMenuItem("Download", "/Client/images/context-menu/download.png");
        downloadItem.setOnAction(e -> {
            if (messageBubble.getUserData() instanceof DocumentInfo) {
                saveDocument((DocumentInfo) messageBubble.getUserData());
            }
        });


        MenuItem deleteItem = createIconMenuItem("Delete", "/Client/images/context-menu/delete.png");
        deleteItem.setOnAction(e -> deleteMessage(messageBubble));
        if (!isOutgoing && !isCurrentUserAdmin()) {
            deleteItem.setDisable(true);
        }

        newMenu.getItems().add(replyItem);

        if (isOutgoing && !isDocument && (!isChannel || isCurrentUserAdmin())) {
            newMenu.getItems().add(editItem);
        }

        newMenu.getItems().add(forwardItem);

        if (isDocument) {
            newMenu.getItems().add(downloadItem);
            // copy link TODO: in the future
            // MenuItem copyLinkItem = createIconMenuItem("Copy Link", ...);
            // newMenu.getItems().add(copyLinkItem);
        } else {
            MenuItem copyItem = createIconMenuItem("Copy Text", "/Client/images/context-menu/copy.png");
            copyItem.setOnAction(e -> copyMessageText(messageBubble));
            newMenu.getItems().add(copyItem);
        }
        deleteItem.setOnAction(e -> deleteMessage(messageBubble));
        if (isOutgoing || isChannelAdmin) {
            newMenu.getItems().add(deleteItem);
        }

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

    private void promptForEdit(VBox messageBubble) {

        if (replyPreviewContainer.isVisible()) {
            closeReplyPreview();
        }

        UUID messageId = (UUID) messageBubble.getProperties().get("messageId");
        if (messageId == null) return;

        String rawText = (String) messageBubble.getProperties().get("raw_text");
        if (rawText == null) return;

        this.editingMessageBubble = messageBubble;
        this.originalEditText = rawText;

        //showEditPreview();
    }


    /**
     * Edits a message by sending a request to the server.
     * The UI will be updated by the MessageEditedEvent.
     */
    private void editMessage(VBox messageBubble) {
        if (messageBubble == null) return;
        resetReplyEditState();
        if (replyPreviewAnimation != null) replyPreviewAnimation.stop();

        isEditing = true;
        editingMessageBubble = messageBubble;

        String originalMessageText = (String) messageBubble.getProperties().get("raw_text");
        if (originalMessageText == null) {
            resetReplyEditState();
            return;
        }

        replyToLabel.setText("Edit Message");
        replyMessageLabel.setText(originalMessageText.length() > 45 ?
                originalMessageText.substring(0, 42) + "..." : originalMessageText);
        messageInputField.clear();
        replyMessageLabel.setText(originalMessageText);
        messageInputField.setText(originalMessageText);

        if (!replyPreviewContainer.isVisible()) {
            replyPreviewContainer.setVisible(true);
            replyPreviewContainer.setManaged(true);
            replyPreviewContainer.setTranslateY(-30);
            replyPreviewContainer.setOpacity(0);
            TranslateTransition slideDown = new TranslateTransition(Duration.millis(200), replyPreviewContainer);
            slideDown.setToY(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), replyPreviewContainer);
            fadeIn.setToValue(1.0);
            replyPreviewAnimation = new ParallelTransition(slideDown, fadeIn);
            replyPreviewAnimation.play();
        }

        messageInputField.requestFocus();
        messageInputField.positionCaret(originalMessageText.length());
    }

    private void confirmAndDeleteMessage(UUID messageId) {
        if (messageId == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Message");
        alert.setHeaderText("Are you sure you want to delete this message?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteMessage(messageId);
            }
        });
    }

    /**
     * Updates the text and icon of the mute button shown in restricted channels.
     */
    private void updateChannelMuteButtonText() {
        if (currentSelectedChat == null) return;

        if (currentSelectedChat.isMuted()) {
            channelMuteToggleButton.setText("UNMUTE");
        } else {
            channelMuteToggleButton.setText("MUTE");
        }
    }

    /**
     * Deletes a message by sending a request to the server.
     * The UI will be updated by the MessageDeletedEvent.
     */
    private void deleteMessage(UUID messageId) {
        Task<RpcResponse<Object>> deleteTask = chatService.deleteMessage(messageId);

        deleteTask.setOnSucceeded(event -> {
            RpcResponse<Object> response = deleteTask.getValue();
            if (response.getStatusCode() != StatusCode.OK) {
                Platform.runLater(() -> showTemporaryNotification("Failed to delete message: " + response.getMessage() + "\n"));
            }
        });
        deleteTask.setOnFailed(event -> {
            deleteTask.getException().printStackTrace();
            Platform.runLater(() -> showTemporaryNotification("Error deleting message.\n"));
        });
        new Thread(deleteTask).start();
    }

    /**
     * Copies the raw text content of a message bubble to the system clipboard,
     * after stripping any formatting markers.
     *
     * @param messageBubble The VBox of the message from which to copy the text.
     */
    private void copyMessageText(VBox messageBubble) {
        if (messageBubble == null) return;

        String rawText = (String) messageBubble.getProperties().get("raw_text");

        if (rawText != null && !rawText.isEmpty()) {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();

            content.putString(rawText);
            clipboard.setContent(content);

            showTemporaryNotification("Text copied to clipboard.\n");
            System.out.println("Text copied: " + rawText);
        } else {
            System.err.println("Could not find raw text on the message bubble to copy.");
        }
    }

    protected void sendTypingStatus(boolean isTyping) {
        if (currentSelectedChat == null) return;
        Task<Void> typingTask = chatService.sendTypingStatus(UUID.fromString(currentSelectedChat.getChatId()), isTyping);
        // We run this in the background and don't need to handle success/failure explicitly on the UI
        typingTask.setOnFailed(e -> System.err.println("Failed to send typing status: " + typingTask.getException().getMessage()));
        new Thread(typingTask).start();
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
     * Forwards a message.
     */
    private void forwardMessage(VBox messageBubble) {
        if (messageBubble == null) return;

        UUID messageId = (UUID) messageBubble.getProperties().get("messageId");
        if (messageId == null) {
            showTemporaryNotification("Cannot forward this message (ID not found).\n");
            return;
        }

        System.out.println("Forwarding message: " + messageId);

        try {
            var forwardingData = new ForwardMessageController.ForwardingData(messageId, allChatUsers);

            Stage parentStage = (Stage) mainChatContainer.getScene().getWindow();

            Stage dialogStage = SceneUtil.createDialog(
                    "/Client/fxml/forwardMessageDialog.fxml",
                    parentStage,
                    this,
                    forwardingData,
                    "Forward Message"
            );

            dialogStage.show();
//            Optional<List<ChatViewModel>> result = dialog.showAndWait();
//
//            result.ifPresent(selectedUsers -> {
//                if (!selectedUsers.isEmpty()) {
//                    List<UUID> targetChatIds = selectedUsers.stream()
//                            .map(u -> UUID.fromString(u.getUserId()))
//                            .collect(Collectors.toList());
//
//                    Task<RpcResponse<Object>> forwardTask = chatService.forwardMessage(messageId, targetChatIds);
//                    forwardTask.setOnSucceeded(e ->
//                            Platform.runLater(() -> {
//                                RpcResponse<Object> response = forwardTask.getValue();
//                                showTemporaryNotification("Message forwarded.");
//
//                                // Update the last message for each chat we forwarded to.
//                                String newTimestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
//                                String lastMessagePreview = "You: " + TextUtil.stripFormattingForCopying(messageContent);
//
//
//                                ChatViewModel lastUpdatedUser = null;
//                                for (ChatViewModel user : selectedUsers) {
//                                    user.setLastMessage(lastMessagePreview);
//                                    user.setTime(newTimestamp);
//                                    lastUpdatedUser = user;
//                                }
//
//                                if (lastUpdatedUser != null) {
//                                    reorderAndRefreshChatList(lastUpdatedUser);
//                                }
//                            }));
//
//                    forwardTask.setOnFailed(e -> {
//                        forwardTask.getException().printStackTrace();
//                        Platform.runLater(() -> showTemporaryNotification("Failed to forward message."));
//                    });
//                    new Thread(forwardTask).start();
//                }
//            });


        } catch (IOException e) {
            e.printStackTrace();
            showTemporaryNotification("Could not open forward dialog.\n");
        }
    }

    /**
     * Executes the actual forwarding of a message to multiple chats.
     * This is called by the ForwardMessageController.
     *
     * @param messageId        The ID of the message to forward.
     * @param recipientChatIds A list of chat IDs to forward the message to.
     */
    public void executeForwardMessage(UUID messageId, List<UUID> recipientChatIds) {
        if (messageId == null || recipientChatIds == null || recipientChatIds.isEmpty()) {
            return;
        }

        // Create the background task for forwarding the message
        Task<RpcResponse<Object>> forwardTask = chatService.forwardMessage(messageId, recipientChatIds);

        forwardTask.setOnSucceeded(event -> {
            RpcResponse<Object> response = forwardTask.getValue();
            Platform.runLater(() -> {
                if (response.getStatusCode() == StatusCode.OK) {
                    // --- Improved UI Feedback and Update Logic ---

                    // 1. Show a more informative success message
                    String successMessage = recipientChatIds.size() == 1 ?
                            "Message forwarded successfully!" :
                            "Message forwarded to " + recipientChatIds.size() + " chats.";
                    showTemporaryNotification(successMessage + "\n");

                    // 2. Prepare data for UI update
                    String newTimestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    String lastMessagePreview = "Forwarded message";

                    // 3. Create a Set of recipient IDs for efficient lookup
                    Set<String> recipientIdStrings = recipientChatIds.stream()
                            .map(UUID::toString)
                            .collect(Collectors.toSet());

                    // 4. Iterate through all known chats and update the ones that were recipients
                    allChatUsers.stream()
                            .filter(user -> recipientIdStrings.contains(user.getChatId()))
                            .forEach(user -> {
                                user.setLastMessage(lastMessagePreview);
                                user.setTime(newTimestamp);
                                user.setMessageStatus("sent"); // Ensure status is updated to a single tick
                            });

                    // 5. Sort and refresh the entire chat list just once after all updates
                    sortAndRefreshChatList();

                } else {
                    showTemporaryNotification("Failed to forward message: " + response.getMessage() + "\n");
                }
            });
        });

        forwardTask.setOnFailed(event -> {
            forwardTask.getException().printStackTrace();
            Platform.runLater(() -> showTemporaryNotification("Error forwarding message.\n"));
        });

        new Thread(forwardTask).start();
    }

    /**
     * Puts the UI into editing mode. It resets any prior reply state and configures
     * the preview panel for an edit action.
     *
     * @param messageBubble The VBox of the message to be edited.
     */


    /**
     * Deletes a message from the UI and optionally from the backend.
     *
     * @param messageBubble The VBox of the message to be deleted.
     */
    private void deleteMessage(VBox messageBubble) {
        if (messageBubble == null) return;
        UUID messageId = (UUID) messageBubble.getProperties().get("messageId");
        if (messageId != null) {
            confirmAndDeleteMessage(messageId);
        }
    }

    // ============ UTILITY METHODS ============

    /**
     * Loads the default header avatar image.
     * TODO (Server): Fetch default avatar from server if needed.
     */
    private void loadDefaultHeaderAvatar() {
        try {
            Image defaultAvatar = new Image(Objects.requireNonNull(getClass().getResource("/Client/images/11Devs-black.png")).toExternalForm());
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

    // ============ PUBLIC API METHODS ============
    /**
     * Updates the online status of a user in the chat list and UI.
     * TODO: Implement server-driven status updates.
     *
     * @param userName The name of the user whose status is updated.
     * @param isOnline True if the user is online, false otherwise.
     */
    public void updateUserOnlineStatus(String userName, boolean isOnline) {
        Platform.runLater(() -> {
            for (ChatViewModel user : allChatUsers) {
                if (user.getDisplayName().equals(userName)) {
                    user.setOnline(isOnline);
                    user.setLastSeen(isOnline ? "online" : "last seen just now");

                    if (user == currentSelectedChat) {
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
     * @param user The ChatViewModel to add.
     */
    public void addUser(ChatViewModel user) {
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
     * @param user The ChatViewModel to remove.
     */
    public void removeUser(ChatViewModel user) {
        Platform.runLater(() -> {
            allChatUsers.remove(user);
            filteredChatUsers.remove(user);

            if (currentSelectedChat == user) {
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
        for (ChatViewModel user : allChatUsers) {
            if (user.getDisplayName().equals(userName)) {
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
     * @return The current ChatViewModel, or null if none selected.
     */
    public ChatViewModel getCurrentSelectedChat() {
        return currentSelectedChat;
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
    public void handleUserStatusChanged(UserStatusChangedEventModel eventModel) {
        allChatUsers.stream()
                .filter(user -> user.getChatId().equals(eventModel.getChatId().toString()))
                .findFirst()
                .ifPresent(user -> {
                    user.setOnline(eventModel.isOnline());
                    if (!eventModel.isOnline() && eventModel.getLastSeenTimestamp() != null) {
                        user.setLastSeen(eventModel.getLastSeenTimestamp());
                    }

                    // If the updated user is currently selected, refresh the header and right panel
                    if (currentSelectedChat != null && currentSelectedChat.getChatId().equals(user.getChatId())) {
                        updateChatHeader(user);
                        if (isRightPanelVisible) {
                            updateRightPanel(user);
                        }
                    }
                });
    }

    public void handleChatInfoChanged(ChatInfoChangedEventModel eventModel) {
        allChatUsers.stream()
                .filter(user -> user.getChatId().equals(eventModel.getChatId().toString()))
                .findFirst()
                .ifPresent(user -> {
                    if(!user.getIsContact()) {
                        if (eventModel.getNewTitle() != null) {
                            user.setDisplayName(eventModel.getNewTitle());
                        }
                        if (eventModel.getNewProfilePictureId() != null) {
                            user.setAvatarId(eventModel.getNewProfilePictureId());
                        }

                        if (currentSelectedChat != null && currentSelectedChat.getChatId().equals(user.getChatId())) {
                            Platform.runLater(() -> {
                                updateChatHeader(user);
                                if (isRightPanelVisible) {
                                    updateRightPanel(user);
                                }
                            });
                        }
                    }
                });
    }
    public void openChatWithContact(ContactInfo contactInfo) {
        if (contactInfo == null) return;
        Task<RpcResponse<GetChatInfoOutputModel>> findChatTask = chatService.getChatByUserId(contactInfo.getUserId());

        findChatTask.setOnSucceeded(event -> {
            RpcResponse<GetChatInfoOutputModel> response = findChatTask.getValue();
            if (response.getStatusCode() == StatusCode.OK && response.getPayload() != null) {
                GetChatInfoOutputModel chatInfo = response.getPayload();
                Platform.runLater(() -> {
                    // Check if this user is already in the local chat list
                    Optional<ChatViewModel> existingUser = allChatUsers.stream()
                            .filter(uvm -> uvm.getChatId().equals(chatInfo.getChatId().toString()))
                            .findFirst();

                    ChatViewModel userToSelect;
                    if (existingUser.isPresent()) {
                        userToSelect = existingUser.get();
                    } else {
                        // If not present, create a new ChatViewModel and add it to the list
                        ChatViewModel uvm = new ChatViewModelBuilder()
                                .chatId(chatInfo.getChatId().toString())
                                .avatarId(chatInfo.getProfilePictureId())
                                .displayName(chatInfo.getTitle())
                                .lastMessage(chatInfo.getLastMessage())
                                .time(chatInfo.getLastMessageTimestamp())
                                .type(chatInfo.getType())
                                .notificationsNumber(String.valueOf(chatInfo.getUnreadCount()))
                                .build();
                        allChatUsers.add(0, uvm); // Add to the top of the master list
                        performSearch(searchField.getText()); // Re-apply current filter
                        userToSelect = uvm;
                    }

                    // Select the user in the ListView, which will open the chat
                    chatListView.getSelectionModel().select(userToSelect);
                    chatListView.scrollTo(userToSelect);
                });
            } else {
                Platform.runLater(() -> showTemporaryNotification("User @" + contactInfo.getUserId() + " not found.\n"));
                System.err.println("Failed to find user by username: " + response.getMessage());
            }
        });

        findChatTask.setOnFailed(event -> {
            findChatTask.getException().printStackTrace();
            Platform.runLater(() -> showTemporaryNotification("Error finding user.\n"));
        });

        new Thread(findChatTask).start();

    }

    public boolean isMyProfile(ChatViewModel userData) {
        return userData.getChatId().equals(currentUserId);
    }
}