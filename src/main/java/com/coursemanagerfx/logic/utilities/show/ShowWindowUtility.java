package com.coursemanagerfx.logic.utilities.show;

import com.coursemanagerfx.CM_HELPER;
import com.coursemanagerfx.logic.Actions;
import com.coursemanagerfx.logic.CourseInfo;
import com.coursemanagerfx.Launcher;
import com.coursemanagerfx.animations.WindowInAnimation;
import com.coursemanagerfx.controllers.StageAttachable;
import com.coursemanagerfx.controllers.StageSetupUtility;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX_type;
import com.coursemanagerfx.controllers.main.Main_controller;
import com.coursemanagerfx.controllers.main.Start_controller;
import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.security.CmanSecurityUtility;
import com.coursemanagerfx.logic.utilities.show.exceptions.WindowLoadException;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ShowWindowUtility {
    private static <T> T loadWindow(String fxmlPath, Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(CM_HELPER.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        scene.setFill(Color.TRANSPARENT);
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

            /*animateAppearance(controller.getRootPane(), () -> {
                Thread updateCheck = new Thread(() -> {
                    String newVersion = UpdateUtility.checkForUpdates(startStage);
                    Platform.runLater(() -> {
                        if (!newVersion.equals("-1")) {
                            UpdateUtility.showUpdateDialog(startStage);
                        }
                    });
                });
                updateCheck.setDaemon(true);
                updateCheck.start();
            });*/

            startStage.show();

            // ПОКА БЕЗ ПРОВЕРКИ ПРОВЕРКИ НА ОБНОВЛЕНИЯ
            WindowInAnimation.play(
                    controller,
                    controller.getRootPane().getWidth(),
                    controller.getRootPane().getHeight(),
                    Start_controller.SW_ANIM_STRIPE_COUNT,
                    Duration.seconds(1),
                    () -> StageSetupUtility.setup(controller, startStage)
            );
        } catch (IOException ex) {
            AlertFX.showNotification(
                    null,
                    AlertFX_type.ERROR,
                    "Startup Error",
                    "Failed to load the start window:\n" + ex.getMessage(),
                    true
            );
            //throw new WindowLoadException("Failed to open start window", ex);
        }
    }

    // === МЕТОД ОТКРЫТИЯ ГЛАВНОГО ОКНА ПРОГРАММЫ ===
    public static void showMainWindow(String courseName, File file) {
        try {
            Stage mainStage = new Stage();
            Main_controller controller = loadWindow(
                    "/com/coursemanagerfx/ui/forms/main.fxml",
                    mainStage
            );

            //if (Launcher.isPrintMouseOnP()) GetPoint.setupMousePositionLogger(mainStage.getScene());

            controller.setStage(mainStage);

            // === set full screen stage ===
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            mainStage.setX(bounds.getMinX());
            mainStage.setY(bounds.getMinY());
            mainStage.setWidth(bounds.getWidth());
            mainStage.setHeight(bounds.getHeight());
            // ---=======================---

            mainStage.show();
            controller.getLblAppName().setText("CourseManagerFX v" + Launcher.CUR_VERSION + " – " + courseName);

            // ПОКА БЕЗ ПРОВЕРКИ ПРОВЕРКИ НА ОБНОВЛЕНИЯ
            WindowInAnimation.play(
                    controller,
                    controller.getRootPane().getWidth(),
                    controller.getRootPane().getHeight(),
                    Main_controller.MW_ANIM_STRIPE_COUNT,
                    Duration.seconds(1),
                    () -> StageSetupUtility.setup(controller, mainStage, 0)
            );

            String password;
            Group[] course = null;
            Window owner = mainStage.getScene().getWindow();

            if (Launcher.getPresetPassword() == null) {
                // === password checking ===
                do {
                    password = ShowDialogUtility.showCheckPasswordDialog(owner);
                    if (password == null) {
                        Actions.getInstance().uiActions().mainExitAction();
                        return;
                    } // if pressed Cancel

                    try {
                        course = CmanSecurityUtility.readSecureFile(file, password);
                    } catch (Exception e) {
                        AlertFX.showNotification(owner,
                                AlertFX_type.ERROR,
                                "Invalid password",
                                "Please try again.",
                                true);
                    }

                } while (course == null);
                // =========================
            } else {
                password = Launcher.getPresetPassword();
                try {
                    course = CmanSecurityUtility.readSecureFile(file, password);
                } catch (Exception e) {
                    AlertFX.showNotification(owner,
                            AlertFX_type.ERROR,
                            "Failed to load course",
                            "Preset password appears to be incorrect.",
                            true);
                    Actions.getInstance().uiActions().mainExitAction();
                    return;
                }
            }

            Launcher.setCourseInfo(new CourseInfo(courseName, password, course));

            Actions.getInstance().taskLoader().loadTask(    // add check updates
                    2000,
                    () -> {                               // onSuccess
                        System.out.println("=== DATA LOADING COMPLETED SUCCESSFULLY ===");
                        Actions.getInstance().repaint().initGroupTabs();
                    },

                    ex -> {                               // onFailure
                        AlertFX.showNotification(
                                mainStage.getScene().getWindow(),
                                AlertFX_type.ERROR,
                                "Data loading failed",
                                "Something went wrong during data loading",
                                true);
                        Actions.TaskLoader.LOGGER.log(Level.SEVERE, "=== DATA LOADING FAILED ===", ex);
                    }
            );
        } catch (IOException ex) {
            AlertFX.showNotification(
                    null,
                    AlertFX_type.ERROR,
                    "Startup Error",
                    "Failed to load the main window:\n" + ex.getMessage(),
                    true
            );
            //throw new WindowLoadException("Failed to open main window", ex);
        }
    }
}