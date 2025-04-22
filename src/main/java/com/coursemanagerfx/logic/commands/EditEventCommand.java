package com.coursemanagerfx.logic.commands;

import com.coursemanagerfx.controllers.Main_controller;
import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.basic.Student;
import com.coursemanagerfx.logic.basic.event.StudentEvent;
import com.coursemanagerfx.logic.utilitys.HistoryUtility;

public class EditEventCommand implements Command {
    private final Group group;
    private final Student student;
    private final StudentEvent event;
    private final StudentEvent before;
    private final StudentEvent after;
    private final Main_controller ctrl;
    public EditEventCommand(Group group, Student student,
                            StudentEvent original,
                            StudentEvent newCopy,
                            Main_controller ctrl) {
        this.group   = group;
        this.student = student;
        this.event   = original;
        this.before  = new StudentEvent(original);
        this.after   = newCopy;
        this.ctrl    = ctrl;
    }
    @Override
    public void execute(boolean isRedo) {
        apply(after);
        ctrl.loadStudentEvents(ctrl.getSelectedStudent().getID());

        String shortDesc = event.getDescription().length() > 10
                ? event.getDescription().substring(0, 10) + "..."
                : event.getDescription();
        if (!isRedo) HistoryUtility.setHistory(
                ctrl.getRichTxtPaneHistory(),
                ctrl.getLblCurHistory(),
                HistoryUtility.Types.INFO,
                "Edited event \"" + shortDesc + "\"");
    }

    @Override
    public void undo() {
        apply(before);
        ctrl.displayStudents(group);
        ctrl.selectGroupAndStudent(group, student.getID());
    }

    private void apply(StudentEvent src) {
        event.setDescription(src.getDescription());
        event.setCrtDate(src.getCrtDate());
        event.setMark(src.getMark());
        event.setExpDate(src.getExpDate());
    }

    @Override
    public String getDescription() {
        String shortDesc = event.getDescription().length() > 10
                ? event.getDescription().substring(0, 10) + "..."
                : event.getDescription();
        return "edited event \"" + shortDesc + "\"";
    }
}