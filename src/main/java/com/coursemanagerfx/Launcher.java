/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx;

import com.coursemanagerfx.controllers.dialogs.alert.AlertFX;
import com.coursemanagerfx.controllers.dialogs.alert.AlertMessageType;
import com.coursemanagerfx.logic.CourseInfo;
import com.coursemanagerfx.logic.config_api.ConfigManager;
import com.coursemanagerfx.logic.utilities.update.UpdateUtility;
import com.coursemanagerfx.logic.utilities.view.ShowWindowUtility;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static com.coursemanagerfx.AppConstants.*;

public class Launcher extends Application {

    /* ===== STATIC INFORMATION ABOUT OPENED COURSE ===== */
    private static CourseInfo courseInfo;
    public static CourseInfo getCourseInfo() { return courseInfo; }
    public static void setCourseInfo(CourseInfo courseInfo) { Launcher.courseInfo = courseInfo; }
    /* ================================================== */

    @Override
    public void init() { ConfigManager.safeLoadingConfig(); }

    @Override
    public void start(Stage primaryStage) throws Exception {

        /* set locale for language */
        String language = ConfigManager.getLanguage();
        if (language.equals("ua")) Locale.setDefault(UA);    // === UA - LOCALE
        else Locale.setDefault(Locale.US);                   // === US - LOCALE in all other cases
        /* ----------------------- */

        /* === handling all exceptions === */
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Platform.runLater(() -> {
                AlertFX.showNotification(
                        AlertMessageType.ERROR,
                        "Unexpected Error",
                        "An unhandled error occurred:\n" + throwable.getMessage()
                );
            });
        });
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            Platform.runLater(() -> {
                AlertFX.showNotification(
                        AlertMessageType.ERROR,
                        "UI Error",
                        "An unhandled error occurred in UI thread:\n" + throwable.getMessage()
                );
            });
        });
        /* =============================== */

        try {
            Files.createDirectories(COURSES_PATH);
        } catch (IOException e) {
            System.err.println("Failed to create courses directory: " + COURSES_PATH.toAbsolutePath());
            AlertFX.showNotification(
                    AlertMessageType.ERROR,
                    "Startup Error",
                    "Failed to create courses directory:\n" + COURSES_PATH.toAbsolutePath() +
                            "\n\nPlease check write permissions or run the app as administrator."
            );
            return;
        }

        primaryStage.close();

        String courseName = ConfigManager.getOpenCourse();
        if (courseName.equals("none")) ShowWindowUtility.showStartWindow();
        else {
            File expectedFile = resolveConfiguredCourse(courseName);
            if (expectedFile != null
                    && expectedFile.isFile()
                    && ShowWindowUtility.showMainWindow(expectedFile)) {
                // Main window opened successfully.
            } else {
                ConfigManager.setOpenCourse("none");
                ShowWindowUtility.showStartWindow();
            }
        }

        UpdateUtility.signalSuccessfulStart(getParameters().getRaw());
    }

    @Override
    public void stop() {
        clearCourseInfo();
    }

    public static void clearCourseInfo() {
        if (courseInfo == null) return;
        courseInfo.clearSeedPhrase();
        courseInfo = null;
    }

    private static File resolveConfiguredCourse(String configuredCourse) {
        try {
            Path configuredPath = Path.of(configuredCourse);
            if (configuredPath.isAbsolute()) {
                return configuredPath.toAbsolutePath().normalize().toFile();
            }
            String relativeFile = configuredCourse.endsWith(".cman")
                    ? configuredCourse
                    : configuredCourse + ".cman";
            return COURSES_PATH.resolve(relativeFile)
                    .toAbsolutePath()
                    .normalize()
                    .toFile();
        } catch (InvalidPathException exception) {
            return null;
        }
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
