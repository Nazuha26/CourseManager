package com.coursemanagerfx.dialogs.password;

import com.coursemanagerfx.CM_HELPER;
import com.coursemanagerfx.dialogs.ConfirmDialogType;
import com.coursemanagerfx.logic.security.CmanSecurityParser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.coursemanagerfx.CM_HELPER.actionClose;

public class InputPass_controller {
    @FXML private BorderPane rootPane;
    @FXML private PasswordField passField;
    @FXML private Label infoLabel;
    private Stage stage;

    private String inputPassword;
    public String getInputPassword() {
        return inputPassword;
    }

    private File file;
    public void setFile(File file) {
        this.file = file;
    }

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

        // После загрузки интерфейса фокусируем поле ввода
        Platform.runLater(() -> passField.requestFocus());
    }

    @FXML
    private void btnClose() {
        actionClose(stage, () -> Platform.runLater(Platform::exit));
    }

    @FXML
    private void btnConfirm() {
        infoLabel.setText("");
        infoLabel.setManaged(false);
        Platform.runLater(() -> stage.sizeToScene());
        String rawPassword = passField.getText();
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            infoLabel.setText("Field cannot be empty!");
            infoLabel.setStyle("-fx-text-fill: #BC0000FF;");
            infoLabel.setManaged(true);
            Platform.runLater(() -> stage.sizeToScene());
            return;
        }

        if (!CmanSecurityParser.tryParse(file, rawPassword)) {
            CM_HELPER.showConfirmDialog(stage, ConfirmDialogType.ERROR,
                    "Wrong password", "You entered a wrong password.\nPlease try again.");
        } else {
            inputPassword = rawPassword;
            String newName = file.getName().replace(".cman", "");
            // фиксируем последний открытый курс
            try (FileWriter w = new FileWriter(CM_HELPER.FIRST_RUN_FILE)) {
                w.write(newName);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            actionClose(stage, null);
        }
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
