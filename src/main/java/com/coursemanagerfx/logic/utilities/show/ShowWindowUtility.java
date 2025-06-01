package com.coursemanagerfx.logic.utilities.show;

import com.coursemanagerfx.CM_HELPER;
import com.coursemanagerfx.custom_ui.ProgressSpinner;
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
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.*;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;

public class ShowWindowUtility {
    private static <T> T loadWindow(String fxmlPath, Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(CM_HELPER.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        scene.setFill(Color.TRANSPARENT);

        stage.getIcons().addAll(
                new Image(Objects.requireNonNull(ShowWindowUtility.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/cmfx_icon-1.png"))),
                new Image(Objects.requireNonNull(ShowWindowUtility.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/cmfx_icon-2.png"))),
                new Image(Objects.requireNonNull(ShowWindowUtility.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/cmfx_icon-3.png"))),
                new Image(Objects.requireNonNull(ShowWindowUtility.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/cmfx_icon-4.png"))),
                new Image(Objects.requireNonNull(ShowWindowUtility.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/cmfx_icon-5.png"))),
                new Image(Objects.requireNonNull(ShowWindowUtility.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/cmfx_icon-6.png"))),
                new Image(Objects.requireNonNull(ShowWindowUtility.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/cmfx_icon-7.png"))),
                new Image(Objects.requireNonNull(ShowWindowUtility.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/cmfx_icon-8.png")))
        );
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

            WindowInAnimation.play(
                    controller,
                    controller.getRootPane().getWidth(),
                    controller.getRootPane().getHeight(),
                    Start_controller.SW_ANIM_STRIPE_COUNT,
                    Duration.seconds(1),
                    () -> StageSetupUtility.setup(controller, startStage)
            );

            /* === CHECK FOR UPDATES === */
            Actions.getInstance().updateActions().checkAndInstallUpdate(
                    startStage.getScene().getWindow(), true);
            /* ---===================--- */

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

            /* === CHECK FOR UPDATES === */
            Actions.getInstance().updateActions().checkAndInstallUpdate(
                    owner, false);
            /* ---===================--- */

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



            Task<Void> dataLoadingTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    updateProgress(0.15, 1);
                    Thread.sleep(1000);

                    updateProgress(0.50, 1);
                    Thread.sleep(1000);

                    updateProgress(0.80, 1);
                    Thread.sleep(1000);

                    Platform.runLater(() -> Actions.getInstance().repaint().initGroupTabs());

                    updateProgress(1.0, 1);
                    return null;
                }
            };

            ProgressSpinner psl = new ProgressSpinner(owner,
                    ProgressSpinner.Style.BIG,
                    ProgressSpinner.Position.CENTER,
                    Modality.APPLICATION_MODAL,
                    "Data loading");
            Actions.getInstance().taskLoader().loadRealTask(
                    psl,
                    dataLoadingTask,

                    unused -> psl.close(),

                    ex -> {                               // onFailure
                        psl.close();
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