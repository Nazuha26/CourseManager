package com.coursemanagerfx.dialogs;

import com.coursemanagerfx.CM_HELPER;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class InputDialog_controller {

    @FXML private BorderPane rootPane;
    @FXML private Label labelTitle;
    @FXML private Label labelPrompt;
    @FXML private Label errorLabel;
    @FXML private TextField textField;
    @FXML private Button btnClose;
    @FXML private Button btnCancel;
    @FXML private Button btnConfirm;

    private Stage stage;
    private String inputText;

    // Смещение для перетаскивания окна
    private double xOffset;
    private double yOffset;

    public Label getLabelPrompt() {
        return labelPrompt;
    }
    public Label getLabelTitle() {
        return labelTitle;
    }

    public String getInputText() {
        return inputText;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        // === СКРУГЛЕННЫЕ КРАЯ ===
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(rootPane.widthProperty());
        clip.heightProperty().bind(rootPane.heightProperty());
        clip.setArcWidth(20);         // ← стартовое значение
        clip.setArcHeight(20);        // ← стартовое значение
        rootPane.setClip(clip);       // ← устанавливаем новые края
        // ========================

        // После загрузки интерфейса фокусируем поле ввода
        Platform.runLater(() -> textField.requestFocus());
        errorLabel.setManaged(false);
    }

    @FXML
    private void btnConfirm() {
        String text = textField.getText();
        if (text == null || text.trim().isEmpty()) {
            errorLabel.setText("Field cannot be empty!");
            errorLabel.setManaged(true);
            // Пересчёт размеров окна, чтобы учесть появление ошибки
            Platform.runLater(() -> stage.sizeToScene());
            return;
        }
        inputText = text.trim();
        errorLabel.setText("");
        errorLabel.setManaged(false);
        CM_HELPER.actionClose(stage, null);
    }

    @FXML
    private void btnClose() {
        cancelAction();
    }

    @FXML
    private void btnCancel() {
        cancelAction();
    }

    private void cancelAction() {
        inputText = null;
        CM_HELPER.actionClose(stage, null);
    }

    @FXML
    private void onTitleBarPressed(javafx.scene.input.MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    private void onTitleBarDragged(javafx.scene.input.MouseEvent event) {
        CM_HELPER.onDragged(event, stage, xOffset, yOffset);
    }
}