/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.controllers.dialogs.password;

import com.coursemanagerfx.AppConstants;
import com.coursemanagerfx.animations.HideAnimation;
import com.coursemanagerfx.controllers.StageAttachable;
import com.coursemanagerfx.logic.utilities.security.SeedPhraseGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class GeneratedPass_controller implements StageAttachable {

    // ========== FXML ==========
    @FXML private BorderPane rootPane;
    @FXML private HBox titleBar;
    @FXML private Label lblPassword;
    @FXML private Label lblCopied;
    // ========== FXML ==========

    private boolean copied = false;

    private Stage stage;

    private char[] seedPhrase;

    public char[] takeSeedPhrase() {
        if (!copied || seedPhrase == null) return null;
        char[] result = seedPhrase;
        seedPhrase = null;
        return result;
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
        stage.setOnCloseRequest(event -> {
            if (!copied) {
                event.consume();
                showCopyWarning();
            } else {
                lblPassword.setText("");
            }
        });
    }
    // ===== IMPLEMENTED =====

    @FXML
    private void initialize() {
        seedPhrase = SeedPhraseGenerator.generate();
        lblPassword.setText(new String(seedPhrase));
    }

    @FXML
    private void btnClose() {
        if (!copied) {
            showCopyWarning();
            return;
        }
        lblPassword.setText("");
        HideAnimation.play(stage, stage::close);
    }

    @FXML
    private void btnCopy() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(new String(seedPhrase));
        clipboard.setContent(content);

        copied = true;

        lblCopied.setText("Successfully copied");
        lblCopied.setStyle("-fx-text-fill: " + AppConstants.ColorConstants.toCssRGB(AppConstants.ColorConstants.SUCCESS_COLOUR) + ";");
        lblCopied.setManaged(true);
        stage.sizeToScene();
    }

    private void showCopyWarning() {
        lblCopied.setText("You haven’t copied the seed phrase yet!");
        lblCopied.setStyle("-fx-text-fill: " + AppConstants.ColorConstants.toCssRGB(AppConstants.ColorConstants.ERROR_COLOUR) + ";");
        lblCopied.setManaged(true);
        stage.sizeToScene();
    }
}
