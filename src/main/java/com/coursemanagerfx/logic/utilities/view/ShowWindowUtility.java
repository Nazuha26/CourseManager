/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic.utilities.view;

import com.coursemanagerfx.AppConstants;
import com.coursemanagerfx.logic.Actions;
import com.coursemanagerfx.logic.CourseInfo;
import com.coursemanagerfx.Launcher;
import com.coursemanagerfx.animations.WindowBlindsInAnimation;
import com.coursemanagerfx.controllers.StageAttachable;
import com.coursemanagerfx.controllers.StageSetupUtility;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX;
import com.coursemanagerfx.controllers.dialogs.alert.AlertMessageType;
import com.coursemanagerfx.controllers.main.Main_controller;
import com.coursemanagerfx.controllers.main.Start_controller;
import com.coursemanagerfx.logic.config_api.ConfigManager;
import com.coursemanagerfx.logic.utilities.security.CmanSecurityUtility;
import com.coursemanagerfx.logic.utilities.security.BinaryPlainCmanUtility;
import com.coursemanagerfx.logic.utilities.AppUtility;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.*;
import javafx.stage.Window;
import javafx.util.Duration;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class ShowWindowUtility {
    private static <T> T loadWindow(String fxmlPath, Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(AppConstants.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        scene.setFill(Color.TRANSPARENT);

        AppUtility.setAppIcon(stage);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);

        if (loader.getController() instanceof StageAttachable windowController)
            windowController.setStage(stage);

        return loader.getController();
    }

    // === МЕТОД ОТКРЫТИЯ СТАРТОВОГО ОКНА ПРОГРАММЫ ===
    public static void showStartWindow() {
        try {
            Stage startStage = new Stage();
            Start_controller controller = loadWindow(
                    "/com/coursemanagerfx/ui/forms/start.fxml",
                    startStage
            );

            controller.setStage(startStage);

            startStage.show();
            closeNativeSplash();

            String title = "Welcome to CourseManagerFX – v" + AppConstants.APP_VERSION;
            startStage.setTitle(title);
            controller.getLabelTitle().setText(title);

            WindowBlindsInAnimation.play(
                    controller,
                    controller.getRootPane().getWidth(),
                    controller.getRootPane().getHeight(),
                    Start_controller.SW_ANIM_STRIPE_COUNT,
                    Duration.seconds(1),
                    () -> StageSetupUtility.setup(controller, startStage)
            );

            /* === CHECK FOR UPDATES === */
            Actions.getInstance().uiFlowActions().runUpdateFlow(false);
            /* ---===================--- */

        } catch (IOException ex) {
            AlertFX.showNotification(
                    AlertMessageType.ERROR,
                    "Startup Error",
                    "Failed to load the start window:\n" + ex.getMessage()
            );
            //throw new WindowLoadException("Failed to open start window", ex);
        }
    }

    // === МЕТОД ОТКРЫТИЯ ГЛАВНОГО ОКНА ПРОГРАММЫ ===
    public static boolean showMainWindow(File file) {
        return showMainWindow(file, null);
    }

    public static boolean showMainWindow(File file, Window passwordOwner) {
        char[] seedPhrase = null;
        BinaryPlainCmanUtility.CourseData courseData = null;

        do {
            seedPhrase = ShowDialogUtility.showCheckSeedPhraseDialog(passwordOwner);
            if (seedPhrase == null) return false;

            try {
                courseData = CmanSecurityUtility.readSecureCourseData(file, seedPhrase);
            } catch (Exception exception) {
                Arrays.fill(seedPhrase, '\0');
                seedPhrase = null;
                AlertFX.showNotification(
                        AlertMessageType.ERROR,
                        "Unable to open the course file",
                        "The seed phrase is incorrect, the file is corrupted, or it uses an unsupported format."
                );
            }
        } while (courseData == null);

        try {
            Stage mainStage = new Stage();
            Main_controller controller = loadWindow(
                    "/com/coursemanagerfx/ui/forms/main.fxml",
                    mainStage
            );

            controller.setStage(mainStage);


            // === set full screen stage ===
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            mainStage.setX(bounds.getMinX());
            mainStage.setY(bounds.getMinY());
            mainStage.setWidth(bounds.getWidth());
            mainStage.setHeight(bounds.getHeight());
            // ---=======================---

            mainStage.show();
            closeNativeSplash();

            String fileName = file.getName();
            String courseName = fileName.endsWith(".cman")
                    ? fileName.substring(0, fileName.length() - 5)
                    : fileName;

            String title = "CourseManagerFX v" + AppConstants.APP_VERSION + " – " + courseName;
            controller.getLblAppName().setText(title);
            mainStage.setTitle(title);

            WindowBlindsInAnimation.play(
                    controller,
                    controller.getRootPane().getWidth(),
                    controller.getRootPane().getHeight(),
                    Main_controller.MW_ANIM_STRIPE_COUNT,
                    Duration.seconds(1),
                    () -> StageSetupUtility.setup(controller, mainStage, 0)
            );
            try {
                Launcher.setCourseInfo(new CourseInfo(
                        file,
                        seedPhrase,
                        courseData.groups(),
                        courseData.nextStudentId(),
                        courseData.nextEventId()));
            } finally {
                Arrays.fill(seedPhrase, '\0');
            }
            ConfigManager.setOpenCourse(file.getAbsolutePath());

            Actions.getInstance().uiFlowActions().runCourseDataLoadingFlow();

            /* Check only after the course has been opened successfully. */
            Actions.getInstance().uiFlowActions().runUpdateFlow(false);
            return true;

        } catch (IOException ex) {
            AlertFX.showNotification(
                    AlertMessageType.ERROR,
                    "Startup Error",
                    "Failed to load the main window:\n" + ex.getMessage()
            );
            //throw new WindowLoadException("Failed to open main window", ex);
            return false;
        } finally {
            if (seedPhrase != null) Arrays.fill(seedPhrase, '\0');
        }
    }

    private static void closeNativeSplash() {
        SplashScreen splash = SplashScreen.getSplashScreen();
        if (splash != null) {
            splash.close();
        }
    }
}
