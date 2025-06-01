package com.coursemanagerfx;

import com.coursemanagerfx.controllers.dialogs.alert.AlertFX;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX_type;
import com.coursemanagerfx.logic.CourseInfo;
import com.coursemanagerfx.logic.utilities.show.ShowWindowUtility;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import static com.coursemanagerfx.CM_HELPER.*;

public class Launcher extends Application {

    public static final String CUR_VERSION = "1.1.0";

    private static CourseInfo courseInfo;
    public static CourseInfo getCourseInfo() {
        return courseInfo;
    }
    public static void setCourseInfo(CourseInfo courseInfo) {
        Launcher.courseInfo = courseInfo;
    }

    private static String presetPassword;
    public static String getPresetPassword() {
        return presetPassword;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        /* === handling all exceptions === */
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Platform.runLater(() -> {
                AlertFX.showNotification(
                        null,
                        AlertFX_type.ERROR,
                        "Unexpected Error",
                        "An unhandled error occurred:\n" + throwable.getMessage(),
                        true
                );
            });
        });
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            Platform.runLater(() -> {
                AlertFX.showNotification(
                        null,
                        AlertFX_type.ERROR,
                        "UI Error",
                        "An unhandled error occurred in UI thread:\n" + throwable.getMessage(),
                        true
                );
            });
        });
        /* =============================== */

        if (!CONFIG_DIR.exists() && !CONFIG_DIR.mkdirs()) {
            System.err.println("Failed to create config directory: " + CONFIG_DIR.getAbsolutePath());
            AlertFX.showNotification(
                    null,
                    AlertFX_type.ERROR,
                    "Startup Error",
                    "Failed to create configuration directory:\n" + CONFIG_DIR.getAbsolutePath() +
                    "\n\nPlease check write permissions or run the app as administrator.",
                    true);
            return;
        }
        if (!COURSES_DIR.exists() && !COURSES_DIR.mkdirs()) {
            System.err.println("Failed to create courses directory: " + COURSES_DIR.getAbsolutePath());
            AlertFX.showNotification(
                    null,
                    AlertFX_type.ERROR,
                    "Startup Error",
                    "Failed to create courses directory:\n" + COURSES_DIR.getAbsolutePath() +
                            "\n\nPlease check write permissions or run the app as administrator.",
                    true
            );
            return;
        }

        /* when was installed the newest update then delete all old LAST_RUN_FILE */
        File[] files = CONFIG_DIR.listFiles((dir, name) ->
                name.startsWith(template) && !name.equals(LAST_RUN_FILE.getName())
        );
        if (files != null)
            for (File f : files)
                try {
                    if (!f.delete()) {
                        System.err.println("Failed to delete file: " + f.getAbsolutePath());
                        AlertFX.showNotification(
                                null,
                                AlertFX_type.ERROR,
                                "Startup Error",
                                "Failed to delete old cache file:\n" + f.getAbsolutePath() +
                                        "\n\nPlease try restarting the app or delete it manually.",
                                true
                        );
                        return;
                    }
                } catch (Exception ex) {
                    System.err.println("Error deleting file: " + f.getAbsolutePath());
                    AlertFX.showNotification(
                            null,
                            AlertFX_type.ERROR,
                            "Startup Error",
                            "An unexpected error occurred while deleting file:\n" + f.getAbsolutePath() +
                                    "\n\nDetails: " + ex.getMessage(),
                            true
                    );
                    return;
                }
        /* ---------------------------------------------------------------------- */

        //CM_HELPER.setPassword("RMieg>mja%");       // Test
        //CM_HELPER.setPassword("lm6d!_O6A3");       // Test 2
        //presetPassword = "0Qn$rptY2*";               // 1 курс
        LaunchCourseInfo info = existCourseFile();

        primaryStage.close();

        if (info.exists()) {
            ShowWindowUtility.showMainWindow(info.courseName(), info.courseFile());
            return;
        }

        ShowWindowUtility.showStartWindow();    // in other case open start window
    }

    private static LaunchCourseInfo existCourseFile() {
        if (!LAST_RUN_FILE.exists()) return new LaunchCourseInfo(false, null, null);

        try (BufferedReader reader = new BufferedReader(new FileReader(LAST_RUN_FILE))) {
            String line = reader.readLine();
            if (line == null || line.isEmpty()) return new LaunchCourseInfo(false, null, null);

            char firstChar = line.trim().charAt(0);
            if (!Character.isDigit(firstChar)) return new LaunchCourseInfo(false, null, null);

            int courseNum = Character.getNumericValue(firstChar);
            String courseName = courseNameTemplate.replace("X", String.valueOf(courseNum));
            File expectedFile = new File(COURSES_DIR, courseName + ".cman");

            if (expectedFile.exists()) {
                return new LaunchCourseInfo(true, courseName, expectedFile);
            }
        } catch (Exception ignored) {}

        return new LaunchCourseInfo(false, null, null);
    }

    private static boolean printMouseOnP = false;
    public static boolean isPrintMouseOnP() {
        return printMouseOnP;
    }

    public static void main(String[] args) {
        for (String arg : args) {
            if ("-p".equalsIgnoreCase(arg)) {
                System.out.println("""
                        === TURN ON GET MOUSE POSITION MODE ===
                        ===      RELATIVE TO THE SCENE      ===
                        X | Y
                        """);
                printMouseOnP = true;
            }
        }
        launch(args);
    }
}

record LaunchCourseInfo(boolean exists, String courseName, File courseFile) { }