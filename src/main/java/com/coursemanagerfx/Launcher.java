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
import com.coursemanagerfx.logic.config_api.AppConfig;
import com.coursemanagerfx.logic.config_api.ConfigManager;
import com.coursemanagerfx.logic.utilities.update.UpdateUtility;
import com.coursemanagerfx.logic.utilities.view.ShowWindowUtility;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;

import static com.coursemanagerfx.AppConstants.*;

public class Launcher extends Application {

    /* ===== STATIC INFORMATION ABOUT OPENED COURSE ===== */
    private static CourseInfo courseInfo;
    public static CourseInfo getCourseInfo() { return courseInfo; }
    public static void setCourseInfo(CourseInfo courseInfo) { Launcher.courseInfo = courseInfo; }
    /* ================================================== */

    private static String defaultPassword;
    public static String getDefaultPassword() { return defaultPassword; }

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

        AppConfig config = ConfigManager.safeLoadingConfig();

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

        String defPass_ = ConfigManager.getDefaultPassword();
        if (defPass_.equals("none")) defaultPassword = "magic";
        else defaultPassword = defPass_;


        //presetPassword = "AKs#Ku!H@K";               // 1 курс
        //presetPassword = "3Tr_J>UrTy";               // 2 курс
        //presetPassword = "T#6JJd>cm@";               // 3 курс

        primaryStage.close();

        String courseName = ConfigManager.getOpenCourse();
        if (courseName.equals("none")) ShowWindowUtility.showStartWindow();
        else {
            File expectedFile = COURSES_PATH.resolve(courseName + ".cman").toFile();
            if (expectedFile.exists()) ShowWindowUtility.showMainWindow(expectedFile);
            else ShowWindowUtility.showStartWindow();
        }

        UpdateUtility.signalSuccessfulStart(getParameters().getRaw());
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
