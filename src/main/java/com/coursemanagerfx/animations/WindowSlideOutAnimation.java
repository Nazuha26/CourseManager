/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/


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

public class WindowSlideOutAnimation {

    public static void play(StageAttachable controller,
                            double width,
                            double height,
                            Duration totalDuration,
                            int waitMillis,
                            Runnable onFinished) {
        Node root = controller.getRootPane();


        Rectangle leftRect = new Rectangle(0, 0, width / 2, height);
        Rectangle rightRect = new Rectangle(width / 2, 0, width / 2, height);

        Group clipGroup = new Group(leftRect, rightRect);
        root.setClip(clipGroup);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(leftRect.xProperty(), 0),
                        new KeyValue(rightRect.xProperty(), width / 2)
                ),
                new KeyFrame(totalDuration,
                        new KeyValue(leftRect.xProperty(), -width / 2),
                        new KeyValue(rightRect.xProperty(), width)
                )
        );

        timeline.setOnFinished(e -> {
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
