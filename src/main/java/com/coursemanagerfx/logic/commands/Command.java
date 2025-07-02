/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic.commands;

public interface Command {
    void execute();     // just do action
    //void redo();        // just do action with redo history
    void undo();        // undo action with undo history
    String getHistoryDescription();     // text for history description
}
