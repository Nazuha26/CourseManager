/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/


package com.coursemanagerfx;

import javafx.scene.paint.Color;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.Locale;

public final class AppConstants {

    public static final String APP_VERSION = "1.2.4";

    public static final Path CONFIG_PATH = Paths.get(System.getProperty("user.home"), ".cmanfx/config", "config.json");
    public static final Path COURSES_PATH = Paths.get(System.getProperty("user.home"), ".cmanfx/courses/");

    public static final Locale UA = Locale.of("uk", "UA");
    public static final Collator UA_COLLATOR;
    static {
        UA_COLLATOR = Collator.getInstance(UA);
        UA_COLLATOR.setStrength(Collator.PRIMARY);
    }

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