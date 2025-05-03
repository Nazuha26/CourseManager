package com.coursemanagerfx.dialogs;

import com.coursemanagerfx.CM_HELPER;
import com.coursemanagerfx.logic.utilitys.UpdateUtility;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import static com.coursemanagerfx.CM_HELPER.actionClose;

public class Update_controller {
    @FXML private BorderPane rootPane;

    @FXML private Label lblMain;
    @FXML private Label lblAdditional;
    public Label getLblMain() {
        return lblMain;
    }
    public Label getLblAdditional() {
        return lblAdditional;
    }


    @FXML private ProgressBar installingProgressBar;
    @FXML private Button btnClose;
    @FXML private Button btnCancel;
    @FXML private Button btnConfirm;

    private Stage stage;

    // Смещение для перетаскивания окна
    private double xOffset;
    private double yOffset;

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
        installingProgressBar.setVisible(true);
        UpdateUtility.downloadAndInstallUpdate(installingProgressBar);
    }

    @FXML
    private void btnClose() {
        System.out.println("Update canceled...");
        actionClose(stage, null);
    }

    @FXML
    private void btnCancel() {
        System.out.println("Update canceled...");
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
