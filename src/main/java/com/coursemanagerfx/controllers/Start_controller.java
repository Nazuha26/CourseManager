package com.coursemanagerfx.controllers;

import com.coursemanagerfx.CM_HELPER;
import com.coursemanagerfx.custom_ui.GradientBackground;
import com.coursemanagerfx.dialogs.NewCourseDialog_controller;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

import static com.coursemanagerfx.CM_HELPER.*;

public class Start_controller {
    @FXML private BorderPane main_start;
    @FXML private Label labelTitle;

    private Stage stage;

    private double xOffset = 0;     // ← для передвижения окна
    private double yOffset = 0;     // ← для передвижения окна

    // === дополнительные поля для выбранного курса ===
    private String courseName;
    private File selectedCourseFile;
    // ================================================

    public void setStage(Stage stage) {
        this.stage = stage;
        labelTitle.setText("Welcome to CourseManagerFX – v" + CUR_VERSION);

        // === СКРУГЛЕННЫЕ КРАЯ ===
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(main_start.widthProperty());
        clip.heightProperty().bind(main_start.heightProperty());
        clip.setArcWidth(20);         // ← стартовое значение
        clip.setArcHeight(20);        // ← стартовое значение
        main_start.setClip(clip);     // ← устанавливаем новые края
        // ========================

        // Слушатель восстановления окна
        stage.iconifiedProperty().addListener((obs, wasMinimized, isNowMinimized) -> {
            if (!isNowMinimized) {
                animateRestoration(stage);
            }
        });
    }

    @FXML
    private void initialize() {
        new GradientBackground(main_start);
    }

    @FXML
    private void btnClose() {
        actionClose(stage, null);
    }

    // === ПЕРЕМЕЩЕНИЕ (ДРАГ) ОКНА ===
    @FXML private void onTitleBarPressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }
    @FXML private void onTitleBarDragged(MouseEvent event) {
        CM_HELPER.onDragged(event, stage, xOffset, yOffset);
    }
    // ===============================

    @FXML
    private void btnMinimize() {
        actionMinimize(stage);
    }

    @FXML
    private void btnOpenCourse() {
        File file = CM_HELPER.showOpenCourseDialog(stage);
        if (file == null) return;                 // пользователь нажал Cancel

        courseName         = file.getName().replace(".cman", "");
        selectedCourseFile = file;

        actionClose(stage, this::openMainWindow);
    }

    @FXML
    private void btnNewCourse() {
        NewCourseDialog_controller res = CM_HELPER.showNewCourseDialog(stage);
        if (res == null || res.getCourseName() == null) {   // Cancel
            return;
        }

        courseName         = res.getCourseName();
        selectedCourseFile = res.getNewCourseFile();

        // Закрываем стартовое окно и открываем главное
        actionClose(stage, this::openMainWindow);
    }

    // === *ОБЕРТКА* ДЛЯ МЕТОДА ОТКРЫТИЯ ГЛАВНОГО ОКНА ПРОГРАММЫ ===
    private void openMainWindow() {
        try {
            CM_HELPER.openMainWindow(courseName, selectedCourseFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}