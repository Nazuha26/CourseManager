package com.coursemanagerfx;

import com.coursemanagerfx.logic.basic.Group;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.stage.*;
import javafx.util.Duration;

import java.io.File;

public class CM_HELPER {

    // ===== CONSTANTS =====
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
    public static final File LAST_RUN_FILE = new File(CONFIG_DIR, template + "_" + Launcher.CUR_VERSION);
    public static final File CONFIG_FILE = new File(CONFIG_DIR, "config.json");

    //private static String new_version;
    //public static String get_new_version() { return new_version; }
    public static final String courseNameTemplate = "Рейтинг X-го курсу";
    private static String courseName;
    private static Group[] Course;

    private static int courseNumber;
    public static int getCourseNumber() {
        return courseNumber;
    }
    public static void setCourseNumber(int courseNumber) {
        CM_HELPER.courseNumber = courseNumber;
    }

    private static String generatedPassword;
    public static String getGeneratedPassword() {
        return generatedPassword;
    }
    public static void setGeneratedPassword(String generatedPassword) {
        CM_HELPER.generatedPassword = generatedPassword;
    }


    // ------------------------------------------------------------------

    public static String getCourseName() {
        return courseName;
    }
    public static void setCourseName(String courseName) {
        CM_HELPER.courseName = courseName;
    }

    public static Group[] getCourse() {
        return Course;
    }
    public static void setCourse(Group[] course) {
        CM_HELPER.Course = course;
    }

    // ------------------------------------------------------------------




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
    /*public static void actionClose(Stage stage, Runnable onFinished) {
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
    }*/

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




    // Утилитный метод для перезапуска приложения


    // ------------------------------------------------------------------
}
