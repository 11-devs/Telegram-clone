package Client.Controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the video player dialog. Manages video playback and a modern UI.
 *
 * SUPER EDITION: Enhanced with animations, keyboard shortcuts, and improved UX.
 */
public class VideoPlayerController implements Initializable {

    // --- FXML Injections ---
    @FXML private StackPane rootPane;
    @FXML private MediaView mediaView;
    @FXML private VBox controlsContainer;

    @FXML private Button playPauseButton;
    @FXML private SVGPath playIcon;
    @FXML private SVGPath pauseIcon;

    @FXML private HBox volumeContainer;
    @FXML private Button volumeButton;
    @FXML private SVGPath volumeHighIcon;
    @FXML private SVGPath volumeMuteIcon;
    @FXML private Slider volumeSlider;
    @FXML private StackPane volumeSliderContainer;

    @FXML private Label currentTimeLabel;
    @FXML private Label totalTimeLabel;

    @FXML private StackPane timeSliderContainer;
    @FXML private Pane trackBackground;
    @FXML private Pane bufferBar;
    @FXML private Pane progressBar;
    @FXML private Slider timeSlider;

    @FXML private Button fullscreenButton;
    @FXML private SVGPath fullscreenEnterIcon;
    @FXML private SVGPath fullscreenExitIcon;

    // --- State Management ---
    private MediaPlayer mediaPlayer;
    private final BooleanProperty isPlaying = new SimpleBooleanProperty(false);
    private final BooleanProperty isMuted = new SimpleBooleanProperty(false);
    private final BooleanProperty areControlsVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty isMouseOverControls = new SimpleBooleanProperty(false);
    private PauseTransition hideControlsTimer;
    private Timeline volumeSliderAnimation;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initial setup
        controlsContainer.setOpacity(0.0);
        hideControlsTimer = new PauseTransition(Duration.seconds(3));
        hideControlsTimer.setOnFinished(e -> hideControls());

        // --- THIS IS THE FIX ---
        // Force the controls VBox to be rendered on top of the MediaView.
        controlsContainer.toFront();

        setupEventHandlers();
        setupVolumeSliderAnimation();
    }

    /**
     * Sets the video file and initializes the player.
     * @param videoFile The video file to play.
     */
    public void setVideoFile(File videoFile) {
        if (videoFile != null && videoFile.exists()) {
            try {
                Media media = new Media(videoFile.toURI().toString());
                mediaPlayer = new MediaPlayer(media);
                mediaView.setMediaPlayer(mediaPlayer);

                // Make video view responsive
                mediaView.fitWidthProperty().bind(rootPane.widthProperty());
                mediaView.fitHeightProperty().bind(rootPane.heightProperty());

                setupMediaPlayerListeners();
                // Request focus for keyboard shortcuts
                Platform.runLater(rootPane::requestFocus);
                mediaPlayer.play();
            } catch (Exception e) {
                System.err.println("Error loading video file: " + e.getMessage());
            }
        }
    }

    private void setupEventHandlers() {
        playPauseButton.setOnAction(e -> togglePlayPause());
        volumeButton.setOnAction(e -> toggleMute());
        fullscreenButton.setOnAction(e -> toggleFullscreen());

        // Hide/Show controls on mouse movement
        rootPane.setOnMouseMoved(this::handleMouseMovement);
        rootPane.setOnMouseClicked(e -> { if (e.getClickCount() == 2) toggleFullscreen(); });

        // Smarter controls hiding logic
        controlsContainer.setOnMouseEntered(e -> isMouseOverControls.set(true));
        controlsContainer.setOnMouseExited(e -> isMouseOverControls.set(false));
        isMouseOverControls.addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                hideControlsTimer.stop();
            } else {
                resetHideControlsTimer();
            }
        });

        // Keyboard Shortcuts
        rootPane.setOnKeyPressed(this::handleKeyPress);
    }

    private void setupMediaPlayerListeners() {
        // Listener for when the media player is ready
        mediaPlayer.setOnReady(() -> {
            // Set initial values once media is loaded
            Duration totalDuration = mediaPlayer.getMedia().getDuration();
            timeSlider.setMax(totalDuration.toSeconds());
            totalTimeLabel.setText(formatDuration(totalDuration));
            volumeSlider.setValue(mediaPlayer.getVolume() * 100);
            isPlaying.set(true);
            showControls();
        });

        // Listener for playback time changes
        mediaPlayer.currentTimeProperty().addListener((obs, old, current) -> {
            if (!timeSlider.isValueChanging()) {
                timeSlider.setValue(current.toSeconds());
            }
            currentTimeLabel.setText(formatDuration(current));
            updateProgressBars();
        });

        // Listener for seeking with the time slider
        timeSlider.valueProperty().addListener((obs, old, current) -> {
            if (timeSlider.isValueChanging()) {
                mediaPlayer.seek(Duration.seconds(current.doubleValue()));
                updateProgressBars();
            }
        });
        timeSlider.setOnMousePressed(e -> mediaPlayer.seek(Duration.seconds(timeSlider.getValue())));
        timeSlider.setOnMouseDragged(e -> mediaPlayer.seek(Duration.seconds(timeSlider.getValue())));


        // Listener for volume slider
        volumeSlider.valueProperty().addListener((obs, old, current) -> {
            mediaPlayer.setVolume(current.doubleValue() / 100.0);
        });

        // Listener for buffer progress
        mediaPlayer.bufferProgressTimeProperty().addListener((obs, old, current) -> updateProgressBars());

        // Reset when video ends
        mediaPlayer.setOnEndOfMedia(() -> {
            mediaPlayer.seek(Duration.ZERO);
            mediaPlayer.pause();
            isPlaying.set(false);
            showControls(); // Show controls when video ends
        });

        // Bind UI elements to state properties
        isPlaying.addListener((obs, old, playing) -> {
            toggleIcon(playIcon, pauseIcon, !playing);
            if (playing) {
                resetHideControlsTimer();
            } else {
                showControls(); // Always show controls when paused
                hideControlsTimer.stop();
            }
        });
        isMuted.addListener((obs, old, muted) -> {
            mediaPlayer.setMute(muted);
            toggleIcon(volumeHighIcon, volumeMuteIcon, !muted);
        });

        mediaPlayer.volumeProperty().addListener((obs, old, newVol) -> {
            if (!volumeSlider.isValueChanging()) {
                volumeSlider.setValue(newVol.doubleValue() * 100);
            }
        });
    }

    private void setupVolumeSliderAnimation() {
        volumeSliderContainer.setMinWidth(0);
        volumeSliderContainer.setPrefWidth(0);
        volumeSliderContainer.setMaxWidth(0);

        volumeSliderAnimation = new Timeline(
                new KeyFrame(Duration.millis(250),
                        new KeyValue(volumeSliderContainer.minWidthProperty(), 80),
                        new KeyValue(volumeSliderContainer.prefWidthProperty(), 80),
                        new KeyValue(volumeSliderContainer.maxWidthProperty(), 80)
                )
        );

        volumeContainer.setOnMouseEntered(e -> volumeSliderAnimation.play());
        volumeContainer.setOnMouseExited(e -> {
            volumeSliderAnimation.setRate(-1); // Reverse the animation
            volumeSliderAnimation.play();
        });
    }

    private void updateProgressBars() {
        Platform.runLater(() -> {
            if (mediaPlayer == null || mediaPlayer.getMedia().getDuration() == null || !mediaPlayer.getMedia().getDuration().greaterThan(Duration.ZERO)) return;
            double totalWidth = timeSliderContainer.getWidth();
            double totalDuration = timeSlider.getMax();

            if(totalDuration <= 0) return;

            // Update playback progress bar
            double currentTime = mediaPlayer.getCurrentTime().toSeconds();
            double progress = (currentTime / totalDuration) * totalWidth;
            progressBar.setPrefWidth(progress);

            // Update buffer progress bar
            double bufferTime = mediaPlayer.getBufferProgressTime().toSeconds();
            double buffer = (bufferTime / totalDuration) * totalWidth;
            bufferBar.setPrefWidth(buffer);
        });
    }

    // --- Control Actions ---

    private void togglePlayPause() {
        if (mediaPlayer == null) return;
        isPlaying.set(!isPlaying.get());
        if (isPlaying.get()) {
            mediaPlayer.play();
        } else {
            mediaPlayer.pause();
        }
    }

    private void toggleMute() { isMuted.set(!isMuted.get()); }

    private void toggleFullscreen() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        if (stage != null) {
            stage.setFullScreen(!stage.isFullScreen());
            toggleIcon(fullscreenEnterIcon, fullscreenExitIcon, !stage.isFullScreen());
        }
    }

    private void seekRelative(Duration offset) {
        if (mediaPlayer == null) return;
        Duration newTime = mediaPlayer.getCurrentTime().add(offset);
        Duration totalDuration = mediaPlayer.getTotalDuration();

        // Clamp the new time between 0 and total duration
        if (newTime.lessThan(Duration.ZERO)) {
            newTime = Duration.ZERO;
        } else if (newTime.greaterThan(totalDuration)) {
            newTime = totalDuration;
        }

        mediaPlayer.seek(newTime);
        showControlsBriefly();
    }

    private void adjustVolume(double delta) {
        if (mediaPlayer == null) return;
        double newVolume = mediaPlayer.getVolume() + delta;
        // Clamp volume between 0.0 and 1.0
        newVolume = Math.max(0.0, Math.min(1.0, newVolume));
        mediaPlayer.setVolume(newVolume);
        showControlsBriefly();
    }


    // --- UI and Utility Methods ---

    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.SPACE) {
            togglePlayPause();
        } else if (event.getCode() == KeyCode.RIGHT) {
            seekRelative(Duration.seconds(5));
        } else if (event.getCode() == KeyCode.LEFT) {
            seekRelative(Duration.seconds(-5));
        } else if (event.getCode() == KeyCode.UP) {
            adjustVolume(0.1);
        } else if (event.getCode() == KeyCode.DOWN) {
            adjustVolume(-0.1);
        } else if (event.getCode() == KeyCode.F) {
            toggleFullscreen();
        } else if (event.getCode() == KeyCode.M) {
            toggleMute();
        }
    }

    private void handleMouseMovement(MouseEvent event) {
        showControls();
        resetHideControlsTimer();
    }

    private void resetHideControlsTimer() {
        hideControlsTimer.playFromStart();
    }

    private void showControls() {
        if (areControlsVisible.get()) return; // Already visible, do nothing
        areControlsVisible.set(true);
        animateControls(1.0);
    }

    private void showControlsBriefly() {
        showControls();
        resetHideControlsTimer();
    }

    private void hideControls() {
        if (!isPlaying.get() || isMouseOverControls.get()) return; // Don't hide if paused or mouse is over controls
        if (!areControlsVisible.get()) return; // Already hidden, do nothing
        areControlsVisible.set(false);
        animateControls(0.0);
    }

    private void animateControls(double targetOpacity) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), controlsContainer);
        ft.setToValue(targetOpacity);
        ft.play();
    }

    private void toggleIcon(SVGPath primaryIcon, SVGPath secondaryIcon, boolean showPrimary) {
        primaryIcon.setVisible(showPrimary);
        primaryIcon.setManaged(showPrimary);
        secondaryIcon.setVisible(!showPrimary);
        secondaryIcon.setManaged(!showPrimary);
    }

    private String formatDuration(Duration duration) {
        if(duration == null || duration.isIndefinite() || duration.isUnknown()) return "00:00";
        long totalSeconds = (long) duration.toSeconds();
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void cleanup() {
        hideControlsTimer.stop();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }
}