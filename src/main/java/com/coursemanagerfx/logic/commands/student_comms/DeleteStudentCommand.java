package com.coursemanagerfx.logic.commands.student_comms;

import com.coursemanagerfx.logic.Actions;
import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.basic.Student;
import com.coursemanagerfx.logic.commands.Command;

public class DeleteStudentCommand implements Command {
    private final Group group;
    private final Student student;
    private int previousIndex = -1;

    public DeleteStudentCommand(Group group, Student student) {
        this.group = group;
        this.student = student;
    }

    @Override
    public void execute() {
        previousIndex = group.getStudents().indexOf(student);
        group.getStudents().remove(student);
        Actions.getInstance().repaint().repaintStudentPanels(group);
        Actions.getInstance().select().selectAnyOrClear(group);
    }

    @Override
    public void undo() {
        if (previousIndex >= 0 && previousIndex <= group.getStudents().size()) {
            group.getStudents().add(previousIndex, student);
        } else {
            group.getStudents().add(student); // fallback, if something went wrong
        }

        Actions.getInstance().repaint().refreshStudentView(group, student);
    }

    @Override
    public String getHistoryDescription() {
        return "deleted student \"" + student.getName() + "\"";
    }
}