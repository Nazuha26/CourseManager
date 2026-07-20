package com.coursemanagerfx.logic.autosave;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/** Runs periodic saves only while the open course has unsaved changes. */
public final class AutoSaveManager implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoSaveManager.class);

    private final BooleanSupplier dirtyState;
    private final Runnable saveAction;
    private final Timeline timeline;

    public AutoSaveManager(
            boolean enabled,
            int intervalSeconds,
            BooleanSupplier dirtyState,
            Runnable saveAction) {

        this.dirtyState = Objects.requireNonNull(dirtyState, "dirtyState");
        this.saveAction = Objects.requireNonNull(saveAction, "saveAction");

        if (!enabled) {
            timeline = null;
            return;
        }
        if (intervalSeconds <= 0) {
            throw new IllegalArgumentException("Autosave interval must be positive");
        }

        timeline = new Timeline(new KeyFrame(
                Duration.seconds(intervalSeconds),
                event -> saveIfDirty()));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    public void start() {
        if (timeline != null) timeline.play();
    }

    private void saveIfDirty() {
        if (!dirtyState.getAsBoolean()) return;
        try {
            saveAction.run();
        } catch (RuntimeException exception) {
            // Keep the repeating timeline alive after a transient save failure.
            LOGGER.error("Automatic course save failed", exception);
        }
    }

    @Override
    public void close() {
        if (timeline != null) timeline.stop();
    }
}
