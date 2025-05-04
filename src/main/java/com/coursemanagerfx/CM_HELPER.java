package com.coursemanagerfx;

import com.coursemanagerfx.controllers.Main_controller;
import com.coursemanagerfx.controllers.Start_controller;
import com.coursemanagerfx.dialogs.InputDialog_controller;
import com.coursemanagerfx.dialogs.NewCourseDialog_controller;
import com.coursemanagerfx.dialogs.password.InputPass_controller;
import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.utilitys.GetPoint;
import com.coursemanagerfx.logic.utilitys.UpdateUtility;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.*;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;

public class CM_HELPER {

    // ===== CONSTANTS =====
    public static final String CUR_VERSION = "1.0.9";
    public static final int ANIMATION_DURATION = 300;
    public static final double MAIN_SMALL_WINDOW_WIDTH = 1300;
    public static final double MAIN_SMALL_WINDOW_HEIGHT = 700;
    // =====================

    private static String password;
    public static String getPassword() {
        return password;
    }
    public static void setPassword(String password) {
        CM_HELPER.password = password;
    }

    public static final File CONFIG_DIR = new File(System.getProperty("user.home"), "AppData/Local/CManFX");
    public static final File COURSES_DIR = new File(System.getProperty("user.home"), ".cmanfx/Courses/");
    //public static final File SECRET_KEY_DIR = CONFIG_DIR;
    public static final String template = "LastRun";
    public static final File LAST_RUN_FILE = new File(CONFIG_DIR, template + "_" + CUR_VERSION);
    public static final File CONFIG_FILE = new File(CONFIG_DIR, "config.json");

    //private static String new_version;
    //public static String get_new_version() { return new_version; }
    private static String courseName;
    private static Group[] Course;

    public static String getCourseName() {
        return courseName;
    }
    public static void setCourseName(String courseName) {
        CM_HELPER.courseName = courseName;
    }

    // ------------------------------------------------------------------

    public static Group[] getCourse() {
        return Course;
    }
    public static void setCourse(Group[] course) {
        CM_HELPER.Course = course;
    }

    // ------------------------------------------------------------------

    // === INPUT DIALOG ===
    public static String showInputDialog(Window owner, String dialogTitle, String dialogPrompt) {
        try {
            FXMLLoader loader = new FXMLLoader(CM_HELPER.class.getResource("/com/coursemanagerfx/ui/dialogs/input_dialog.fxml"));
            Parent root = loader.load();

            InputDialog_controller controller = loader.getController();
            controller.getLabelTitle().setText(dialogTitle);
            controller.getLabelPrompt().setText(dialogPrompt);

            Stage dialogStage = new Stage();
            dialogStage.initOwner(owner);
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            dialogStage.initModality(Modality.WINDOW_MODAL);

            Scene scene = new Scene(root);
            scene.setFill(null);
            dialogStage.setScene(scene);

            controller.setStage(dialogStage);

            // Запускаем анимацию сразу после того, как окно покажется
            dialogStage.setOnShown(event -> {
                CM_HELPER.animateAppearance(root);
            });

            // Используем только showAndWait() – она и покажет окно модально и дождется закрытия
            dialogStage.showAndWait();
            return controller.getInputText();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // === CHECK PASSWORD DIALOG ===
    public static String showCheckPasswordDialog(Window owner, File courseFile) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    CM_HELPER.class.getResource("/com/coursemanagerfx/ui/dialogs/password/input_password_dialog.fxml")
            );
            Parent root = loader.load();

            InputPass_controller controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.initOwner(owner);
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            dialogStage.initModality(Modality.WINDOW_MODAL);

            Scene scene = new Scene(root);
            scene.setFill(null);
            dialogStage.setScene(scene);

            controller.setStage(dialogStage);
            controller.setFile(courseFile);

            dialogStage.setOnShown(event -> CM_HELPER.animateAppearance(root));
            dialogStage.showAndWait();

            return controller.getInputPassword();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load password dialog", e);
        }
    }

    // === NEW COURSE DIALOG ===
    /** Показывает окно «Новый курс» и возвращает его контроллер после закрытия. */
    public static NewCourseDialog_controller showNewCourseDialog(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    CM_HELPER.class.getResource("/com/coursemanagerfx/ui/dialogs/new_course_dialog.fxml"));
            Parent root = loader.load();

            NewCourseDialog_controller controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.initOwner(owner);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(root);
            scene.setFill(null);
            dialogStage.setScene(scene);

            controller.setStage(dialogStage);

            dialogStage.setOnShown(e -> CM_HELPER.animateAppearance(root));
            dialogStage.showAndWait();          // модально

            return controller;                  // ← вернём, чтобы узнать результат
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    // === OPEN COURSE DIALOG ===
    public static File showOpenCourseDialog(Window owner) {
        FileChooser fc = new FileChooser();
        if (COURSES_DIR.exists()) {
            fc.setInitialDirectory(COURSES_DIR);
        }
        fc.getExtensionFilters().setAll(
                new FileChooser.ExtensionFilter("CMan course (*.cman)", "*.cman"));
        fc.setTitle("Select an existing course");

        return fc.showOpenDialog(owner);   // null ‑ если отменили
    }


    // === АНИМАЦИЯ ПОЯВЛЕНИЯ ОКНА ===
    public static void animateAppearance(Parent window) {
        window.setOpacity(0);
        window.setScaleX(0.5);
        window.setScaleY(0.5);
        Timeline fadeInAndScale = new Timeline(
                new KeyFrame(Duration.millis(ANIMATION_DURATION),
                        new KeyValue(window.opacityProperty(), 1),
                        new KeyValue(window.scaleXProperty(), 1),
                        new KeyValue(window.scaleYProperty(), 1)
                )
        );
        fadeInAndScale.play();
    }
    public static void animateAppearance(Parent window, Runnable onFinished) {
        window.setOpacity(0);
        window.setScaleX(0.5);
        window.setScaleY(0.5);

        Timeline fadeInAndScale = new Timeline(
                new KeyFrame(Duration.millis(ANIMATION_DURATION),
                        new KeyValue(window.opacityProperty(), 1),
                        new KeyValue(window.scaleXProperty(), 1),
                        new KeyValue(window.scaleYProperty(), 1)
                )
        );

        fadeInAndScale.setOnFinished(e -> {
            if (onFinished != null) onFinished.run();
        });

        fadeInAndScale.play();
    }

    // === АНИМАЦИЯ ВОССТАНОВЛЕНИЯ ОКНА ===
    public static void animateRestoration(Stage stage) {
        if (stage == null) return;
        Parent root = stage.getScene().getRoot();
        root.setOpacity(0);
        Timeline fadeIn = new Timeline(
                new KeyFrame(Duration.millis((double) ANIMATION_DURATION / 2),
                        new KeyValue(root.opacityProperty(), 1)
                )
        );
        fadeIn.play();
    }

    // === ACTION CLOSE С ВОЗМОЖНЫМ ЗАПУСКОМ КОЛЛБЭКА ===
    public static void actionClose(Stage stage, Runnable onFinished) {
        if (stage == null) return;
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(ANIMATION_DURATION),
                        new KeyValue(stage.getScene().getRoot().opacityProperty(), 0),
                        new KeyValue(stage.getScene().getRoot().scaleXProperty(), 0.5),
                        new KeyValue(stage.getScene().getRoot().scaleYProperty(), 0.5)
                )
        );
        timeline.setOnFinished(e -> {
            stage.close();
            if (onFinished != null) onFinished.run();
        });
        timeline.play();
    }

    // === ACTION MINIMIZE ===
    public static void actionMinimize(Stage stage) {
        if (stage == null) return;
        Parent root = stage.getScene().getRoot();
        Timeline fadeOut = new Timeline(
                new KeyFrame(Duration.millis((double) ANIMATION_DURATION / 2),
                        new KeyValue(root.opacityProperty(), 0)
                )
        );
        fadeOut.setOnFinished(e -> stage.setIconified(true));
        fadeOut.play();
    }

    // === МЕТОД ДВИЖЕНИЯ ОКНОМ ===
    public static void onDragged(MouseEvent event, Stage stage, double xOffset, double yOffset) {
        if (stage == null) return;
        double newX = event.getScreenX() - xOffset;
        double newY = event.getScreenY() - yOffset;
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

        if (newX < bounds.getMinX()) newX = bounds.getMinX();
        if (newX + stage.getWidth() > bounds.getMaxX()) newX = bounds.getMaxX() - stage.getWidth();
        if (newY < bounds.getMinY()) newY = bounds.getMinY();
        if (newY + stage.getHeight() > bounds.getMaxY()) newY = bounds.getMaxY() - stage.getHeight();

        stage.setX(newX);
        stage.setY(newY);
    }

    // === МЕТОД ОТКРЫТИЯ СТАРТОВОГО ОКНА ПРОГРАММЫ ===
    public static void openStartWindow() throws IOException {
        FXMLLoader startLoader = new FXMLLoader(CM_HELPER.class.getResource("/com/coursemanagerfx/ui/forms/start.fxml"));
        Parent startRoot = startLoader.load();
        Scene startScene = new Scene(startRoot);
        startScene.setFill(Color.TRANSPARENT);

        Stage startStage = new Stage();
        startStage.initStyle(StageStyle.TRANSPARENT);
        startStage.setScene(startScene);

        Start_controller startController = startLoader.getController();
        startController.setStage(startStage);
        startStage.show();

        // Анимация появления стартового окна
        animateAppearance(startRoot, () -> {
            new Thread(() -> {
                String new_version = UpdateUtility.checkForUpdates(startStage.getScene().getWindow());
                Platform.runLater(() -> {
                    if (!new_version.equals("-1"))
                        UpdateUtility.showUpdateDialog(startStage);
                });
            }).start();
        });
    }

    // === МЕТОД ОТКРЫТИЯ ГЛАВНОГО ОКНА ПРОГРАММЫ (СРАЗУ) ===
    /*public static void openMainWindow(Stage stage, String courseName, File courseFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(CM_HELPER.class.getResource("/com/coursemanagerfx/ui/forms/main.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);

        showMainStage(stage, courseName, courseFile, loader, root);
    }*/
    // === МЕТОД ОТКРЫТИЯ ГЛАВНОГО ОКНА ПРОГРАММЫ (ПОСЛЕ СТАРТОВОГО ОКНА) ===
    /** Открывает основное окно программы как новый Stage*/
    public static void openMainWindow(String courseName, File courseFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(CM_HELPER.class.getResource("/com/coursemanagerfx/ui/forms/main.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        Stage mainStage = new Stage();
        mainStage.initStyle(StageStyle.TRANSPARENT);
        mainStage.setScene(scene);

        if (Launcher.isPrintMouseOnP()) GetPoint.setupMousePositionLogger(scene);

        // Получаем контроллер до истории
        Main_controller mainController = loader.getController();
        mainController.setStage(mainStage);

        // === QUICK TOUR ===
        /*if (showOnboardingOverlay) {
            mainStage.setOnShown(e -> {
                OnboardingOverlayUtility overlay = new OnboardingOverlayUtility(mainStage, () -> restartFX(mainStage));

                overlay.addInstruction(2 , 41, 1528 , 82,
                        "Groups tab",
                        OnboardingOverlayUtility.Position.BOTTOM);
                overlay.addInstruction(2 , 88, 51 , 145,
                        "Add student button",
                        OnboardingOverlayUtility.Position.BOTTOM);
                overlay.addInstruction(206 , 94, 406 , 131,
                        "Search students in a group",
                        OnboardingOverlayUtility.Position.BOTTOM);
                overlay.addInstruction(404 , 742, 522 , 808,
                        "Add event",
                        OnboardingOverlayUtility.Position.TOP);


                    });
        }*/

        mainController.getLblAppName().setText("CourseManagerFX v" + CUR_VERSION + " – " + courseName);

        // Устанавливаем сразу полноэкранный режим
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        mainStage.setWidth(bounds.getWidth());
        mainStage.setHeight(bounds.getHeight());

        mainStage.show();
        animateAppearance(root, () -> Platform.runLater(() -> {
            mainController.initAfterStageShown(mainStage, courseName, courseFile);
        }));
    }

    // Утилитный метод для перезапуска приложения
    public static void restartFX(Stage currentStage) {
        Platform.runLater(() -> {
            actionClose(currentStage, null); // Закрыть текущее окно
            try {
                new Launcher().start(new Stage()); // Запускаем снова
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ------------------------------------------------------------------
}
