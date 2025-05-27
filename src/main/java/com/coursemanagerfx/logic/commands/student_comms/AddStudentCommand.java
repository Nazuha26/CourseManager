package com.coursemanagerfx.logic.commands.student_comms;

import com.coursemanagerfx.logic.Actions;
import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.basic.Student;
import com.coursemanagerfx.logic.commands.Command;

public class AddStudentCommand implements Command {
    private final Group group;
    private final Student student;

    public AddStudentCommand(Group group, Student student) {
        this.group = group;
        this.student = student;
    }

    @Override
    public void execute() {
        group.getStudents().add(student);
        Actions.getInstance().repaint().refreshStudentView(group, student);
        System.out.println("added student: { name: \"" + student.getName() + "\"; ID: " + student.getStudentID() + " }");
    }

    @Override
    public void undo() {
        group.getStudents().remove(student);
        Actions.getInstance().repaint().repaintStudentPanels(group);
        Actions.getInstance().select().selectAnyOrClear(group);
        System.out.println("undo adding student: { name: \"" + student.getName() + "\"; ID: " + student.getStudentID() + " }");
    }

    @Override
    public String getHistoryDescription() {
        return "added student \"" + student.getName() + "\"";
    }
}