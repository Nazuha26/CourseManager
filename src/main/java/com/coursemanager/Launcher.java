package com.coursemanager;

import com.coursemanager.other.CmanParser;
import com.coursemanager.other.SecurityUtils;
import com.coursemanager.windows.MainCoursesWindow;
import com.coursemanager.windows.StartViewWindow;
import org.json.JSONException;
import org.json.JSONObject;
//import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;

public class Launcher {
    public static StartViewWindow startView;

    public static void main(String[] args) {
        //Locale.setDefault(Locale.of("en", "US"));   // English
        //Locale.setDefault(Locale.of("uk", "UA"));   // Ukrainian

        if (!CM_HELPER.SECRET_KEY_DIR.exists()) {
            CM_HELPER.SECRET_KEY_DIR.mkdirs();
        }
        File envFile = new File(CM_HELPER.SECRET_KEY_DIR, ".env");
        if (!envFile.exists()) {
            String randomKey = SecurityUtils.generateRandomKey(16);
            try (FileWriter writer = new FileWriter(envFile)) {
                writer.write("CMAN_SECRET_KEY=" + randomKey);
                System.out.println("Secret key successfully created");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Secret key already exists");
        }

        if (!CM_HELPER.COURSES_DIR.exists()) {
            CM_HELPER.COURSES_DIR.mkdirs();
        }
        if (!CM_HELPER.CONFIG_DIR.exists()) {
            CM_HELPER.CONFIG_DIR.mkdirs();
        }

        CM_HELPER.initTheme();  // Подключаем тему FlatLaf

        SwingUtilities.invokeLater(() -> {


            // --- Создание файла с дефолтными настройками, если не существует ---
            if (!CM_HELPER.CONFIG_FILE.exists()) {
                try {
                    JSONObject defaultConfig = new JSONObject();
                    defaultConfig.put("language", "en");
                    defaultConfig.put("country", "US");

                    try (FileWriter writer = new FileWriter(CM_HELPER.CONFIG_FILE)) {
                        writer.write(defaultConfig.toString(2)); // Красиво с отступами
                        System.out.println("The settings file is created");
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            // --- Чтение и установка локали ---
            try {
                String configContent = Files.readString(CM_HELPER.CONFIG_FILE.toPath());
                JSONObject config = new JSONObject(configContent);

                String lang = config.getString("language");
                String country = config.getString("country");
                Locale.setDefault(Locale.of(lang, country));

                System.out.println("Locale set: " + lang + "_" + country);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            // --- Запуск основного окна или стартового ---
            if (CM_HELPER.FIRST_RUN_FILE.exists()) {
                try {
                    CM_HELPER.setCourseName(Files.readString(CM_HELPER.FIRST_RUN_FILE.toPath()));

                    CM_HELPER.setFileCoursePath(CM_HELPER.COURSES_DIR + "/" + CM_HELPER.getCourseName() + ".cman");
                    CM_HELPER.setCourseGroupsList(CmanParser.parseFile(CM_HELPER.getFileCoursePath()));

                    if (!CM_HELPER.getCourseGroupsList().isEmpty()) {
                        MainCoursesWindow mainCoursesWindow = new MainCoursesWindow();
                        mainCoursesWindow.populateTabs(CM_HELPER.getCourseGroupsList());
                        mainCoursesWindow.setExtendedState(Frame.MAXIMIZED_BOTH);
                        mainCoursesWindow.setTitle("CourseManager - " + CM_HELPER.getCourseName());
                        mainCoursesWindow.setVisible(true);
                    } else {
                        showStartView();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    showStartView();
                }
            } else {
                showStartView();
            }
        });
    }

    private static void showStartView() {
        startView = new StartViewWindow();
        startView.setVisible(true);
    }
}
