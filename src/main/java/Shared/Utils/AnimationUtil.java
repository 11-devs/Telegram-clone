package Shared.Utils;

import javafx.animation.TranslateTransition;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.util.Duration;

public class AnimationUtil {
    public static void showErrorAnimation(Node node ,PseudoClass errorPseudoClass) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(60), node);
        shake.setFromX(0);
        shake.setByX(8);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        if(errorPseudoClass != null)
        {
            node.pseudoClassStateChanged(errorPseudoClass, true);
            shake.setOnFinished(e -> node.pseudoClassStateChanged(errorPseudoClass, false));
        }
        shake.play();
    }
    public static void showErrorAnimation(Node node) {
        showErrorAnimation(node,null);
    }
}
