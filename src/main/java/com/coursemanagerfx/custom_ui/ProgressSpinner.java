/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.custom_ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import javafx.geometry.*;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.*;
import javafx.util.Duration;

import java.util.Objects;
import java.util.function.Consumer;

public class ProgressSpinner extends Region {

    /* =====================  PUBLIC API  ===================== */

    public DoubleProperty progressProperty() { return progress; }
    public double getProgress()            { return progress.get(); }
    public void   setProgress(double v)    { progress.set(clamp(v)); }

    /* centering */
    public <V> void showLoadingWindow( Window owner,
                                       boolean modal,
                                       Task<V> task,
                                       Consumer<V> onSuccess,
                                       Consumer<Throwable> onFail,
                                       String task_name ) {

        showLoadingWindow(owner, modal, task, onSuccess, onFail, task_name, 0, 0,
                new Image(Objects.requireNonNull(ProgressSpinner.class.getResourceAsStream(
                        "/com/coursemanagerfx/ui/icons/app/256.png"))));
    }

    public <V> void showLoadingWindow( Window owner,
                                       boolean modal,
                                       Task<V> task,
                                       Consumer<V> onSuccess,
                                       Consumer<Throwable> onFail,
                                       String task_name,
                                       double offsetXPercent,
                                       double offsetYPercent,
                                       Image icon) {

        /* ---------- stage shell ---------- */
        StackPane shell = new StackPane();
        shell.setAlignment(Pos.CENTER);
        shell.setStyle("""
                -fx-background-color: rgba(0,0,0,0.70);
                -fx-background-radius: 20;
                """);

        Scene scene = new Scene(shell);
        scene.setFill(Color.TRANSPARENT);

        Stage stage = new Stage(StageStyle.TRANSPARENT);
        if (modal) {
            stage.initModality(Modality.WINDOW_MODAL);
            if (owner != null) stage.initOwner(owner);
        } else {
            stage.initModality(Modality.NONE);
        }
        stage.setAlwaysOnTop(true);
        stage.getIcons().add(icon);
        stage.setScene(scene);

        stage.setOnShown(e -> {
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            double width  = stage.getWidth();
            double height = stage.getHeight();

            double centerX = screenBounds.getMinX() + (screenBounds.getWidth()  - width) / 2;
            double centerY = screenBounds.getMinY() + (screenBounds.getHeight() - height) / 2;

            stage.setX(centerX + screenBounds.getWidth()  * offsetXPercent);
            stage.setY(centerY + screenBounds.getHeight() * offsetYPercent);
        });

        /* ---------- spinner ---------- */
        shell.getChildren().add(this);
        stage.show();

        /* ---------- async task ---------- */
        this.progressProperty().bind(task.progressProperty());

        task.setOnSucceeded(e -> {
            stage.close();
            onSuccess.accept(task.getValue());
        });
        task.setOnFailed(e -> {
            stage.close();
            onFail.accept(task.getException());
        });

        new Thread(task, task_name).start();
    }

    /* ==================  Constructor  ================= */

    public ProgressSpinner() {
        this.segments = 12;
        this.radius = 30;
        this.barWidth = 5;
        this.barLength = 15;
        this.baseColor = Color.rgb(100, 200, 100);
        this.baseFont = Font.font("Roboto", 16);
        this.loadingMessage = "none";
    }

    public ProgressSpinner(int     segments,
                           double  radius,
                           double  width,
                           double  length,
                           Color   baseColor,
                           Font    baseFont,
                           double  durationMillis,
                           String  loadingMessage) {

        this.segments       = segments;
        this.radius         = radius;
        this.barWidth       = width;
        this.barLength      = length;
        this.baseColor      = baseColor;
        this.baseFont       = baseFont;
        this.loadingMessage = loadingMessage;

        buildBars();
        buildPercentText();
        buildLoadingLabel();
        assembleLayout();

        startBarAnimation(durationMillis);
        startLoadingAnimation(durationMillis);
        bindPercentToProgress();
    }

    /* ================  Internal building  ================ */

    private void buildBars() {
        bars = new Group();
        for (int i = 0; i < segments; i++) {
            double a   = Math.toRadians(360.0 / segments * i);
            double sin = Math.sin(a);
            double cos = Math.cos(a);

            double x1 =  radius            * cos;
            double y1 =  radius            * sin;
            double x2 = (radius + barLength) * cos;
            double y2 = (radius + barLength) * sin;

            Line line = new Line(x1, y1, x2, y2);
            line.setStroke(baseColor);
            line.setStrokeWidth(barWidth);
            line.setOpacity(0.15);
            bars.getChildren().add(line);
        }
    }

    private void buildPercentText() {
        percentText = new Text("0%");
        percentText.setFont(baseFont);
        percentText.setFill(baseColor);
    }

    private void buildLoadingLabel() {
        loadingText = new Text(loadingMessage + "...");
        loadingText.setFont(baseFont);
        loadingText.setFill(baseColor);
    }

    private void assembleLayout() {
        StackPane spinnerPane = new StackPane(bars, percentText);
        spinnerPane.setAlignment(Pos.CENTER);

        layoutRoot = new VBox(8, spinnerPane, loadingText);
        layoutRoot.setAlignment(Pos.CENTER);

        getChildren().add(layoutRoot);

        setPadding(new Insets(15));
    }

    /* ================  Animations  ================= */

    private void startBarAnimation(double cycleMillis) {
        Timeline spin = new Timeline(
                new KeyFrame(Duration.millis(cycleMillis / segments), e -> advance()));
        spin.setCycleCount(Animation.INDEFINITE);
        spin.play();
    }

    private void startLoadingAnimation(double durationMillis) {
        Timeline dots = new Timeline(
                new KeyFrame(Duration.millis(0),
                        e -> loadingText.setText(loadingMessage + ".")),
                new KeyFrame(Duration.millis(durationMillis / 2),
                        e -> loadingText.setText(loadingMessage + "..")),
                new KeyFrame(Duration.millis(durationMillis),
                        e -> loadingText.setText(loadingMessage + "...")),
                new KeyFrame(Duration.millis(durationMillis + (durationMillis / 2)),
                        e -> loadingText.setText(loadingMessage)));
        dots.setCycleCount(Animation.INDEFINITE);
        dots.play();
    }

    private void advance() {
        for (int i = 0; i < segments; i++) {
            Line line = (Line) bars.getChildren().get(i);
            int offset   = (currentTick - i + segments) % segments;
            double alpha = 1.0 - (double) offset / (segments - 1);
            line.setOpacity(Math.max(0.15, alpha));
        }
        currentTick = (currentTick + 1) % segments;
    }

    private void bindPercentToProgress() {
        progress.addListener((obs, oldV, newV) ->
                percentText.setText(String.format("%d%%", (int) Math.round(clamp(newV.doubleValue()) * 100))));
    }

    /* ===============  Layout overrides  =============== */

    @Override protected double computePrefWidth(double h)
        { return snapSizeX(layoutRoot.prefWidth(h) + getInsets().getLeft() + getInsets().getRight()); }

    @Override protected double computePrefHeight(double w)
        { return snapSizeY(layoutRoot.prefHeight(w) + getInsets().getTop() + getInsets().getBottom()); }

    @Override protected void layoutChildren() {
        double x = getInsets().getLeft();
        double y = getInsets().getTop();
        double w = getWidth()  - x - getInsets().getRight();
        double h = getHeight() - y - getInsets().getBottom();
        layoutInArea(layoutRoot, x, y, w, h, 0, HPos.CENTER, VPos.CENTER);
    }

    /* ====================  Utils / fields  =================== */

    private static double clamp(double v)
        { return Math.max(0, Math.min(1, v)); }

    private      final     int            segments;
    private      final     double         radius, barWidth, barLength;
    private      final     Color          baseColor;
    private      final     Font           baseFont;
    private      final     String         loadingMessage;

    private                Group          bars;
    private                Text           percentText;
    private                Text           loadingText;
    private                VBox           layoutRoot;

    private final DoubleProperty progress = new SimpleDoubleProperty(0);
    private int currentTick = 0;
}
