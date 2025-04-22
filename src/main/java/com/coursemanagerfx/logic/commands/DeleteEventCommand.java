package com.coursemanagerfx.logic.commands;

import com.coursemanagerfx.controllers.Main_controller;
import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.basic.Student;
import com.coursemanagerfx.logic.basic.event.StudentEvent;
import com.coursemanagerfx.logic.utilitys.HistoryUtility;

public class DeleteEventCommand implements Command {
    private final Group group;
    private final Student student;
    private final StudentEvent event;
    private final Main_controller ctrl;

    public DeleteEventCommand(Group group, Student student,
                              StudentEvent event, Main_controller ctrl) {
        this.group = group;
        this.student = student;
        this.event = event;
        this.ctrl = ctrl;
    }

    @Override
    public void execute(boolean isRedo) {
        student.getEvents().remove(event);
        ctrl.loadStudentEvents(student.getID());

        String shortDesc = event.getDescription().length() > 10
                ? event.getDescription().substring(0, 10) + "..."
                : event.getDescription();

        if (!isRedo) {
            HistoryUtility.setHistory(
                    ctrl.getRichTxtPaneHistory(),
                    ctrl.getLblCurHistory(),
                    HistoryUtility.Types.SUCCESS,
                    "Deleted event \"" + shortDesc + "\""
            );
        }
    }

    @Override
    public void undo() {
        student.getEvents().add(event);
        ctrl.displayStudents(group);
        ctrl.selectGroupAndStudent(group, student.getID());
    }

    @Override
    public String getDescription() {
        String shortDesc = event.getDescription().length() > 10
                ? event.getDescription().substring(0, 10) + "..."
                : event.getDescription();
        return "deleted event \"" + shortDesc + "\"";
    }
}