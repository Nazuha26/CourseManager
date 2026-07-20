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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddStudentCommand implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddStudentCommand.class);

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
        LOGGER.debug("Added student: name='{}', globalId={}",
                student.getName(), student.getStudentID());
    }

    @Override
    public void undo() {
        group.getStudents().remove(student);
        Actions.getInstance().select().selectFirstOrClear(group);
        LOGGER.debug("Undid adding student: name='{}', globalId={}",
                student.getName(), student.getStudentID());
    }

    @Override
    public String getHistoryDescription() {
        return "added student \"" + student.getName() + "\"";
    }
}
