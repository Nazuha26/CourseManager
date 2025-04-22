package com.coursemanagerfx.logic.commands;

import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.basic.Student;
import com.coursemanagerfx.controllers.Main_controller;
import com.coursemanagerfx.logic.utilitys.HistoryUtility;

public class DeleteStudentCommand implements Command {
    private Group group;
    private Student student;
    private Main_controller mainController;

    public DeleteStudentCommand(Group group, Student student, Main_controller mainController) {
        this.group = group;
        this.student = student;
        this.mainController = mainController;
    }

    @Override
    public void execute(boolean isRedo) {
        group.getStudents().remove(student);
        mainController.displayStudents(group);

        if (!group.getStudents().isEmpty()) {
            mainController.selectGroupAndStudent(group, group.getStudents().getFirst().getID());
        } else {
            mainController.selectGroupAndStudent(group, -1); // ничего не выделяем
        }

        if (!isRedo) {
            HistoryUtility.setHistory(mainController.getRichTxtPaneHistory(), mainController.getLblCurHistory(),
                    HistoryUtility.Types.SUCCESS, "Deleted student \"" + student.getName() + "\"");
        }
    }

    @Override
    public void undo() {
        group.getStudents().add(student);
        mainController.displayStudents(group);
        mainController.selectGroupAndStudent(group, student.getID());
    }

    @Override
    public String getDescription() {
        return "deleted student \"" + student.getName() + "\"";
    }
}
