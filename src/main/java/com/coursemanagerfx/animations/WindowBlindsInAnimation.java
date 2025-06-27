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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WindowBlindsInAnimation {

    public static void play(StageAttachable controller,
                            double width,
                            double height,
                            int stripeCount,
                            Duration totalDuration,
                            int waitMillis,
                            Runnable onFinished) {

        Node root = controller.getRootPane();

        Group clipGroup = new Group();
        Random random = new Random();

        double stripeHeight = height / stripeCount;
        List<Rectangle> stripes = new ArrayList<>();

        for (int i = 0; i < stripeCount; i++) {
            Rectangle stripe = new Rectangle(0, i * stripeHeight - 1, width, stripeHeight + 2);
            stripe.setHeight(0);
            stripes.add(stripe);
            clipGroup.getChildren().add(stripe);
        }

        root.setClip(clipGroup);

        Timeline timeline = new Timeline();

        for (Rectangle stripe : stripes) {
            double delay = random.nextDouble() * totalDuration.toMillis() * 0.6;
            Duration start = Duration.millis(delay);
            Duration end = start.add(Duration.millis(350));

            timeline.getKeyFrames().addAll(
                    new KeyFrame(start, new KeyValue(stripe.heightProperty(), 0)),
                    new KeyFrame(end, new KeyValue(stripe.heightProperty(), stripeHeight))
            );
        }

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
                            int stripeCount,
                            Duration totalDuration,
                            Runnable onFinished) {
        play(controller, width, height, stripeCount, totalDuration, 100, onFinished);
    }
}
