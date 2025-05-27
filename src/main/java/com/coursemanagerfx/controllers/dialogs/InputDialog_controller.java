package com.coursemanagerfx.controllers.dialogs;

import com.coursemanagerfx.animations.HideAnimation;
import com.coursemanagerfx.controllers.StageAttachable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class InputDialog_controller implements StageAttachable {

    // ========== FXML ==========
    @FXML private BorderPane rootPane;
    @FXML private HBox titleBar;
    @FXML private Label labelTitle;
    @FXML private Label labelPrompt;
    @FXML private Label errorLabel;
    @FXML private TextField textField;
    // ========== FXML ==========

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

    private Stage stage;

    private String inputText;

    public Label getLabelPrompt() {
        return labelPrompt;
    }

    public Label getLabelTitle() {
        return labelTitle;
    }

    public TextField getTextField() {
        return textField;
    }

    public String getInputText() {
        return inputText;
    }

    @FXML
    private void initialize() {
        Platform.runLater(() -> textField.requestFocus());
    }

    @FXML
    public void btnConfirm() {
        String text = textField.getText();
        if (text == null || text.trim().isEmpty()) {
            errorLabel.setText("Field cannot be empty!");
            errorLabel.setManaged(true);
            stage.sizeToScene();
            return;
        }
        inputText = text.trim();
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