/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic.commands.event_comms;

import com.coursemanagerfx.logic.Actions;
import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.basic.Student;
import com.coursemanagerfx.logic.basic.event.StudentEvent;
import com.coursemanagerfx.logic.commands.Command;

public class DeleteEventCommand implements Command {
    private final Group group;
    private final Student student;
    private final StudentEvent event;

    public DeleteEventCommand(Group group, Student student, StudentEvent event) {
        this.group = group;
        this.student = student;
        this.event = event;
    }

    @Override
    public void execute() {
        student.getEvents().remove(event);
        Actions.getInstance().repaint().smartRefresh(group, student);
    }

    @Override
    public void undo() {
        student.getEvents().add(event);
        Actions.getInstance().repaint().smartRefresh(group, student);
    }

    @Override
    public String getHistoryDescription() {
        String shortDesc = event.getDescription().length() > 10
                ? event.getDescription().substring(0, 10) + "..."
                : event.getDescription();
        return "deleted event \"" + event.getDescription() + "\"";
    }
}
