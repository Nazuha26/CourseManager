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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Window;

public class StudentPanel_controller {
    @FXML private BorderPane rootPane;
    @FXML private Button btnStudent;
    @FXML private ContextMenu ctxStudentMenu;
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
            if (!rootPane.getStyleClass().contains("search-background"))
                rootPane.getStyleClass().add("search-background");
        } else {
            rootPane.getStyleClass().remove("search-background");
        }
    }

    @FXML
    private void onStudent()
        { Actions.getInstance().select().selectStudentPanel(student); }

    /* show context menu */
    @FXML private void handleContextMenu(MouseEvent e) {
        if (e.isSecondaryButtonDown()) {
            Actions.getInstance().select().selectStudentPanel(student);
            ctxStudentMenu.show(btnStudent, e.getScreenX(), e.getScreenY());
        }
    }

    /* student RENAMING */
    @FXML private void miRename() {
        Window owner = ctrl.getStage().getScene().getWindow();

        String oldName = student.getName();
        String raw = ShowDialogUtility.showInputDialog(
                owner,
                "Rename student",
                "Enter new name:",
                oldName
        );
        if (raw == null) return;

        String newName = raw.trim().replaceAll("\\s+", " ");
        if (newName.split(" ").length != 3) {
            AlertFX.showNotification(
                    owner,
                    AlertFX_type.WARNING,
                    "Invalid input",
                    "Enter full name in format:\nFirstname Lastname Patronymic",
                    true
            );
            return;
        }

        if (!newName.equals(oldName)) {
            RenameStudentCommand cmd = new RenameStudentCommand(
                    Actions.getInstance().select().getSelectedGroup(),
                    student,
                    newName
            );
            cmd.execute();
            Actions.getInstance().undoRedo().addCommand(cmd);
        }
    }

    /* student DELETING */
    @FXML private void miDelete() {

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

    /* for delete key */
    /*@FXML private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE) {
            miDelete();
        }
    }*/
}
