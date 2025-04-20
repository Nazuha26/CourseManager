package com.coursemanagerfx;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import static com.coursemanagerfx.CM_HELPER.*;

public class Launcher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
        if (!COURSES_DIR.exists()) COURSES_DIR.mkdirs();

        // Если файл FIRST_RUN существует и содержит имя курса, проверяем, что такой курс есть
        if (CM_HELPER.FIRST_RUN_FILE.exists()) {
            String courseName;
            try (BufferedReader reader = new BufferedReader(new FileReader(CM_HELPER.FIRST_RUN_FILE))) {
                courseName = reader.readLine();
            }
            if (courseName != null && !courseName.trim().isEmpty()) {
                File courseFile = new File(CM_HELPER.COURSES_DIR, courseName + ".cman");
                if (courseFile.exists()) {
                    openMainWindow(primaryStage, courseName, courseFile);   // ← открываем главное окно программы
                    return;
                }
            }
        }

        openStartWindow();
    }

    public static void main(String[] args) { launch(args); }
}