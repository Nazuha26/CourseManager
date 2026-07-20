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
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.Arrays;

public class InputPass_controller implements StageAttachable {
    // ========== FXML ==========
    @FXML private BorderPane rootPane;
    @FXML private HBox titleBar;
    @FXML private PasswordField passField;
    @FXML private Label errorLabel;
    // ========== FXML ==========

    private Stage stage;

    private char[] inputSeedPhrase;
    public char[] takeSeedPhrase() {
        char[] result = inputSeedPhrase;
        inputSeedPhrase = null;
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
            if (inputSeedPhrase == null) passField.clear();
        });
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
        errorLabel.setText("");
        errorLabel.setManaged(false);
        stage.sizeToScene();
        String rawPhrase = passField.getText();
        if (rawPhrase == null || rawPhrase.trim().isEmpty()) {
            errorLabel.setText("Field cannot be empty!");
            errorLabel.setStyle("-fx-text-fill: " + AppConstants.ColorConstants.toCssRGB(AppConstants.ColorConstants.ERROR_COLOUR) + ";");
            errorLabel.setManaged(true);
            stage.sizeToScene();
            return;
        }

        char[] rawCharacters = rawPhrase.toCharArray();
        passField.clear();
        char[] normalized = SeedPhraseGenerator.normalize(rawCharacters);
        Arrays.fill(rawCharacters, '\0');
        if (!SeedPhraseGenerator.isValid(normalized)) {
            Arrays.fill(normalized, '\0');
            errorLabel.setText("Enter the five words generated for this course.");
            errorLabel.setStyle("-fx-text-fill: " + AppConstants.ColorConstants.toCssRGB(AppConstants.ColorConstants.ERROR_COLOUR) + ";");
            errorLabel.setManaged(true);
            stage.sizeToScene();
            return;
        }

        inputSeedPhrase = normalized;
        HideAnimation.play(stage, stage::close);
    }
}
