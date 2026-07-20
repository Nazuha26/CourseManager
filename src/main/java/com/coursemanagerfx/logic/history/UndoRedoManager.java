package com.coursemanagerfx.logic.history;

import com.coursemanagerfx.logic.commands.Command;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/** Owns undo/redo stacks and also serves as the current dirty-state source. */
public final class UndoRedoManager {

    private final BooleanSupplier controllerAvailable;
    private final HistoryManager history;
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();
    private final BooleanProperty canUndo = new SimpleBooleanProperty(false);
    private final BooleanProperty canRedo = new SimpleBooleanProperty(false);
    private boolean disabled;

    public UndoRedoManager(
            BooleanSupplier controllerAvailable,
            HistoryManager history) {

        this.controllerAvailable = Objects.requireNonNull(
                controllerAvailable,
                "controllerAvailable");
        this.history = Objects.requireNonNull(history, "history");
        updateState();
    }

    public Deque<Command> getUndoStack() {
        return undoStack;
    }

    public Deque<Command> getRedoStack() {
        return redoStack;
    }

    public BooleanProperty canUndoProperty() {
        return canUndo;
    }

    public BooleanProperty canRedoProperty() {
        return canRedo;
    }

    public boolean hasUnsavedChanges() {
        return !undoStack.isEmpty();
    }

    public void setDisabled(boolean value) {
        disabled = value;
        updateState();
    }

    public void add(Command command) {
        if (!controllerAvailable.getAsBoolean() || command == null) return;
        undoStack.addLast(command);
        redoStack.clear();
        updateState();
    }

    public void undo() {
        if (!controllerAvailable.getAsBoolean() || undoStack.isEmpty() || disabled) return;

        Command command = undoStack.removeLast();
        command.undo();
        history.add(
                HistoryManager.HistoryType.INFO,
                "Undo: " + command.getHistoryDescription());
        redoStack.addLast(command);
        updateState();
    }

    public void redo() {
        if (!controllerAvailable.getAsBoolean() || redoStack.isEmpty() || disabled) return;

        Command command = redoStack.removeLast();
        command.execute();
        history.add(
                HistoryManager.HistoryType.INFO,
                "Redo: " + command.getHistoryDescription());
        undoStack.addLast(command);
        updateState();
    }

    public void markSaved() {
        undoStack.clear();
        redoStack.clear();
        updateState();
    }

    private void updateState() {
        canUndo.set(!disabled && !undoStack.isEmpty());
        canRedo.set(!disabled && !redoStack.isEmpty());
    }
}
