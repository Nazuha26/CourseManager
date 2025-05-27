package com.coursemanagerfx.controllers.dialogs;

import com.coursemanagerfx.CM_HELPER;
import com.coursemanagerfx.animations.HideAnimation;
import com.coursemanagerfx.controllers.StageAttachable;
import com.coursemanagerfx.controllers.dialogs.exceptions.CourseCreationException;
import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.security.CmanSecurityUtility;
import com.coursemanagerfx.logic.utilities.show.ShowDialogUtility;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.coursemanagerfx.CM_HELPER.*;

public class NewCourseDialog_controller implements StageAttachable {

    // ========== FXML ==========
    @FXML private BorderPane rootPane;
    @FXML private HBox titleBar;
    @FXML private Button btnClose;
    @FXML private Button btnCancel;
    @FXML private Button btnConfirm;
    @FXML private Label errorLabel;
    @FXML private Spinner<Integer> spinCourseNumber;
    @FXML private Spinner<Integer> spinGroupsCount;
    // ========== FXML ==========

    private boolean courseCreated = false;
    public boolean wasCourseCreated() {
        return courseCreated;
    }

    private Stage stage;

    // ===== IMPLEMENTED =====
    @Override
    public BorderPane getRootPane() {
        return rootPane;
    }
    @Override
    public HBox getTitleBar() {
        return titleBar;
    }
    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    // ===== IMPLEMENTED =====

    @FXML
    private void initialize() {
        // === SPINNERS SETUP ===
        SpinnerValueFactory<Integer> valueFactoryMark =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1, 1);
        spinGroupsCount.setValueFactory(valueFactoryMark);

        SpinnerValueFactory<Integer> valueFactoryCourseNumber =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 4, 1, 1);
        spinCourseNumber.setValueFactory(valueFactoryCourseNumber);
        // === SPINNERS SETUP ===

        Platform.runLater(() -> spinCourseNumber.requestFocus());
    }

    @FXML
    private void btnConfirm() {
        CM_HELPER.setCourseNumber(spinCourseNumber.getValue());
        String courseName = courseNameTemplate.replace("X", String.valueOf(CM_HELPER.getCourseNumber()));
        File newCourseFile = new File(COURSES_DIR, courseName + ".cman");

        try {
            // create empty *.cman
            int groupsCount = spinGroupsCount.getValue();
            Group[] groups = new Group[groupsCount];
            for (int i = 0; i < groupsCount; i++)
                groups[i] = new Group();
            ShowDialogUtility.showGeneratedPasswordDialog(stage.getScene().getWindow());
            try {
                CmanSecurityUtility.createSecureFile(groups, newCourseFile, CM_HELPER.getGeneratedPassword());
            } catch (Exception ex) {
                errorLabel.setText("Could not create course. Please try again.");
                errorLabel.setManaged(true);
                Platform.runLater(stage::sizeToScene);
                return;
                //throw new CourseFileCreateException("Failed to create encrypted course file", ex);
            }
            // remember the created course
            try (FileWriter w = new FileWriter(CM_HELPER.LAST_RUN_FILE)) {
                w.write(String.valueOf(CM_HELPER.getCourseNumber()));
            } catch (IOException ex) {
                errorLabel.setText("Failed to save last run course info. Please try again.");
                errorLabel.setManaged(true);
                Platform.runLater(stage::sizeToScene);
                return;
                //throw new LastRunFileWriteException("Failed to save last run course info", ex);
            }
            courseCreated = true;
            HideAnimation.play(stage, stage::close);
        } catch (Exception e) {
            errorLabel.setText("Unexpected error.");
            errorLabel.setManaged(true);
            Platform.runLater(stage::sizeToScene);
            throw new CourseCreationException("Unexpected error in NewCourseDialog", e);
        }
    }

    @FXML
    private void btnClose() {
        HideAnimation.play(stage, stage::close);
    }

    @FXML
    private void btnCancel() {
        HideAnimation.play(stage, stage::close);
    }
}
