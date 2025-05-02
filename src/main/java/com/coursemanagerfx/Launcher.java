package com.coursemanagerfx;

import com.coursemanagerfx.logic.utilitys.GetPoint;
import com.coursemanagerfx.logic.utilitys.UpdateUtility;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.json.JSONArray;
import org.json.JSONObject;

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
                    //primaryStage.close();
                    openMainWindow(courseName, courseFile);   // ← открываем главное окно программы
                    return;
                }
            }
        }

        openStartWindow();
    }

    private static boolean printMouseOnP = false;
    public static boolean isPrintMouseOnP() {
        return printMouseOnP;
    }

    public static void main(String[] args) {
        for (String arg : args) {
            if ("-p".equalsIgnoreCase(arg)) {
                System.out.println("=== TURN ON GET MOUSE POSITION MODE ===\n" +
                        "===      RELATIVE TO THE SCENE      ===\n" +
                        "X | Y");
                printMouseOnP = true;
            }
        }
        launch(args);
    }
}