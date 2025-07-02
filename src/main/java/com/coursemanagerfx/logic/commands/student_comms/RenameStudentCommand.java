/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic.commands.student_comms;

import com.coursemanagerfx.logic.Actions;
import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.basic.Student;
import com.coursemanagerfx.logic.commands.Command;

public class RenameStudentCommand implements Command {
    private final Group group;
    private final Student student;
    private final String oldName;
    private final String newName;

    public RenameStudentCommand(Group group, Student student, String newName) {
        this.group = group;
        this.student = student;
        this.oldName = student.getName();
        this.newName = newName;
    }

    @Override
    public void execute() {
        student.setName(newName);
        Actions.getInstance().repaint().refreshStudentView(group, student);
    }

    @Override
    public void undo() {
        student.setName(oldName);
        Actions.getInstance().repaint().refreshStudentView(group, student);
    }

    @Override
    public String getHistoryDescription() { return "renamed student to \"" + newName + "\""; }
}
