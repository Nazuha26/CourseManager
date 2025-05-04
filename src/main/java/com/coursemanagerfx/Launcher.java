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
        File[] files = CONFIG_DIR.listFiles((dir, name) ->
                name.startsWith(template) && !name.equals(LAST_RUN_FILE.getName())
        );
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }

        CM_HELPER.setPassword("RMieg>mja%");    // Test
        //CM_HELPER.setPassword("lm6d!_O6A3");    // Test 2
        // Если файл FIRST_RUN существует и содержит имя курса, проверяем, что такой курс есть
        if (CM_HELPER.LAST_RUN_FILE.exists()) {
            String courseName;
            try (BufferedReader reader = new BufferedReader(new FileReader(CM_HELPER.LAST_RUN_FILE))) {
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