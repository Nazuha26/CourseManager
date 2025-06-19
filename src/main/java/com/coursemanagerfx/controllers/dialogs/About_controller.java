package com.coursemanagerfx.controllers.dialogs;

import com.coursemanagerfx.AppConstants;
import com.coursemanagerfx.animations.HideAnimation;
import com.coursemanagerfx.controllers.StageAttachable;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX_type;
import com.coursemanagerfx.logic.Actions;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class About_controller implements StageAttachable {

    // ========== FXML ==========
    @FXML private BorderPane rootPane;
    @FXML private HBox titleBar;
    @FXML private Label lblVersion;
    @FXML private Label lblEmail;
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

    @FXML private void initialize() {
        lblVersion.setText("v" + AppConstants.APP_VERSION);
    }

    @FXML private void onLblEmailHover() {
        lblEmail.setCursor(Cursor.HAND);
        lblEmail.setStyle("-fx-text-fill: #005ec9;");    // hovered color
    }

    @FXML private void onLblEmailUnhover() {
        lblEmail.setCursor(Cursor.DEFAULT);
        lblEmail.setStyle("-fx-text-fill: #0077ff;");    // default color
    }

    @FXML private void onLblEmailClicked() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(lblEmail.getText());
        clipboard.setContent(content);

        AlertFX.showNotification(
                stage.getScene().getWindow(),
                AlertFX_type.INFO,
                "Copied",
                "Email successfully copied to clipboard",
                true
        );
    }

    @FXML
    private void btnClose() {
        HideAnimation.play(stage, stage::close);
    }

    @FXML
    private void btnCheckForUpdates() {
        Actions.getInstance().loadingActions().updateWindow(true);
    }
}