package util;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public final class AnimationUtil {

    private AnimationUtil() {}

    public static void fadeInSlideUp(Node node, double durationMillis) {
        node.setOpacity(0);
        node.setTranslateY(14);

        FadeTransition fade = new FadeTransition(Duration.millis(durationMillis), node);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(durationMillis), node);
        slide.setFromY(14);
        slide.setToY(0);

        new ParallelTransition(fade, slide).play();
    }

    public static void fadeIn(Node node, double durationMillis) {
        node.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(durationMillis), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    public static void shake(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(55), node);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.setOnFinished(e -> node.setTranslateX(0));
        tt.play();
    }

    public static void pulse(Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(90), node);
        st.setFromX(1);
        st.setFromY(1);
        st.setToX(0.93);
        st.setToY(0.93);
        st.setCycleCount(2);
        st.setAutoReverse(true);
        st.play();
    }

    public static void attachHoverScale(Node node, double hoverScale) {
        ScaleTransition grow = new ScaleTransition(Duration.millis(120), node);
        grow.setToX(hoverScale);
        grow.setToY(hoverScale);

        ScaleTransition shrink = new ScaleTransition(Duration.millis(120), node);
        shrink.setToX(1.0);
        shrink.setToY(1.0);

        node.setOnMouseEntered(e -> { shrink.stop(); grow.playFromStart(); });
        node.setOnMouseExited(e -> { grow.stop(); shrink.playFromStart(); });
    }
}
