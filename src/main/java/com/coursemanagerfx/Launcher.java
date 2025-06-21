package com.coursemanagerfx;

import com.coursemanagerfx.controllers.dialogs.alert.AlertFX;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX_type;
import com.coursemanagerfx.logic.CourseInfo;
import com.coursemanagerfx.logic.utilities.config_api.AppConfig;
import com.coursemanagerfx.logic.utilities.config_api.ConfigManager;
import com.coursemanagerfx.logic.utilities.show.ShowWindowUtility;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;

import static com.coursemanagerfx.AppConstants.*;

public class Launcher extends Application {

    private static CourseInfo courseInfo;
    public static CourseInfo getCourseInfo() {
        return courseInfo;
    }
    public static void setCourseInfo(CourseInfo courseInfo) {
        Launcher.courseInfo = courseInfo;
    }

    private static String defaultPassword;
    public static String getDefaultPassword() {
        return defaultPassword;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Locale.setDefault(Locale.US);                                   // === US - LOCALE
        //Locale.setDefault(Locale.of("uk", "UA"));       // === UA - LOCALE

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

        AppConfig config = ConfigManager.safeLoadingConfig();

        /*if (!ConfigManager.CONFIG_PATH.exists() && !CONFIG_PATH.mkdirs()) {
            System.err.println("Failed to create config_api directory: " + CONFIG_DIR.getAbsolutePath());
            AlertFX.showNotification(
                    null,
                    AlertFX_type.ERROR,
                    "Startup Error",
                    "Failed to create configuration directory:\n" + CONFIG_DIR.getAbsolutePath() +
                    "\n\nPlease check write permissions or run the app as administrator.",
                    true);
            return;
        }*/
        try {
            Files.createDirectories(COURSES_PATH);
        } catch (IOException e) {
            System.err.println("Failed to create courses directory: " + COURSES_PATH.toAbsolutePath());
            AlertFX.showNotification(
                    null,
                    AlertFX_type.ERROR,
                    "Startup Error",
                    "Failed to create courses directory:\n" + COURSES_PATH.toAbsolutePath() +
                            "\n\nPlease check write permissions or run the app as administrator.",
                    true
            );
            return;
        }

        /* when was installed the newest update then delete all old LAST_RUN_FILE */
        /*File[] files = CONFIG_DIR.listFiles((dir, name) ->
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
                }*/
        /* ---------------------------------------------------------------------- */

        String defPass_ = ConfigManager.getDefaultPassword();
        if (defPass_ == null || defPass_.equals("none") || defPass_.isBlank()) defaultPassword = "magic";
        else defaultPassword = defPass_;


        //defaultPassword = "0Qn$rptY2*";               // 1 курс
        //presetPassword = "3Tr_J>UrTy";               // 2 курс
        //LaunchCourseInfo info = existCourseFile();

        Pair<String, File> courseInfo = getLastOpenedCourseFromConfig();

        primaryStage.close();

        if (courseInfo != null) ShowWindowUtility.showMainWindow(courseInfo.getKey(), courseInfo.getValue());
        else ShowWindowUtility.showStartWindow();
    }

    private static Pair<String, File> getLastOpenedCourseFromConfig() {
        String courseName = ConfigManager.getOpenCourse();
        if (courseName == null || courseName.equals("none") || courseName.isBlank())
            return null;

        File expectedFile = COURSES_PATH.resolve(courseName + ".cman").toFile();
        return expectedFile.exists() ? new Pair<>(courseName, expectedFile) : null;
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

//record LaunchCourseInfo(boolean exists, String courseName, File courseFile) { }