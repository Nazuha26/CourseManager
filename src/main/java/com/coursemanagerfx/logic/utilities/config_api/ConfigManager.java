package com.coursemanagerfx.logic.utilities.config_api;

import com.coursemanagerfx.AppConstants;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX_type;
import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.*;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /* ========== PUBLIC API ========== */



    /* ===== CONFIG GETTERS ===== */

    public static String getOpenCourse() { return safeLoadingConfig().open_course; }

    public static String getLanguage() { return safeLoadingConfig().language; }

    public static String getDefaultPassword() { return safeLoadingConfig().default_password; }

    public static boolean isAutoUpdateEnabled() { return safeLoadingConfig().auto_update; }

    public static boolean isAutoSaveEnabled() { return safeLoadingConfig().auto_save; }

    public static int getAutoSaveSecInterval() { return safeLoadingConfig().auto_save_sec_interval; }



    /* ===== CONFIG SETTERS ===== */

    public static void setOpenCourse(String courseName) {
        AppConfig config = safeLoadingConfig();
        config.open_course = courseName;
        saveConfig(config);
    }

    public static void setLanguage(String lang) {
        AppConfig config = safeLoadingConfig();
        config.language = lang;
        saveConfig(config);
    }

    public static void setDefaultPassword(String pass) {
        AppConfig config = safeLoadingConfig();
        config.default_password = pass;
        saveConfig(config);
    }

    public static void setAutoUpdate(boolean autoUpdate) {
        AppConfig config = safeLoadingConfig();
        config.auto_update = autoUpdate;
        saveConfig(config);
    }

    public static void setAutoSave(boolean autoSave) {
        AppConfig config = safeLoadingConfig();
        config.auto_save = autoSave;
        saveConfig(config);
    }

    public static void setAutoSaveSecInterval(int sec) {
        AppConfig config = safeLoadingConfig();
        config.auto_save_sec_interval = sec;
        saveConfig(config);
    }

    /* ========================== */


    private static AppConfig cachedConfig;

    public static AppConfig safeLoadingConfig() {
        if (cachedConfig != null) return cachedConfig;

        AppConfig config = new AppConfig();     // default params
        boolean changed = false;

        try {
            if (Files.notExists(AppConstants.CONFIG_PATH)) {
                saveConfig(config);
                cachedConfig = config;
                return config;
            }

            String json = Files.readString(AppConstants.CONFIG_PATH);
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            for (Field field : AppConfig.class.getDeclaredFields()) {
                String key = field.getName();
                if (!obj.has(key)) {
                    changed = true;
                    continue;
                }

                JsonElement val = obj.get(key);
                if (val == null || val.isJsonNull()) {
                    changed = true;
                    continue;
                }

                field.setAccessible(true);

                try {
                    /* --- STRING --- */
                    if (field.getType() == String.class) {
                        if (val.isJsonPrimitive() && val.getAsJsonPrimitive().isString()) {
                            field.set(config, val.getAsString());
                        } else {
                            changed = true;
                        }
                    }
                    /* --- BOOLEAN --- */
                    else if (field.getType() == boolean.class) {
                        if (val.isJsonPrimitive() && val.getAsJsonPrimitive().isBoolean()) {
                            field.setBoolean(config, val.getAsBoolean());
                        } else {
                            changed = true;
                        }
                    }
                    /* --- INTEGER --- */
                    else if (field.getType() == int.class) {
                        if (val.isJsonPrimitive() && val.getAsJsonPrimitive().isNumber()) {
                            int value = val.getAsInt();

                            /* --- exception parameters --- */
                            if (key.equals("auto_save_sec_interval") && value <= 0) {
                                changed = true;
                            }

                            else field.setInt(config, value);
                            /* ------------------ */

                        } else changed = true;
                    }

                    /* === SETTING OTHER TYPES HERE === */
                    // ...

                } catch (Exception e) {
                    changed = true;
                }
            }

        } catch (Exception ex) {
            AlertFX.showNotification(null, AlertFX_type.ERROR, "Configuration Error",
                    "Failed to load config:\n" + ex.getMessage(), true);
            changed = true;
        }

        if (changed) saveConfig(config);

        cachedConfig = config;
        return config;
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
