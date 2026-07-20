/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.controllers.main;

import com.coursemanagerfx.animations.WindowBlindsOutAnimation;
import com.coursemanagerfx.controllers.StageAttachable;
import com.coursemanagerfx.custom_ui.GradientBackground;
import com.coursemanagerfx.logic.utilities.view.ShowDialogUtility;
import com.coursemanagerfx.logic.utilities.view.ShowWindowUtility;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.File;

public class Start_controller implements StageAttachable {

    public static final int SW_ANIM_STRIPE_COUNT = 20;  // start window

    // ========== FXML ==========
    @FXML private BorderPane rootPane;
    @FXML private HBox titleBar;
    @FXML private Label labelTitle;
    // ========== FXML ==========

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


    public Label getLabelTitle() {
        return labelTitle;
    }

    @FXML
    private void initialize() {
        new GradientBackground(rootPane);
    }

    @FXML
    private void btnClose() {
        WindowBlindsOutAnimation.play(
                this,
                rootPane.getWidth(),
                rootPane.getHeight(),
                SW_ANIM_STRIPE_COUNT,
                Duration.seconds(1),
                stage::close
        );
    }

    @FXML
    private void btnMinimize() {
        stage.setIconified(true);
    }

    @FXML private void btnOpenCourse() {
        Window owner = stage.getScene().getWindow();
        File file = ShowDialogUtility.showOpenCourse(owner);
        if (file == null) return;
        openCourse(file, owner);
    }

    @FXML
    private void btnNewCourse() {
        Window owner = stage.getScene().getWindow();
        File createdCourse = ShowDialogUtility.showNewCourseDialog(owner);
        if (createdCourse != null) openCourse(createdCourse, owner);
    }

    /* === CORE === */

    private void openCourse(File file, Window passwordOwner) {
        if (!ShowWindowUtility.showMainWindow(file, passwordOwner)) return;

        WindowBlindsOutAnimation.play(
                this,
                rootPane.getWidth(),
                rootPane.getHeight(),
                SW_ANIM_STRIPE_COUNT,
                Duration.seconds(1),
                stage::close
        );
    }
}
