package com.coursemanagerfx.logic.commands.student_comms;

import com.coursemanagerfx.controllers.Main_controller;
import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.basic.Student;
import com.coursemanagerfx.logic.commands.Command;
import com.coursemanagerfx.logic.utilitys.HistoryUtility;

public class RenameStudentCommand implements Command {
    private Group group;
    private Student student;
    private String oldName;
    private String newName;
    private Main_controller mainController;

    public RenameStudentCommand(Group group, Student student, String newName, Main_controller mainController) {
        this.group = group;
        this.student = student;
        this.oldName = student.getName();
        this.newName = newName;
        this.mainController = mainController;
    }

    @Override
    public void execute(boolean isRedo) {
        student.setName(newName);
        mainController.displayStudents(group);
        mainController.selectGroupAndStudent(group, student.getID());

        if (!isRedo) {
            HistoryUtility.setHistory(mainController.getRichTxtPaneHistory(), mainController.getLblCurHistory(),
                    HistoryUtility.Types.SUCCESS, "Renamed student to \"" + student.getName() + "\"");
        }
    }

    @Override
    public void undo() {
        student.setName(oldName);
        mainController.displayStudents(group);
        mainController.selectGroupAndStudent(group, student.getID());
    }

    @Override
    public String getDescription() {
        return "renamed student to \"" + student.getName() + "\"";
    }
}
