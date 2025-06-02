package com.coursemanagerfx.controllers.dialogs.password;

import com.coursemanagerfx.AppConstants;
import com.coursemanagerfx.animations.HideAnimation;
import com.coursemanagerfx.controllers.StageAttachable;
import com.coursemanagerfx.controllers.dialogs.NewCourseDialog_controller;
import com.coursemanagerfx.logic.security.CmanSecurityUtility;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class GeneratedPass_controller implements StageAttachable {

    // ========== FXML ==========
    @FXML private BorderPane rootPane;
    @FXML private HBox titleBar;
    @FXML private Label lblPassword;
    @FXML private Label lblCopied;
    // ========== FXML ==========

    private boolean copied = false;

    private Stage stage;

    private String password;

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
        password = CmanSecurityUtility.generatePassword();
        NewCourseDialog_controller.setGeneratedPassword(password);
        lblPassword.setText(password);
    }

    @FXML
    private void btnClose() {
        if (!copied) {
            lblCopied.setText("You haven’t copied the password yet!");
            lblCopied.setStyle("-fx-text-fill: " + AppConstants.ColorConstants.toCssRGB(AppConstants.ColorConstants.ERROR_COLOUR) + ";");
            lblCopied.setManaged(true);
            stage.sizeToScene();
            return;
        }
        HideAnimation.play(stage, stage::close);
    }

    @FXML
    private void btnCopy() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(password);
        clipboard.setContent(content);

        copied = true;
        lblCopied.setText("Successfully copied");
        lblCopied.setStyle("-fx-text-fill: " + AppConstants.ColorConstants.toCssRGB(AppConstants.ColorConstants.SUCCESS_COLOUR) + ";");
        lblCopied.setManaged(true);
        stage.sizeToScene();
    }
}
