package com.coursemanagerfx.dialogs;

import com.coursemanagerfx.CM_HELPER;
import com.coursemanagerfx.logic.utilitys.UpdateUtility;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import static com.coursemanagerfx.CM_HELPER.actionClose;

public class About_controller {

    @FXML private BorderPane rootPane;
    @FXML private Label lblVersion;
    @FXML private Label lblNoUpdates;

    private Stage stage;
    private double xOffset, yOffset;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        lblVersion.setText("Version " + CM_HELPER.CUR_VERSION);
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(rootPane.widthProperty());
        clip.heightProperty().bind(rootPane.heightProperty());
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        rootPane.setClip(clip);
    }

    @FXML
    private void btnClose() {
        actionClose(stage, null);
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

    @FXML
    private void onTitleBarPressed(javafx.scene.input.MouseEvent e) {
        xOffset = e.getSceneX();
        yOffset = e.getSceneY();
    }

    @FXML
    private void onTitleBarDragged(javafx.scene.input.MouseEvent e) {
        CM_HELPER.onDragged(e, stage, xOffset, yOffset);
    }
}
