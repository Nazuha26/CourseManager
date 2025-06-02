package com.coursemanagerfx.logic.utilities.config_api;

import com.coursemanagerfx.AppConstants;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX_type;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.*;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /* ========== PUBLIC API ========== */



    /* ===== CONFIG GETTERS ===== */

    public static String getOpenCourse() { return loadConfig().open_course; }

    public static String getLanguage() { return loadConfig().language; }

    public static boolean isAutoSaveEnabled() { return loadConfig().autoSave; }

    public static String getDefaultPassword() { return loadConfig().default_password; }



    /* ===== CONFIG SETTERS ===== */

    public static void setOpenCourse(String courseName) {
        AppConfig config = loadConfig();
        config.open_course = courseName;
        saveConfig(config);
    }

    public static void setLanguage(String lang) {
        AppConfig config = loadConfig();
        config.language = lang;
        saveConfig(config);
    }

    public static void setAutoSave(boolean autoSave) {
        AppConfig config = loadConfig();
        config.autoSave = autoSave;
        saveConfig(config);
    }

    public static void setDefaultPassword(String pass) {
        AppConfig config = loadConfig();
        config.default_password = pass;
        saveConfig(config);
    }
    /* ========================== */



    public static AppConfig loadConfig() {
        try {
            if (Files.notExists(AppConstants.CONFIG_PATH)) {
                saveConfig(AppConfig.defaultConfig());
                return AppConfig.defaultConfig();
            }

            Reader reader = Files.newBufferedReader(AppConstants.CONFIG_PATH);
            return GSON.fromJson(reader, AppConfig.class);
        } catch (IOException ex) {
            AlertFX.showNotification(
                    null,
                    AlertFX_type.ERROR,
                    "Configuration Error",
                    "Failed to load configuration:\n" + ex.getMessage(),
                    true
            );
            return AppConfig.defaultConfig(); // fallback
        }
    }

    /* ================================ */



    /* ========== CORE ========== */

    private static void saveConfig(AppConfig config) {
        try {
            Files.createDirectories(AppConstants.CONFIG_PATH.getParent());
            Writer writer = Files.newBufferedWriter(AppConstants.CONFIG_PATH);
            GSON.toJson(config, writer);
            writer.close();
        } catch (IOException ex) {
            AlertFX.showNotification(
                    null,
                    AlertFX_type.ERROR,
                    "Configuration Error",
                    "Failed to save configuration:\n" + ex.getMessage(),
                    true
            );
        }
    }

}
