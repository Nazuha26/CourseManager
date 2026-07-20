package com.coursemanagerfx.logic.history;

import com.coursemanagerfx.logic.commands.Command;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UndoRedoManagerTest {

    @Test
    void dirtyStateTracksCommandsAndSuccessfulSave() {
        HistoryManager history = new HistoryManager(() -> null);
        UndoRedoManager manager = new UndoRedoManager(() -> true, history);
        TestCommand command = new TestCommand();

        assertFalse(manager.hasUnsavedChanges());

        command.execute();
        manager.add(command);
        assertTrue(manager.hasUnsavedChanges());
        assertEquals(1, command.value);

        manager.undo();
        assertFalse(manager.hasUnsavedChanges());
        assertEquals(0, command.value);

        manager.redo();
        assertTrue(manager.hasUnsavedChanges());
        assertEquals(1, command.value);

        manager.markSaved();
        assertFalse(manager.hasUnsavedChanges());
        assertFalse(manager.canUndoProperty().get());
        assertFalse(manager.canRedoProperty().get());
    }

    private static final class TestCommand implements Command {
        private int value;

        @Override
        public void execute() {
            value++;
        }

        @Override
        public void undo() {
            value--;
        }

        @Override
        public String getHistoryDescription() {
            return "test command";
        }
    }
}
