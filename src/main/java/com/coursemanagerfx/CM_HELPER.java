package com.coursemanagerfx;

import com.coursemanagerfx.controllers.Main_controller;
import com.coursemanagerfx.controllers.Start_controller;
import com.coursemanagerfx.dialogs.ConfirmDialog_controller;
import com.coursemanagerfx.dialogs.ConfirmDialogType;
import com.coursemanagerfx.dialogs.InputDialog_controller;
import com.coursemanagerfx.dialogs.NewCourseDialog_controller;
import com.coursemanagerfx.logic.CmanParser;
import com.coursemanagerfx.logic.Group;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.*;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;

public class CM_HELPER {

    // ===== CONSTANTS =====
    public static final int ANIMATION_DURATION = 300;
    public static final double MAIN_SMALL_WINDOW_WIDTH = 1300;
    public static final double MAIN_SMALL_WINDOW_HEIGHT = 700;
    // =====================

    public static final File CONFIG_DIR = new File(System.getProperty("user.home"), "AppData/Local/CManFX");
    public static final File COURSES_DIR = new File(System.getProperty("user.home"), ".cmanfx/Courses/");
    public static final File SECRET_KEY_DIR = CONFIG_DIR;
    public static final File FIRST_RUN_FILE = new File(CONFIG_DIR, "FirstRun");
    public static final File CONFIG_FILE = new File(CONFIG_DIR, "config.json");

    //public File TEMP_FILE = new File(CONFIG_DIR,  "_temp.json");

    private String courseName;
    private Group[] Course;
    //private String FileCoursePath;

    public String getCourseName() {
        return courseName;
    }
    public void setCourseName(String courseName) {
        this.courseName = courseName;
        //this.TEMP_FILE = new File(CONFIG_DIR, courseName + "_temp.json");
    }

    // ------------------------------------------------------------------

    public Group[] getCourse() {
        return Course;
    }
    public void setCourse(Group[] course) {
        this.Course = course;
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
    // === CONFIRM DIALOG ===
    public static boolean showConfirmDialog(Window owner, ConfirmDialogType dialogType, String dialogMainText, String dialogPrompt) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    CM_HELPER.class.getResource("/com/coursemanagerfx/ui/dialogs/confirm_dialog.fxml")
            );
            Parent root = loader.load();

            ConfirmDialog_controller controller = loader.getController();
            controller.getLabelMain().setText(dialogMainText);
            controller.getLabelPrompt().setText(dialogPrompt);

            switch (dialogType) {
                case INFO -> controller.getIconType().setImage(new Image("/com/coursemanagerfx/ui/notifications/icons/info_256x256.png"));
                case WARNING -> controller.getIconType().setImage(new Image("/com/coursemanagerfx/ui/notifications/icons/warning_256x256.png"));
                case ERROR -> controller.getIconType().setImage(new Image("/com/coursemanagerfx/ui/notifications/icons/error_256x256.png"));
            }

            Stage dialogStage = new Stage();
            dialogStage.initOwner(owner);
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            dialogStage.initModality(Modality.WINDOW_MODAL);

            Scene scene = new Scene(root);
            scene.setFill(null);
            dialogStage.setScene(scene);

            controller.setStage(dialogStage);

            dialogStage.setOnShown(event -> CM_HELPER.animateAppearance(root));
            dialogStage.showAndWait();

            return controller.isConfirmed();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
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

        // Скругление углов стартового окна
        Rectangle startClip = new Rectangle();
        startClip.setArcWidth(20);
        startClip.setArcHeight(20);
        startClip.widthProperty().bind(startScene.widthProperty());
        startClip.heightProperty().bind(startScene.heightProperty());
        startRoot.setClip(startClip);

        Stage startStage = new Stage();
        startStage.initStyle(StageStyle.TRANSPARENT);
        startStage.setScene(startScene);

        Start_controller startController = startLoader.getController();
        startController.setStage(startStage);
        startStage.show();

        // Анимация появления стартового окна
        animateAppearance(startRoot);
    }

    // === МЕТОД ОТКРЫТИЯ ГЛАВНОГО ОКНА ПРОГРАММЫ (СРАЗУ) ===
    public static void openMainWindow(Stage stage, String courseName, File courseFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(CM_HELPER.class.getResource("/com/coursemanagerfx/ui/forms/main.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);

        showMainStage(stage, courseName, courseFile, loader, root);
    }
    // === МЕТОД ОТКРЫТИЯ ГЛАВНОГО ОКНА ПРОГРАММЫ (ПОСЛЕ СТАРТОВОГО ОКНА) ===
    public static void openMainWindow(String courseName, File courseFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(CM_HELPER.class.getResource("/com/coursemanagerfx/ui/forms/main.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        Stage mainStage = new Stage();
        mainStage.initStyle(StageStyle.TRANSPARENT);
        mainStage.setScene(scene);

        showMainStage(mainStage, courseName, courseFile, loader, root);
    }
    // Утилитный метод
    private static void showMainStage(Stage stage, String courseName, File courseFile, FXMLLoader loader, Parent root) throws IOException {
        stage.setTitle("CourseManagerFX – " + courseName);

        // Устанавливаем размеры окна под экран
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());

        // Инициализируем главный контроллер с курсом
        Main_controller mainController = loader.getController();
        mainController.setStage(stage);

        mainController.getLblCurHistory().setText("");  // ← очищаем лабель с историей

        stage.show();

        animateAppearance(root);

        // Запускаем фоновую задачу загрузки данных после появления окна
        Task<Void> loadDataTask = getLoadDataTask(courseName, courseFile, mainController);

        new Thread(loadDataTask).start();
    }

    private static final int LOADING_DELAY = 1000;
    // Утилитный метод
    private static Task<Void> getLoadDataTask(String courseName, File courseFile, Main_controller mainController) {
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);
        progressBar.setPrefHeight(40);
        HBox progressContainer = new HBox(progressBar);
        progressContainer.setAlignment(Pos.CENTER);      // ← выравнивание по центру
        progressContainer.setPadding(new Insets(0, 0, 140, 0)); // ← отступ снизу

        mainController.getNotificationPane().setBottom(progressContainer);

        Task<Void> loadDataTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                int totalSteps = 10;
                long stepDelay = LOADING_DELAY / totalSteps;
                for (int i = 0; i < totalSteps; i++) {
                    Thread.sleep(stepDelay);
                    updateProgress(i + 1, totalSteps);
                }
                // Выполнение загрузки данных (убери sleep, если задержка уже учтена)
                loadData(mainController, courseName, courseFile);
                return null;
            }
        };

        progressBar.progressProperty().bind(loadDataTask.progressProperty());

        loadDataTask.setOnSucceeded(event -> {
            mainController.getNotificationPane().setBottom(null);
            System.out.println("Data loaded successfully");
        });

        return loadDataTask;
    }
    // Утилитный метод для загрузки данных
    public static void loadData(Main_controller mainController, String courseName, File courseFile) {
        try {
            System.out.println("Data loading started...");

            CM_HELPER helper = new CM_HELPER();
            helper.setCourse(CmanParser.parseFile(courseFile.getAbsolutePath()));
            helper.setCourseName(courseName);

            javafx.application.Platform.runLater(() -> {
                mainController.init(helper);
                System.out.println("=== DATA LOADING COMPLETED SUCCESSFULLY ===");
            });
        } catch (IOException e) {
            System.err.println("=== FATAL ERROR OF DATA LOADING ===");
            e.printStackTrace();
        }
    }


    // ------------------------------------------------------------------
}
