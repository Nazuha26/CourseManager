package com.coursemanagerfx.controllers.dialogs.alert;

import com.coursemanagerfx.animations.HideAnimation;
import com.coursemanagerfx.controllers.StageAttachable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class AlertFX_controller implements StageAttachable {
    private boolean confirmed = false;
    public boolean isConfirmed() {
        return confirmed;
    }

    // ========== FXML ==========
    @FXML private BorderPane rootPane;
    @FXML private HBox titleBar;
    @FXML private Label labelTitle;
    @FXML private Label labelMain;
    @FXML private Label labelPrompt;
    @FXML private Button btnClose;
    @FXML private Button btnCancel;
    @FXML private Button btnConfirm;
    @FXML private ImageView iconType;
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


    public ImageView getIconType() {
        return iconType;
    }
    public Label getLabelMain() {
        return labelMain;
    }
    public Label getLabelPrompt() {
        return labelPrompt;
    }
    public Button getBtnCancel() {
        return btnCancel;
    }

    @FXML private void initialize() { }

    @FXML
    private void btnConfirm() {
        confirmed = true;
        HideAnimation.play(stage, stage::close);
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
