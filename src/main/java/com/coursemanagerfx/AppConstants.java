package com.coursemanagerfx;

import javafx.scene.paint.Color;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppConstants {

    public static final String APP_VERSION = "1.1.2";

    //public static final File CONFIG_DIR = new File(System.getProperty("user.home"), "AppData/Local/CManFX");
    public static final Path CONFIG_PATH = Paths.get(System.getProperty("user.home"), ".cmanfx/config", "config.json");
    public static final Path COURSES_PATH = Paths.get(System.getProperty("user.home"), ".cmanfx/courses/");
    //public static final String template = "LastRun";
    //public static final File LAST_RUN_FILE = new File(CONFIG_DIR, template + "_" + CUR_VERSION);
    //public static final File CONFIG_FILE = new File(CONFIG_DIR, "config.json");
    //public static final String courseNameTemplate = "Рейтинг X-го курсу";

    public static final class ColorConstants {

        public static final Color SUCCESS_COLOUR = Color.rgb(10, 215, 30);
        public static final Color ERROR_COLOUR = Color.rgb(215, 10, 10);

        public static String toCssRGB(Color color) {
            int r = (int) (color.getRed() * 255);
            int g = (int) (color.getGreen() * 255);
            int b = (int) (color.getBlue() * 255);
            return String.format("#%02x%02x%02x", r, g, b);
        }

    }
}