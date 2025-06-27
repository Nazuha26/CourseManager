package com.coursemanagerfx.animations;

import com.coursemanagerfx.controllers.StageAttachable;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class WindowSlideInAnimation {

    public static void play(StageAttachable controller,
                            double width,
                            double height,
                            Duration totalDuration,
                            int waitMillis,
                            Runnable onFinished) {
        Node root = controller.getRootPane();

        Rectangle topRect = new Rectangle(0, 0, width, 0);
        Rectangle bottomRect = new Rectangle(0, height, width, 0);

        Group clipGroup = new Group(topRect, bottomRect);
        root.setClip(clipGroup);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(topRect.heightProperty(), 0),
                        new KeyValue(bottomRect.heightProperty(), 0),
                        new KeyValue(bottomRect.yProperty(), height)
                ),
                new KeyFrame(totalDuration,
                        new KeyValue(topRect.heightProperty(), height / 2),
                        new KeyValue(bottomRect.heightProperty(), height / 2),
                        new KeyValue(bottomRect.yProperty(), height / 2)
                )
        );

        timeline.setOnFinished(e -> {
            root.setClip(null);
            if (onFinished != null) onFinished.run();
        });

        PauseTransition wait = new PauseTransition(Duration.millis(waitMillis));
        wait.setOnFinished(e -> timeline.play());
        wait.play();
    }

    public static void play(StageAttachable controller,
                            double width,
                            double height,
                            Duration totalDuration,
                            Runnable onFinished) {
        play(controller, width, height, totalDuration, 100, onFinished);
    }
}
