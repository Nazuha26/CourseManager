package com.coursemanagerfx;

import javafx.scene.paint.Color;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.Locale;
import java.util.Properties;

public final class AppConstants {

    public static final String APP_NAME = "CourseManagerFX";
    public static final String APP_VERSION = loadApplicationVersion();
    public static final String GITHUB_REPOSITORY = "Nazuha26/CourseManager";

    public static final Path APP_DATA_PATH = Paths.get(
            System.getProperty("user.home"),
            ".cmanfx");
    public static final Path CONFIG_PATH = APP_DATA_PATH.resolve("config/config.json");
    public static final Path COURSES_PATH = APP_DATA_PATH.resolve("courses");
    public static final Path INSTANCE_LOCK_PATH = APP_DATA_PATH.resolve("coursemanagerfx.lock");

    public static final Locale UA = Locale.of("uk", "UA");
    public static final Collator UA_COLLATOR;

    static {
        UA_COLLATOR = Collator.getInstance(UA);
        UA_COLLATOR.setStrength(Collator.PRIMARY);
    }

    private AppConstants() {
    }

    private static String loadApplicationVersion() {
        try (InputStream input = AppConstants.class.getResourceAsStream(
                "/com/coursemanagerfx/build.properties")) {

            if (input == null) {
                throw new IllegalStateException("build.properties is missing");
            }

            Properties properties = new Properties();
            properties.load(input);
            String version = properties.getProperty("app.version", "").trim();
            if (!version.matches("(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)")) {
                throw new IllegalStateException("Invalid application version: " + version);
            }
            return version;
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    public static final class ColorConstants {

        public static final Color SUCCESS_COLOUR = Color.rgb(10, 215, 30);
        public static final Color ERROR_COLOUR = Color.rgb(215, 10, 10);

        private ColorConstants() {
        }

        public static String toCssRGB(Color color) {
            int r = (int) (color.getRed() * 255);
            int g = (int) (color.getGreen() * 255);
            int b = (int) (color.getBlue() * 255);
            return String.format("#%02x%02x%02x", r, g, b);
        }
    }
}
