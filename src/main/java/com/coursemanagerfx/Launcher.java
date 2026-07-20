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
import com.coursemanagerfx.logic.utilities.SingleInstanceGuard;
import com.coursemanagerfx.logic.utilities.update.UpdateUtility;
import com.coursemanagerfx.logic.utilities.view.ShowWindowUtility;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

import static com.coursemanagerfx.AppConstants.*;

public class Launcher extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    private SingleInstanceGuard instanceGuard;
    private boolean anotherInstanceDetected;
    private IOException instanceLockFailure;

    /* ===== STATIC INFORMATION ABOUT OPENED COURSE ===== */
    private static CourseInfo courseInfo;
    public static CourseInfo getCourseInfo() { return courseInfo; }
    public static void setCourseInfo(CourseInfo courseInfo) { Launcher.courseInfo = courseInfo; }
    /* ================================================== */

    @Override
    public void init() {
        try {
            Optional<SingleInstanceGuard> acquired =
                    SingleInstanceGuard.tryAcquire(INSTANCE_LOCK_PATH);
            if (acquired.isPresent()) instanceGuard = acquired.get();
            else anotherInstanceDetected = true;
        } catch (IOException exception) {
            instanceLockFailure = exception;
        }

        if (!anotherInstanceDetected && instanceLockFailure == null) {
            ConfigManager.safeLoadingConfig();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        if (anotherInstanceDetected) {
            LOGGER.info("A second application instance was blocked");
            AlertFX.showNotification(
                    AlertMessageType.INFO,
                    "CourseManagerFX is already running",
                    "Only one application instance can be open at a time.");
            Platform.exit();
            return;
        }
        if (instanceLockFailure != null) {
            LOGGER.error("Could not acquire the application instance lock", instanceLockFailure);
            AlertFX.showNotification(
                    AlertMessageType.ERROR,
                    "Startup Error",
                    "Could not create the application lock file:\n"
                            + INSTANCE_LOCK_PATH.toAbsolutePath());
            Platform.exit();
            return;
        }

        /* set locale for language */
        String language = ConfigManager.getLanguage();
        if (language.equals("ua")) Locale.setDefault(UA);    // === UA - LOCALE
        else Locale.setDefault(Locale.US);                   // === US - LOCALE in all other cases
        /* ----------------------- */

        /* === handling all exceptions === */
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            LOGGER.error("Unhandled exception in thread {}", thread.getName(), throwable);
            Platform.runLater(() -> {
                AlertFX.showNotification(
                        AlertMessageType.ERROR,
                        "Unexpected Error",
                        "An unhandled error occurred:\n" + throwable.getMessage()
                );
            });
        });
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            LOGGER.error("Unhandled exception in JavaFX thread {}", thread.getName(), throwable);
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
            LOGGER.error("Failed to create courses directory: {}", COURSES_PATH.toAbsolutePath(), e);
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
        if (instanceGuard != null) {
            try {
                instanceGuard.close();
            } catch (IOException exception) {
                LOGGER.warn("Could not release the application instance lock", exception);
            } finally {
                instanceGuard = null;
            }
        }
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
                LOGGER.info("""
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
