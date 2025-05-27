package com.coursemanagerfx.controllers.dialogs.password;

import com.coursemanagerfx.animations.HideAnimation;
import com.coursemanagerfx.controllers.StageAttachable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class InputPass_controller implements StageAttachable {
    // ========== FXML ==========
    @FXML private BorderPane rootPane;
    @FXML private HBox titleBar;
    @FXML private PasswordField passField;
    @FXML private Label infoLabel;
    // ========== FXML ==========

    private Stage stage;

    private String inputPassword;
    public String getInputPassword() {
        return inputPassword;
    }

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
        Platform.runLater(() -> passField.requestFocus());
    }

    @FXML
    private void btnClose() {
        HideAnimation.play(stage, stage::close);
    }

    @FXML
    private void btnConfirm() {
        infoLabel.setText("");
        infoLabel.setManaged(false);
        stage.sizeToScene();
        String rawPassword = passField.getText();
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            infoLabel.setText("Field cannot be empty!");
            infoLabel.setStyle("-fx-text-fill: #BC0000FF;");
            infoLabel.setManaged(true);
            stage.sizeToScene();
            return;
        }
        inputPassword = rawPassword;
        HideAnimation.play(stage, stage::close);
    }
}
