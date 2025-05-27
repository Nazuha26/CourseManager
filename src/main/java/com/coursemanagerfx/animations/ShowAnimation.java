package com.coursemanagerfx.animations;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ShowAnimation {

    public static void play(Stage stage, Runnable onFinished) {
        play(stage, 200, onFinished);
    }

    public static void play(Stage stage, int durationMillis, Runnable onFinished) {
        stage.getScene().getRoot().setOpacity(0.0);
        stage.getScene().getRoot().setScaleX(0.8);
        stage.getScene().getRoot().setScaleY(0.8);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(durationMillis),
                        new KeyValue(stage.getScene().getRoot().opacityProperty(), 1),
                        new KeyValue(stage.getScene().getRoot().scaleXProperty(), 1),
                        new KeyValue(stage.getScene().getRoot().scaleYProperty(), 1)
                )
        );
        timeline.setOnFinished(e -> {
            if (onFinished != null) onFinished.run();
        });
        timeline.play();
    }
}