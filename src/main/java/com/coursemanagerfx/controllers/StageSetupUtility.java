package com.coursemanagerfx.controllers;

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class StageSetupUtility {
    public static void setup(StageAttachable controller, Stage stage) {
        setup(controller, stage, 20);
    }

    public static void setup(StageAttachable controller, Stage stage, int arcs) {
        BorderPane root = controller.getRootPane();
        Node titleBar = controller.getTitleBar();
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(root.widthProperty());
        clip.heightProperty().bind(root.heightProperty());
        clip.setArcWidth(arcs);
        clip.setArcHeight(arcs);
        root.setClip(clip);

        controller.setStage(stage);

        final double[] offset = new double[2];

        titleBar.setOnMousePressed(e -> {
            offset[0] = e.getSceneX();
            offset[1] = e.getSceneY();
        });

        titleBar.setOnMouseDragged(e -> {
            onDragged(e, stage, offset[0], offset[1]);
        });
    }

    // helper dragged method
    private static void onDragged(MouseEvent event, Stage stage, double xOffset, double yOffset) {
        if (stage == null) return;
        double newX = event.getScreenX() - xOffset;
        double newY = event.getScreenY() - yOffset;
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

        if (newX < bounds.getMinX()) newX = bounds.getMinX();
        if (newX + stage.getWidth() > bounds.getMaxX()) newX = bounds.getMaxX() - stage.getWidth();
        if (newY < bounds.getMinY()) newY = bounds.getMinY();
        if (newY + stage.getHeight() > bounds.getMaxY()) newY = bounds.getMaxY() - stage.getHeight();

        stage.setX(newX);
        stage.setY(newY);
    }
}