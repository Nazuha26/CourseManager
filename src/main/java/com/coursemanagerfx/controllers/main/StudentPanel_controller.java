package com.coursemanagerfx.controllers.main;

import com.coursemanagerfx.controllers.dialogs.alert.AlertFX;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX_type;
import com.coursemanagerfx.logic.Actions;
import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.basic.Student;
import com.coursemanagerfx.logic.commands.student_comms.DeleteStudentCommand;
import com.coursemanagerfx.logic.commands.student_comms.RenameStudentCommand;
import com.coursemanagerfx.logic.utilities.show.ShowDialogUtility;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

public class StudentPanel_controller {
    @FXML private BorderPane panelBg;
    @FXML private Button btnStudent;
    @FXML private Label lblStdNum;
    @FXML private MenuItem miDelete;

    private Student student;
    private Main_controller ctrl;

    /** Вызывается из Main_controller сразу после загрузки FXML: */
    public void setCtx(Main_controller controller, Student student, int displayNumber, boolean searched) {
        this.ctrl = controller;
        this.student = student;

        lblStdNum.setText(String.valueOf(displayNumber));
        btnStudent.setText(student.getName());
        btnStudent.setUserData(student);

        Group selectedGroup = Actions.getInstance().select().getSelectedGroup();
        miDelete.setDisable(selectedGroup == null || selectedGroup.getStudents().size() <= 1);

        if (searched) {
            if (!panelBg.getStyleClass().contains("search-background"))
                panelBg.getStyleClass().add("search-background");
        } else {
            panelBg.getStyleClass().remove("search-background");
        }
    }

    @FXML
    private void onStudent()
        { Actions.getInstance().select().selectStudentPanel(student); }

    // Переименование студента
    @FXML
    private void miRename() {
        String raw = ShowDialogUtility.showInputDialog(
                ctrl.getStage().getScene().getWindow(),
                "Rename student",
                "Enter new name:",
                student.getName()
        );
        if (raw == null) return;

        String newName = raw.trim().replaceAll("\\s+", " ");
        RenameStudentCommand cmd = new RenameStudentCommand(
                Actions.getInstance().select().getSelectedGroup(),
                student,
                newName
        );
        cmd.execute();
        Actions.getInstance().undoRedo().addCommand(cmd);
    }

    // Удаление студента
    @FXML
    private void miDelete() {

        boolean confirmed = AlertFX.showConfirm(
                ctrl.getStage().getScene().getWindow(),
                AlertFX_type.INFO,
                "Deleting the student.",
                "Are you sure you want to remove \"" + student.getName() + "\"?"
        );
        if (!confirmed) return;

        DeleteStudentCommand cmd = new DeleteStudentCommand(
                Actions.getInstance().select().getSelectedGroup(),
                student
        );
        cmd.execute();
        Actions.getInstance().undoRedo().addCommand(cmd);
    }

    @FXML
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE) {
            miDelete();
        }
    }
}
