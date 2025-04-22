package com.coursemanagerfx.controllers;

import com.coursemanagerfx.dialogs.ConfirmDialogType;
import com.coursemanagerfx.logic.basic.Student;
import com.coursemanagerfx.logic.commands.DeleteStudentCommand;
import com.coursemanagerfx.logic.commands.RenameStudentCommand;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import static com.coursemanagerfx.CM_HELPER.*;

public class StudentPanel_controller {
    @FXML
    private Button btnStudent;
    public Button getBtnStudent() {
        return btnStudent;
    }

    private Student student;                 // ← ссылка на текущего студента
    private Main_controller mainController;   // ← ссылка на MainController

    public void setStudent(Student student) {
        this.student = student;
        btnStudent.setText(student.getName());
    }

    // Передаем ссылку на MainController, чтобы потом вызывать его методы
    public void setMainController(Main_controller controller) {
        this.mainController = controller;
    }

    // Переименование студента
    @FXML
    private void miRename() {
        String rawNewStudentName = showInputDialog(mainController.getMainPanel().getScene().getWindow(),
                "Rename student",
                "Enter new student name:");

        if (rawNewStudentName == null || rawNewStudentName.trim().isEmpty()) return; // ← пользователь нажал Cancel или оставил поле пустым

        String newStudentName = rawNewStudentName.trim().replaceAll("\\s+", " ");

        RenameStudentCommand cmd = new RenameStudentCommand(mainController.getCurrentGroup(), student, newStudentName, mainController);
        cmd.execute(false);
        mainController.addCommand(cmd);
    }

    // Удаление студента
    @FXML
    private void miDelete() {
        boolean confirmed = showConfirmDialog(
                mainController.getMainPanel().getScene().getWindow(),
                ConfirmDialogType.INFO,
                "Deleting the student.",
                "Are you sure you want to remove\nthe student \"" + student.getName() + "\"?"
        );

        if (confirmed) {
            DeleteStudentCommand cmd = new DeleteStudentCommand(mainController.getCurrentGroup(), student, mainController);
            cmd.execute(false);
            mainController.addCommand(cmd);
        }
    }

    @FXML
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE) {
            miDelete();
        }
    }
}
