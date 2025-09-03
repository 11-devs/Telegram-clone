package Shared.Utils;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class TelegramCellUtils {

    // Animation constants
    private static final Duration HOVER_DURATION = Duration.millis(150);
    private static final Duration SELECTION_DURATION = Duration.millis(200);
    private static final Duration NOTIFICATION_DURATION = Duration.millis(300);

    /**
     * Creates a smooth hover effect for the cell
     */
    public static void createHoverEffect(Node node) {
        ScaleTransition scaleUp = new ScaleTransition(HOVER_DURATION, node);
        scaleUp.setToX(1.02);
        scaleUp.setToY(1.02);

        FadeTransition fadeIn = new FadeTransition(HOVER_DURATION, node);
        fadeIn.setToValue(0.95);

        ParallelTransition hoverIn = new ParallelTransition(scaleUp, fadeIn);

        node.setOnMouseEntered(e -> hoverIn.play());

        ScaleTransition scaleDown = new ScaleTransition(HOVER_DURATION, node);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        FadeTransition fadeOut = new FadeTransition(HOVER_DURATION, node);
        fadeOut.setToValue(1.0);

        ParallelTransition hoverOut = new ParallelTransition(scaleDown, fadeOut);

        node.setOnMouseExited(e -> hoverOut.play());
    }

    /**
     * Creates a selection animation
     */
    public static void animateSelection(Node node, boolean selected) {
        Timeline timeline = new Timeline();

        if (selected) {
            // Selection animation
            KeyFrame keyFrame = new KeyFrame(SELECTION_DURATION,
                    new KeyValue(node.scaleXProperty(), 0.98),
                    new KeyValue(node.scaleYProperty(), 0.98)
            );
            timeline.getKeyFrames().add(keyFrame);
        } else {
            // Deselection animation
            KeyFrame keyFrame = new KeyFrame(SELECTION_DURATION,
                    new KeyValue(node.scaleXProperty(), 1.0),
                    new KeyValue(node.scaleYProperty(), 1.0)
            );
            timeline.getKeyFrames().add(keyFrame);
        }

        timeline.play();
    }

    /**
     * Creates a notification badge animation
     */
    public static void animateNotificationBadge(Node badge, boolean show) {
        if (show) {
            // Show animation
            badge.setVisible(true);
            badge.setScaleX(0);
            badge.setScaleY(0);

            ScaleTransition scale = new ScaleTransition(NOTIFICATION_DURATION, badge);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fade = new FadeTransition(NOTIFICATION_DURATION, badge);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);

            ParallelTransition showAnimation = new ParallelTransition(scale, fade);
            showAnimation.play();
        } else {
            // Hide animation
            ScaleTransition scale = new ScaleTransition(NOTIFICATION_DURATION, badge);
            scale.setToX(0);
            scale.setToY(0);
            scale.setInterpolator(Interpolator.EASE_IN);

            FadeTransition fade = new FadeTransition(NOTIFICATION_DURATION, badge);
            fade.setToValue(0.0);

            ParallelTransition hideAnimation = new ParallelTransition(scale, fade);
            hideAnimation.setOnFinished(e -> badge.setVisible(false));
            hideAnimation.play();
        }
    }

    /**
     * Creates a typing indicator animation
     */
    public static Timeline createTypingAnimation(Node indicator) {
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);

        KeyFrame keyFrame = new KeyFrame(Duration.millis(800),
                new KeyValue(indicator.opacityProperty(), 0.3)
        );
        timeline.getKeyFrames().add(keyFrame);

        return timeline;
    }

    /**
     * Creates a smooth scroll animation for ListView
     */
    public static void smoothScrollTo(ListView<?> listView, int index) {
        if (index < 0 || index >= listView.getItems().size()) return;

        // Get current scroll position
        double currentPosition = listView.getLayoutBounds().getHeight() * index / listView.getItems().size();

        // Create scroll animation
        Timeline timeline = new Timeline();
        KeyFrame keyFrame = new KeyFrame(Duration.millis(300),
                new KeyValue(listView.translateYProperty(), -currentPosition, Interpolator.EASE_OUT)
        );
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    /**
     * Creates a drop shadow effect for avatars
     */
    public static DropShadow createAvatarShadow(boolean hover) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, hover ? 0.4 : 0.2));
        shadow.setRadius(hover ? 8 : 5);
        shadow.setSpread(0.3);
        shadow.setOffsetX(0);
        shadow.setOffsetY(hover ? 2 : 1);
        return shadow;
    }

    /**
     * Creates a pulse animation for online status
     */
    public static Timeline createOnlineStatusPulse(Node statusIndicator) {
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);

        KeyFrame keyFrame1 = new KeyFrame(Duration.ZERO,
                new KeyValue(statusIndicator.scaleXProperty(), 1.0),
                new KeyValue(statusIndicator.scaleYProperty(), 1.0)
        );

        KeyFrame keyFrame2 = new KeyFrame(Duration.millis(1000),
                new KeyValue(statusIndicator.scaleXProperty(), 1.1),
                new KeyValue(statusIndicator.scaleYProperty(), 1.1)
        );

        timeline.getKeyFrames().addAll(keyFrame1, keyFrame2);
        return timeline;
    }

    /**
     * Creates a slide-in animation for new messages
     */
    public static void animateNewMessage(Node messageNode) {
        // Start from right
        messageNode.setTranslateX(300);
        messageNode.setOpacity(0);

        // Slide in animation
        TranslateTransition slide = new TranslateTransition(Duration.millis(300), messageNode);
        slide.setToX(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fade = new FadeTransition(Duration.millis(300), messageNode);
        fade.setToValue(1.0);

        ParallelTransition slideIn = new ParallelTransition(slide, fade);
        slideIn.play();
    }

    /**
     * Creates a shake animation for error states
     */
    public static void createShakeAnimation(Node node) {
        Timeline timeline = new Timeline();

        KeyFrame keyFrame1 = new KeyFrame(Duration.millis(0), new KeyValue(node.translateXProperty(), 0));
        KeyFrame keyFrame2 = new KeyFrame(Duration.millis(50), new KeyValue(node.translateXProperty(), -5));
        KeyFrame keyFrame3 = new KeyFrame(Duration.millis(100), new KeyValue(node.translateXProperty(), 5));
        KeyFrame keyFrame4 = new KeyFrame(Duration.millis(150), new KeyValue(node.translateXProperty(), -5));
        KeyFrame keyFrame5 = new KeyFrame(Duration.millis(200), new KeyValue(node.translateXProperty(), 5));
        KeyFrame keyFrame6 = new KeyFrame(Duration.millis(250), new KeyValue(node.translateXProperty(), 0));

        timeline.getKeyFrames().addAll(keyFrame1, keyFrame2, keyFrame3, keyFrame4, keyFrame5, keyFrame6);
        timeline.play();
    }

    /**
     * Utility method to safely apply CSS class
     */
    public static void addStyleClass(Node node, String styleClass) {
        if (node != null && !node.getStyleClass().contains(styleClass)) {
            node.getStyleClass().add(styleClass);
        }
    }

    /**
     * Utility method to safely remove CSS class
     */
    public static void removeStyleClass(Node node, String styleClass) {
        if (node != null) {
            node.getStyleClass().remove(styleClass);
        }
    }

    /**
     * Utility method to toggle CSS class
     */
    public static void toggleStyleClass(Node node, String styleClass) {
        if (node != null) {
            if (node.getStyleClass().contains(styleClass)) {
                node.getStyleClass().remove(styleClass);
            } else {
                node.getStyleClass().add(styleClass);
            }
        }
    }
}
