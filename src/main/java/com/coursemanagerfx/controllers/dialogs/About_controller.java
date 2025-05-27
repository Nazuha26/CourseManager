package com.coursemanagerfx.controllers.dialogs;

import com.coursemanagerfx.Launcher;
import com.coursemanagerfx.animations.HideAnimation;
import com.coursemanagerfx.controllers.StageAttachable;
import com.coursemanagerfx.logic.utilities.UpdateUtility;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class About_controller implements StageAttachable {

    // ========== FXML ==========
    @FXML private BorderPane rootPane;
    @FXML private HBox titleBar;
    @FXML private Label lblVersion;
    @FXML private Label lblNoUpdates;
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
        lblVersion.setText("v" + Launcher.CUR_VERSION);
    }

    @FXML
    private void btnClose() {
        HideAnimation.play(stage, stage::close);
    }

    @FXML
    private void btnCheckForUpdates() {
        lblNoUpdates.setManaged(false);  // сбросим на случай повторного клика
        String new_version = UpdateUtility.checkForUpdates(stage.getScene().getWindow());
        if (!new_version.equals("-1")) {
            UpdateUtility.showUpdateDialog(stage.getScene().getWindow());
        } else {
            lblNoUpdates.setManaged(true);
            Platform.runLater(stage::sizeToScene);
        }
    }
}
