package com.coursemanagerfx.logic.commands;

import com.coursemanagerfx.controllers.Main_controller;
import com.coursemanagerfx.logic.*;
import com.coursemanagerfx.logic.utilitys.HistoryUtility;

public class AddStudentCommand implements Command {
    private Group group;
    private String studentName;
    private Student student;
    private Main_controller mainController;

    public AddStudentCommand(Group group, String studentName, Main_controller mainController) {
        this.group = group;
        this.studentName = studentName;
        this.mainController = mainController;
    }

    @Override
    public void execute(boolean isRedo) {
        if (student == null) { // Создаем студента только при первом вызове команды
            int uniqueId = mainController.genUniqueStudentId();
            student = new Student(studentName, uniqueId);
        }
        group.getStudents().add(student);
        mainController.displayStudents(group);
        mainController.selectGroupAndStudent(group, student.getID());

        if (!isRedo) {
            HistoryUtility.setHistory(mainController.getRichTxtPaneHistory(), mainController.getLblCurHistory(),
                    HistoryUtility.Types.SUCCESS, "Added student \"" + student.getName() + "\"");
        }
    }

    @Override
    public void undo() {
        // Удаляем именно тот объект
        group.getStudents().remove(student);

        // Перерисовываем список
        mainController.displayStudents(group);

        // Если после удаления остались студенты — выделяем первого
        if (!group.getStudents().isEmpty()) {
            int firstId = group.getStudents().getFirst().getID();
            mainController.selectGroupAndStudent(group, firstId);
        }
        // иначе — оставляем пустую форму (displayStudents сам покажет “нет студентов”)
    }

    @Override
    public String getDescription() {
        return "added student \"" + student.getName() + "\"";
    }
}
