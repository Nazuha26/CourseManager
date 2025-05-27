package com.coursemanagerfx.custom_ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.Group;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class ProgressSpinner extends Region {
    private final int segments;
    private final double radius;
    private final double width;
    private final double length;
    private final Color baseColor;

    private Font font = new Font("Arial", 22);                          // by default
    public void setFont(Font font) {
        this.font = font;
        percentText.setFont(font);
    }

    private Color fontColor = Color.rgb(120, 120, 120, 0.47);     // by default
    public void setFontColor(Color fontColor) {
        this.fontColor = fontColor;
        percentText.setFill(fontColor);
    }

    private final Group group = new Group();
    private final Text percentText = new Text("0%");

    private int currentIndex = 0;

    private final DoubleProperty progress = new SimpleDoubleProperty(0);

    public final DoubleProperty progressProperty() { return progress; }

    public ProgressSpinner() {
        this(
                12,
                60,
                10,
                40,
                Color.rgb(120, 120, 120, 0.47),
                1000
        );
    }

    public ProgressSpinner(int segments,
                           double radius,
                           double width,
                           double length,
                           Color baseColor,
                           double durationMillis) {
        this.segments = segments;
        this.radius = radius;
        this.width = width;
        this.length = length;
        this.baseColor = baseColor;

        buildBars();
        buildText();
        getChildren().addAll(group, percentText);

        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(durationMillis / segments), e -> update()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        progress.addListener((obs, oldVal, newVal) -> {
            double value = Math.max(0.0, Math.min(newVal.doubleValue(), 1.0));
            int percent = (int) Math.round(value * 100);
            percentText.setText(percent + "%");
        });

    }

    private void buildBars() {
        group.getChildren().clear();

        for (int i = 0; i < segments; i++) {
            double angle = Math.toRadians((360.0 / segments) * i);
            double sin = Math.sin(angle);
            double cos = Math.cos(angle);

            double x1 = radius * cos;
            double y1 = radius * sin;
            double x2 = (radius + length) * cos;
            double y2 = (radius + length) * sin;

            Line line = new Line(x1, y1, x2, y2);
            line.setStrokeWidth(width);
            line.setStroke(Color.GRAY);
            line.setOpacity(0.2);
            group.getChildren().add(line);
        }

        group.setLayoutX(radius + length + width);
        group.setLayoutY(radius + length + width);
    }

    private void buildText() {
        percentText.setFont(font);
        percentText.setFill(fontColor);
        percentText.setTextAlignment(TextAlignment.CENTER);

        double center = radius + length + width;

        /*  centering by X  */
        percentText.layoutXProperty().bind(
                javafx.beans.binding.Bindings.createDoubleBinding(
                        () -> center - percentText.getLayoutBounds().getWidth() / 2,
                        percentText.layoutBoundsProperty()
                )
        );

        /*  centering by Y  */
        percentText.layoutYProperty().bind(
                javafx.beans.binding.Bindings.createDoubleBinding(
                        () -> center + percentText.getLayoutBounds().getHeight() / 4,
                        percentText.layoutBoundsProperty()
                )
        );
    }

    private void update() {
        for (int i = 0; i < segments; i++) {
            Line line = (Line) group.getChildren().get(i);
            int offset = (currentIndex - i + segments) % segments;
            line.setStroke(baseColor);

            double opacity = 1.0 - (double) offset / (segments - 1);
            line.setOpacity(Math.max(0.0, opacity));
        }
        currentIndex = (currentIndex + 1) % segments;
    }
}