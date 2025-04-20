package com.coursemanagerfx.logic.commands;

public interface Command {
    void execute(boolean isRedo);
    void undo();
    public abstract String getDescription();
}