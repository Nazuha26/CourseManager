package com.coursemanagerfx.controllers.main;

import com.coursemanagerfx.animations.WindowOutAnimation;
import com.coursemanagerfx.controllers.StageAttachable;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX_type;
import com.coursemanagerfx.custom_ui.GradientBackground;
import com.coursemanagerfx.logic.security.CmanSecurityUtility;
import com.coursemanagerfx.logic.utilities.AppUtility;
import com.coursemanagerfx.logic.utilities.config_api.ConfigManager;
import com.coursemanagerfx.logic.utilities.show.ShowDialogUtility;
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
        WindowOutAnimation.play(
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

        String password = ShowDialogUtility.showCheckPasswordDialog(owner);
        if (password == null) return;

        String courseName;
        try {
            CmanSecurityUtility.readSecureFile(file, password);

            // ======== GET COURSE NAME ==========
            courseName = file.getName();
            if (courseName.endsWith(".cman"))
                courseName = courseName.substring(0, courseName.length() - 5);
        } catch (Exception e) {
            ConfigManager.setOpenCourse("none");
            AlertFX.showNotification(owner,
                    AlertFX_type.ERROR,
                    "Unable to open the course file",
                    "The password is incorrect or the file is corrupted. Please try again.",
                    true);
            return;
        }

        // ======= SAVE COURSE NAME TO CONFIG FILE ========
        ConfigManager.setOpenCourse(courseName);

        try {
            AppUtility.startRestartAppScript();
        } catch (Exception e) {
            throw new RuntimeException("Restarting application failed.", e);
        }

        WindowOutAnimation.play(
                this,
                rootPane.getWidth(),
                rootPane.getHeight(),
                SW_ANIM_STRIPE_COUNT,
                Duration.seconds(1),
                stage::close
        );
    }

    @FXML
    private void btnNewCourse() {
        Window owner = stage.getScene().getWindow();
        boolean success = ShowDialogUtility.showNewCourseDialog(owner);

        if (success) {
            /*AlertFX.showNotification(owner, AlertFX_type.INFO,
                    "Course created successfully",
                    "To apply the changes, please restart the application.",
                    true);*/
            try {
                AppUtility.startRestartAppScript();
            } catch (Exception e) {
                throw new RuntimeException("Restarting application failed.", e);
            }
            WindowOutAnimation.play(
                    this,
                    rootPane.getWidth(),
                    rootPane.getHeight(),
                    SW_ANIM_STRIPE_COUNT,
                    Duration.seconds(1),
                    stage::close
            );
        }
    }
}