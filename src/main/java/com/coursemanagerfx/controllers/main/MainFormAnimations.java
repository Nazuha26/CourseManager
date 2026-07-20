package com.coursemanagerfx.controllers.main;

import javafx.animation.*;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.util.Duration;

import java.util.Objects;
import java.util.function.Supplier;

/** Animations used by the event/history panel in the main window. */
public final class MainFormAnimations {

    public enum State { SHOW, HIDE }

    private final Supplier<Main_controller> controllerSupplier;
    private boolean animationPlaying;
    private boolean toggleAnimationPlaying;
    private boolean showingDatePicker;

    public MainFormAnimations(Supplier<Main_controller> controllerSupplier) {
        this.controllerSupplier = Objects.requireNonNull(
                controllerSupplier,
                "controllerSupplier");
    }

    public boolean isAnimationPlaying() {
        return animationPlaying;
    }

    public boolean isToggleAnimationPlaying() {
        return toggleAnimationPlaying;
    }

    public boolean isShowingDatePicker() {
        return showingDatePicker;
    }

    public void mainTopPanelInOut(State state) {
        mainTopPanelInOut(state, 400);
    }

    public void mainTopPanelInOut(State state, double milliseconds) {
        Main_controller controller = controllerSupplier.get();
        if (controller == null || animationPlaying) return;
        animationPlaying = true;
        if (state == State.SHOW) playIn(controller, milliseconds);
        else playOut(controller, milliseconds);
    }

    public void expirationInputInOut() {
        expirationInputInOut(300);
    }

    public void expirationInputInOut(double milliseconds) {
        Main_controller controller = controllerSupplier.get();
        if (controller == null || toggleAnimationPlaying) return;
        toggleAnimationPlaying = true;
        if (!showingDatePicker) playExpirationIn(controller, milliseconds);
        else playExpirationOut(controller, milliseconds);
    }

    public void showEventInfo() {
        Main_controller controller = controllerSupplier.get();
        if (controller == null) return;
        toggle(controller.getHistoryInfoMaskPane(), false);
        toggle(controller.getEventInfoMaskPane(), true);
    }

    public void showHistory() {
        Main_controller controller = controllerSupplier.get();
        if (controller == null) return;
        toggle(controller.getEventInfoMaskPane(), false);
        toggle(controller.getHistoryInfoMaskPane(), true);
    }

    private void playIn(Main_controller controller, double milliseconds) {
        Duration half = Duration.millis(milliseconds / 2);
        ParallelTransition hideBottom = slideFadeY(
                controller.getInfoBotPane(), 0, 30, 1, 0, half);
        ParallelTransition showTop = slideFadeX(
                controller.getInfoTopPane(), 50, 0, 0, 1, half);

        hideBottom.setOnFinished(event -> {
            toggle(controller.getInfoBotPane(), false);
            toggle(controller.getInfoTopPane(), true);
            showTop.play();
        });
        showTop.setOnFinished(event -> animationPlaying = false);
        new ParallelTransition(
                hideBottom,
                blink(controller.getTableStackPane(), milliseconds)).play();
    }

    private void playOut(Main_controller controller, double milliseconds) {
        Duration half = Duration.millis(milliseconds / 2);
        ParallelTransition hideTop = slideFadeX(
                controller.getInfoTopPane(), 0, 50, 1, 0, half);
        ParallelTransition showBottom = slideFadeY(
                controller.getInfoBotPane(), 30, 0, 0, 1, half);

        hideTop.setOnFinished(event -> {
            toggle(controller.getInfoTopPane(), false);
            toggle(controller.getInfoBotPane(), true);
            showBottom.play();
        });
        showBottom.setOnFinished(event -> animationPlaying = false);
        new ParallelTransition(
                hideTop,
                blink(controller.getTableStackPane(), milliseconds)).play();
    }

    private void playExpirationIn(Main_controller controller, double milliseconds) {
        Duration half = Duration.millis(milliseconds / 2);
        ParallelTransition hideTime = slideFadeX(
                controller.getHboxExpiredTime(), 0, -30, 1, 0, half);
        ParallelTransition showDate = slideFadeX(
                controller.getDtpkExpirationDate(), 30, 0, 0, 1, half);

        hideTime.setOnFinished(event -> {
            toggle(controller.getHboxExpiredTime(), false);
            toggle(controller.getDtpkExpirationDate(), true);
            showDate.play();
        });
        showDate.setOnFinished(event -> {
            showingDatePicker = true;
            toggleAnimationPlaying = false;
        });
        hideTime.play();
    }

    private void playExpirationOut(Main_controller controller, double milliseconds) {
        Duration half = Duration.millis(milliseconds / 2);
        ParallelTransition hideDate = slideFadeX(
                controller.getDtpkExpirationDate(), 0, 30, 1, 0, half);
        ParallelTransition showTime = slideFadeX(
                controller.getHboxExpiredTime(), -30, 0, 0, 1, half);

        hideDate.setOnFinished(event -> {
            toggle(controller.getDtpkExpirationDate(), false);
            toggle(controller.getHboxExpiredTime(), true);
            showTime.play();
        });
        showTime.setOnFinished(event -> {
            showingDatePicker = false;
            toggleAnimationPlaying = false;
        });
        hideDate.play();
    }

    private static ParallelTransition slideFadeY(
            Node node,
            double fromY,
            double toY,
            double fromOpacity,
            double toOpacity,
            Duration duration) {

        TranslateTransition slide = new TranslateTransition(duration, node);
        slide.setFromY(fromY);
        slide.setToY(toY);
        slide.setInterpolator(Interpolator.EASE_IN);
        return cacheDuring(
                node,
                new ParallelTransition(
                        slide,
                        fade(node, fromOpacity, toOpacity, duration))
        );
    }

    private static ParallelTransition slideFadeX(
            Node node,
            double fromX,
            double toX,
            double fromOpacity,
            double toOpacity,
            Duration duration) {

        TranslateTransition slide = new TranslateTransition(duration, node);
        slide.setFromX(fromX);
        slide.setToX(toX);
        slide.setInterpolator(Interpolator.EASE_IN);
        return cacheDuring(
                node,
                new ParallelTransition(
                        slide,
                        fade(node, fromOpacity, toOpacity, duration))
        );
    }

    private static FadeTransition fade(
            Node node,
            double from,
            double to,
            Duration duration) {

        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(from);
        fade.setToValue(to);
        fade.setInterpolator(Interpolator.EASE_IN);
        return fade;
    }

    private static void toggle(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private static Timeline blink(Node node, double milliseconds) {
        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(node.opacityProperty(), 1, Interpolator.EASE_BOTH)),
                new KeyFrame(
                        Duration.millis(milliseconds / 2),
                        new KeyValue(node.opacityProperty(), 0, Interpolator.EASE_BOTH)),
                new KeyFrame(
                        Duration.millis(milliseconds),
                        new KeyValue(node.opacityProperty(), 1, Interpolator.EASE_BOTH))
        );

        return cacheDuring(node, timeline);
    }

    private static <T extends Animation> T cacheDuring(Node node, T animation) {
        boolean[] previousCache = new boolean[1];
        CacheHint[] previousHint = new CacheHint[1];
        boolean[] caching = new boolean[1];

        animation.statusProperty().addListener((observable, oldStatus, newStatus) -> {
            if (newStatus == Animation.Status.RUNNING && !caching[0]) {
                previousCache[0] = node.isCache();
                previousHint[0] = node.getCacheHint();

                node.setCache(true);
                node.setCacheHint(CacheHint.SPEED);
                caching[0] = true;
            } else if (newStatus == Animation.Status.STOPPED && caching[0]) {
                node.setCacheHint(previousHint[0]);
                node.setCache(previousCache[0]);
                caching[0] = false;
            }
        });

        return animation;
    }
}
