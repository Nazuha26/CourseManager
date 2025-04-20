package com.coursemanagerfx.dialogs;

import com.coursemanagerfx.CM_HELPER;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import static com.coursemanagerfx.CM_HELPER.actionClose;

public class ConfirmDialog_controller {
    private boolean confirmed = false;
    public boolean isConfirmed() {
        return confirmed;
    }

    @FXML private BorderPane rootPane;
    @FXML private Label labelTitle;
    @FXML private Label labelMain;
    @FXML private Label labelPrompt;
    @FXML private Button btnClose;
    @FXML private Button btnCancel;
    @FXML private Button btnConfirm;
    @FXML private ImageView iconType;

    private Stage stage;

    // Смещение для перетаскивания окна
    private double xOffset;
    private double yOffset;

    public ImageView getIconType() {
        return iconType;
    }
    public Label getLabelTitle() {
        return labelTitle;
    }
    public Label getLabelMain() {
        return labelMain;
    }
    public Label getLabelPrompt() {
        return labelPrompt;
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
    }

    @FXML
    private void btnConfirm() {
        confirmed = true;
        actionClose(stage, null);
    }

    @FXML
    private void btnClose() {
        actionClose(stage, null);
    }

    @FXML
    private void btnCancel() {
        actionClose(stage, null);
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
