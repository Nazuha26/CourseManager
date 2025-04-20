package com.coursemanagerfx.logic.commands;

import com.coursemanagerfx.controllers.Main_controller;
import com.coursemanagerfx.logic.Group;
import com.coursemanagerfx.logic.Student;
import com.coursemanagerfx.logic.StudentEvent;
import com.coursemanagerfx.logic.utilitys.HistoryUtility;

public class AddEventCommand implements Command {
    private Group group;
    private Student student;
    private StudentEvent event;
    private Main_controller mainController;

    public AddEventCommand(Group group, Student student, StudentEvent event, Main_controller mainController) {
        this.group = group;
        this.student = student;
        this.event = event;
        this.mainController = mainController;
    }

    @Override
    public void execute(boolean isRedo) {
        student.getEvents().add(event);
        mainController.displayStudents(group);
        mainController.selectGroupAndStudent(group, student.getID());

        if (!isRedo) {
            String shortDesc = event.getDescription().length() > 10
                    ? event.getDescription().substring(0, 10) + "..."
                    : event.getDescription();
            HistoryUtility.setHistory(mainController.getRichTxtPaneHistory(), mainController.getLblCurHistory(),
                    HistoryUtility.Types.SUCCESS, "Added event \"" + shortDesc + "\"");
        }
    }

    @Override
    public void undo() {
        student.getEvents().remove(event);
        mainController.displayStudents(group);
        mainController.selectGroupAndStudent(group, student.getID());
    }

    @Override
    public String getDescription() {
        String shortDesc = event.getDescription().length() > 10
                ? event.getDescription().substring(0, 10) + "..."
                : event.getDescription();
        return "added event \"" + shortDesc + "\"";
    }
}
