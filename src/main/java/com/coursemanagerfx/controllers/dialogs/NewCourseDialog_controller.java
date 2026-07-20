/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.controllers.dialogs;

import com.coursemanagerfx.AppConstants;
import com.coursemanagerfx.animations.HideAnimation;
import com.coursemanagerfx.controllers.StageAttachable;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX;
import com.coursemanagerfx.controllers.dialogs.alert.AlertMessageType;
import com.coursemanagerfx.controllers.dialogs.exceptions.CourseCreationException;
import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.utilities.security.CmanSecurityUtility;
import com.coursemanagerfx.logic.utilities.view.ShowDialogUtility;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;

import static com.coursemanagerfx.AppConstants.*;

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

    private File createdCourseFile;
    public File getCreatedCourseFile() {
        return createdCourseFile;
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
        int courseNumber = (spinCourseNumber.getValue());
        String courseName = "Рейтинг X-го курсу".replace("X", String.valueOf(courseNumber));

        File newCourseFile = COURSES_PATH.resolve(courseName + ".cman").toFile();

        try {
            if (Files.exists(newCourseFile.toPath())) {
                errorLabel.setText("A course with this name already exists.");
                errorLabel.setStyle("-fx-text-fill: " + ColorConstants.toCssRGB(ColorConstants.ERROR_COLOUR) + ";");
                errorLabel.setManaged(true);
                Platform.runLater(stage::sizeToScene);
                return;
            }

            // create empty *.cman
            int groupsCount = spinGroupsCount.getValue();
            Group[] groups = new Group[groupsCount];
            for (int i = 0; i < groupsCount; i++)
                groups[i] = new Group();

            char[] seedPhrase = ShowDialogUtility.showGeneratedSeedPhraseDialog(
                    stage.getScene().getWindow());
            if (seedPhrase == null) return;
            try {
                CmanSecurityUtility.createSecureFile(groups, newCourseFile, seedPhrase);
            } catch (Exception e) {
                errorLabel.setText("Could not create course. Please try again.");
                errorLabel.setStyle("-fx-text-fill: " + ColorConstants.toCssRGB(ColorConstants.ERROR_COLOUR) + ";");
                errorLabel.setManaged(true);
                Platform.runLater(stage::sizeToScene);

                AlertFX.showNotification(
                        AlertMessageType.ERROR,
                        "Error Creating Course",
                        "Message: " + e
                );
                return;
                //throw new CourseFileCreateException("Failed to create encrypted course file", ex);
            } finally {
                Arrays.fill(seedPhrase, '\0');
            }
            createdCourseFile = newCourseFile.getAbsoluteFile();
            HideAnimation.play(stage, stage::close);
        } catch (Exception e) {
            errorLabel.setText("Unexpected error");
            errorLabel.setStyle("-fx-text-fill: " + ColorConstants.toCssRGB(ColorConstants.ERROR_COLOUR) + ";");
            errorLabel.setManaged(true);
            Platform.runLater(stage::sizeToScene);

            AlertFX.showNotification(
                    AlertMessageType.ERROR,
                    "Unexpected Error Creating Course",
                    "Message: " + e
            );
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
