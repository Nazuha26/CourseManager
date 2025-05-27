package com.coursemanagerfx.controllers.main;

import com.coursemanagerfx.CM_HELPER;
import com.coursemanagerfx.Launcher;
import com.coursemanagerfx.animations.HideAnimation;
import com.coursemanagerfx.animations.WindowOutAnimation;
import com.coursemanagerfx.controllers.StageAttachable;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX_type;
import com.coursemanagerfx.custom_ui.GradientBackground;
import com.coursemanagerfx.logic.security.CmanSecurityUtility;
import com.coursemanagerfx.logic.utilities.show.ShowDialogUtility;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @FXML
    private void initialize() {
        new GradientBackground(rootPane);
        labelTitle.setText("Welcome to CourseManagerFX – v" + Launcher.CUR_VERSION);
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

    @FXML
    private void btnOpenCourse() {
        Window owner = stage.getScene().getWindow();
        File file = ShowDialogUtility.showOpenCourse(owner);
        if (file == null) return;                // Cancel

        String password = ShowDialogUtility.showCheckPasswordDialog(owner);
        if (password == null) return;            // Cancel
        try {
            CmanSecurityUtility.readSecureFile(file, password);
        } catch (Exception e) {
            AlertFX.showNotification(owner,
                    AlertFX_type.ERROR,
                    "Unable to open the course file",
                    "The password is incorrect or the file is corrupted. Please try again.",
                    true);
            return;
        }

        // remember the opened course
        try (FileWriter w = new FileWriter(CM_HELPER.LAST_RUN_FILE)) {
            String baseName = file.getName();
            if (baseName.endsWith(".cman")) {
                baseName = baseName.substring(0, baseName.length() - 5);
            }

            Pattern p = Pattern.compile("Рейтинг (\\d+)-го курсу");
            Matcher m = p.matcher(baseName);

            if (m.matches()) {
                int courseNum = Integer.parseInt(m.group(1));
                //CM_HELPER.setCourseNumber(courseNum);
                w.write(String.valueOf(courseNum));
            } else {
                w.write("0");
            }
        } catch (IOException ex) {
            AlertFX.showNotification(owner,
                    AlertFX_type.ERROR,
                    "Failed to save course information",
                    "Please try again or contact support if the issue persists.",
                    true);
            return;
        }

        AlertFX.showNotification(owner, AlertFX_type.INFO,
                "Course chose successfully",
                "To apply the changes, please restart the application.",
                true);
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
            AlertFX.showNotification(owner, AlertFX_type.INFO,
                    "Course created successfully",
                    "To apply the changes, please restart the application.",
                    true);
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