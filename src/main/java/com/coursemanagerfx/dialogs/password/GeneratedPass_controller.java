package com.coursemanagerfx.dialogs.password;

import com.coursemanagerfx.CM_HELPER;
import com.coursemanagerfx.logic.security.CmanSecurity;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import static com.coursemanagerfx.CM_HELPER.actionClose;

public class GeneratedPass_controller {
    @FXML private BorderPane rootPane;
    @FXML private Label lblPassword;
    @FXML private Label lblCopied;

    private boolean copied = false;

    private Stage stage;

    private static String generatedPassword;
    public static String getGeneratedPassword() {
        return generatedPassword;
    }

    // Смещение для перетаскивания окна
    private double xOffset;
    private double yOffset;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        String password = CmanSecurity.generatePassword();
        //CM_HELPER.setPassword(password);
        generatedPassword = password;
        lblPassword.setText(password);

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
    private void btnClose() {
        if (!copied) {
            lblCopied.setText("You haven’t copied the password yet!");
            lblCopied.setStyle("-fx-text-fill: #BC0000FF;");
            lblCopied.setManaged(true);
            Platform.runLater(() -> stage.sizeToScene());
            return;
        }
        actionClose(stage, null);
    }

    @FXML
    private void btnCopy() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(generatedPassword);
        clipboard.setContent(content);

        copied = true;
        lblCopied.setText("Successfully copied");
        lblCopied.setStyle("-fx-text-fill: #00bc3c;");
        lblCopied.setManaged(true);
        Platform.runLater(() -> stage.sizeToScene());
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
