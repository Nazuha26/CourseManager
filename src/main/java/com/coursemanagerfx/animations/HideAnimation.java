/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/


package com.coursemanagerfx.animations;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.stage.Stage;
import javafx.util.Duration;

public class HideAnimation {
    public static void play(Stage stage, Runnable onFinished) {
        play(stage, 200, onFinished);
    }

    public static void play(Stage stage, int durationMillis, Runnable onFinished) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(durationMillis),
                        new KeyValue(stage.getScene().getRoot().opacityProperty(), 0),
                        new KeyValue(stage.getScene().getRoot().scaleXProperty(), 0.8),
                        new KeyValue(stage.getScene().getRoot().scaleYProperty(), 0.8)
                )
        );
        timeline.setOnFinished(e -> {
            if (onFinished != null) onFinished.run();
        });
        timeline.play();
    }
}