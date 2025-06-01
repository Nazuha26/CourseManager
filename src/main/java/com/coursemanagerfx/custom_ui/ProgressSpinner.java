package com.coursemanagerfx.custom_ui;

import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.Group;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

public class ProgressSpinner extends Region {

    /* ===== PUBLIC API ===== */

    public enum Style { SMALL, BIG }
    public enum Position { CENTER, TOP, BOTTOM }

    /* constructor with preset style */
    public ProgressSpinner(Window owner,
                           Style style,
                           Position position,
                           Modality modality,
                           String loadingMessage) {
        this(
            owner,
            style,
            Color.rgb(120, 120, 120, 0.47),
            position,
            modality,
            loadingMessage
        );
    }

    /* constructor with custom style and color */
    public ProgressSpinner(Window owner,
                           Style style,
                           Color color,
                           Position position,
                           Modality modality,
                           String loadingMessage) {
        this(
                owner,
                style == Style.SMALL ? 12 : 16,
                style == Style.SMALL ? 30 : 60,
                style == Style.SMALL ? 5  : 10,
                style == Style.SMALL ? 20 : 40,
                color,
                style == Style.SMALL ? new Font("Roboto", 16) : new Font("Roboto", 32),
                style == Style.SMALL ? 500 : 1000,
                loadingMessage,
                modality,
                position
        );
    }

    /* full custom constructor */
    public ProgressSpinner(Window owner,
                           int segments,
                           double radius,
                           double width,
                           double length,
                           Color baseColor,
                           Font baseFont,
                           double durationMillis,
                           String loadingMessage,
                           Modality modality,
                           Position position) {

        this.segments    = segments;
        this.radius      = radius;
        this.width       = width;
        this.length      = length;
        this.baseColor   = baseColor;
        this.baseFont    = baseFont;
        this.loadingBaseMessage = loadingMessage;
        this.position = position;

        buildBars();
        buildPercentText();
        buildLoadingMessage();
        getChildren().addAll(group, percentText, loadingLabel);

        /* segment animation */
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(durationMillis / segments), e -> update()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        /* loading animation */
        Timeline loadingAnim = new Timeline(
                new KeyFrame(Duration.seconds(0),   e -> loadingLabel.setText(loadingBaseMessage + ".")),
                new KeyFrame(Duration.seconds(0.5), e -> loadingLabel.setText(loadingBaseMessage + "..")),
                new KeyFrame(Duration.seconds(1),   e -> loadingLabel.setText(loadingBaseMessage + "...")),
                new KeyFrame(Duration.seconds(1.5), e -> loadingLabel.setText(loadingBaseMessage)));
        loadingAnim.setCycleCount(Animation.INDEFINITE);
        loadingAnim.play();

        /* creating the stage right away */
        StackPane root = new StackPane(this);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: transparent");

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initModality(modality);
        if (owner != null) dialog.initOwner(owner);
        dialog.setAlwaysOnTop(true);
        dialog.setScene(scene);

        /* stage positioning */
        dialog.setOnShown(e -> {
            if (owner == null) return;
            double x = owner.getX() + (owner.getWidth()  - dialog.getWidth())  / 2;
            double y;
            switch (position) {
                case TOP    -> y = owner.getY() + owner.getHeight() * 0.20 - dialog.getHeight() / 2;
                case BOTTOM -> y = owner.getY() + owner.getHeight() * 0.80 - dialog.getHeight() / 2;
                default     -> y = owner.getY() + (owner.getHeight() - dialog.getHeight()) / 2;
            }
            dialog.setX(x);
            dialog.setY(y);
        });

        /* set reaction on the progress property */
        progress.addListener((obs, ov, nv) -> {
            int pct = (int) Math.round(clamp(nv.doubleValue()) * 100);
            percentText.setText(pct + "%");
        });
    }

    /* show dialog */
    public void show() { dialog.show(); }

    /* close loading dialog */
    public void close() {
        dialog.close();
        progressProperty().unbind();
    }

    /*public void close(String finalMessage, double ms) {
        group.setVisible(false);
        percentText.setVisible(false);

        Label finalLabel = new Label(finalMessage);
        finalLabel.setText(finalMessage);
        finalLabel.setOpacity(0);
        finalLabel.setTranslateY(0);
        finalLabel.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(ms), finalLabel);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        TranslateTransition slide = new TranslateTransition(Duration.millis(ms), finalLabel);
        switch (this.position) {
            case BOTTOM -> slide.setByY(40);
            case TOP    -> slide.setByY(-40);
            default     -> slide.setByY(0);
        }

        FadeTransition fadeOut = new FadeTransition(Duration.millis(ms), finalLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        SequentialTransition sequence = new SequentialTransition(
                fadeIn,
                new ParallelTransition(slide, fadeOut)
        );

        sequence.setOnFinished(e -> {
            dialog.close();
            progressProperty().unbind();
        });
        sequence.play();
    }
*/


    /* ==================== CORE ==================== */

    private final int segments;
    private final double radius, width, length;
    private final Color baseColor;
    private final String loadingBaseMessage;
    private final Group group = new Group();
    private final Text percentText = new Text("0%");
    private final Text loadingLabel = new Text();
    private final DoubleProperty progress = new SimpleDoubleProperty(0);

    private final Position position;

    private final Font baseFont;

    private final Stage dialog;
    private int currentIndex = 0;

    public DoubleProperty progressProperty() { return progress; }

    private static double clamp(double v) { return Math.max(0.0, Math.min(1.0, v)); }

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

        //group.setLayoutX(radius + length + width);
        //group.setLayoutY(radius + length + width);
    }

    private void buildPercentText() {
        percentText.setFont(baseFont);
        percentText.setFill(baseColor);
        percentText.setTextAlignment(TextAlignment.CENTER);
    }

    private void buildLoadingMessage() {
        loadingLabel.setFont(baseFont);
        loadingLabel.setFill(baseColor);
        percentText.setFill(baseColor);
        loadingLabel.setTextAlignment(TextAlignment.CENTER);
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

    @Override
    protected double computePrefWidth(double height) {
        double spinnerDiameter = 2 * (radius + length);
        double pctW  = percentText.prefWidth(-1);
        double loadW = loadingLabel.prefWidth(-1);
        double textW = Math.max(pctW, loadW);
        double padding = 20;
        return Math.max(spinnerDiameter, textW) + padding;
    }

    @Override
    protected double computePrefHeight(double width) {
        double spinnerDiameter = 2 * (radius + length);
        double fontSpacing = baseFont.getSize() * 1.7;
        double textHeight = loadingLabel.prefHeight(-1);
        return spinnerDiameter + fontSpacing + textHeight;
    }

    @Override
    protected void layoutChildren() {
        double cx = getWidth()  / 2;
        double cy = getHeight() / 2;

        group.setLayoutX(cx);
        group.setLayoutY(cy);

        percentText.setLayoutX(cx - percentText.getLayoutBounds().getWidth() / 2);
        percentText.setLayoutY(cy + percentText.getLayoutBounds().getHeight() / 4);

        double fontSpacing = baseFont.getSize() * 1.7;

        loadingLabel.setLayoutX(cx - loadingLabel.getLayoutBounds().getWidth() / 2);
        loadingLabel.setLayoutY(cy + radius + length + fontSpacing);
    }
}