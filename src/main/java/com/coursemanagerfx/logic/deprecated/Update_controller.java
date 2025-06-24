package com.coursemanagerfx.logic.deprecated;

import com.coursemanagerfx.animations.HideAnimation;
import com.coursemanagerfx.controllers.StageAttachable;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Update_controller implements StageAttachable {

    // ========== FXML ==========
    @FXML private BorderPane rootPane;
    @FXML private HBox titleBar;
    @FXML private Label lblMain;
    @FXML private Label lblAdditional;
    @FXML private ProgressBar installingProgressBar;
    // ========== FXML ==========

    public Label getLblMain() {
        return lblMain;
    }
    public Label getLblAdditional() {
        return lblAdditional;
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
    private void initialize() { }

    @FXML
    private void btnConfirm() {
        installingProgressBar.setVisible(true);
        //UpdateUtility.downloadAndInstallUpdate(installingProgressBar);
    }

    @FXML
    private void btnClose() {
        System.out.println("Update canceled...");
        HideAnimation.play(stage, stage::close);
    }

    @FXML
    private void btnCancel() {
        System.out.println("Update canceled...");
        HideAnimation.play(stage, stage::close);
    }
}
