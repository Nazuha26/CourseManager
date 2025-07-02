package com.coursemanagerfx.logic.config_api;

import com.coursemanagerfx.AppConstants;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX;
import com.coursemanagerfx.controllers.dialogs.alert.AlertMessageType;
import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());

    /* ========== PUBLIC API ========== */



    /* ===== CONFIG GETTERS ===== */

    public static String getOpenCourse()         { return safeLoadingConfig().open_course; }

    public static String getLanguage()           { return safeLoadingConfig().language; }

    public static String getDefaultPassword()    { return safeLoadingConfig().default_password; }

    public static boolean isAutoUpdateEnabled()  { return safeLoadingConfig().auto_update; }

    public static boolean isAutoSaveEnabled()    { return safeLoadingConfig().auto_save; }

    public static int getAutoSaveSecInterval()   { return safeLoadingConfig().auto_save_sec_interval; }

    public static File getExportPath()           { return new File(safeLoadingConfig().export_path); }



    /* ===== CONFIG SETTERS ===== */

    public static void setOpenCourse(String courseName) {
        updateConfig(c -> c.open_course = courseName);
    }

    public static void setLanguage(String lang) {
        updateConfig(c -> c.language = lang);
    }

    public static void setDefaultPassword(String pass) {
        updateConfig(c -> c.default_password = pass);
    }

    public static void setAutoUpdate(boolean autoUpdate) {
        updateConfig(c -> c.auto_update = autoUpdate);
    }

    public static void setAutoSave(boolean autoSave) {
        updateConfig(c -> c.auto_save = autoSave);
    }

    public static void setAutoSaveSecInterval(int sec) {
        updateConfig(c -> c.auto_save_sec_interval = sec);
    }

    public static void setExportPath(String path) {
        updateConfig(c -> c.export_path = path);
    }

    /* ========================== */


    private static AppConfig cachedConfig;

    public static AppConfig safeLoadingConfig() {
        if (cachedConfig != null) return cachedConfig;

        AppConfig config = new AppConfig();               // default params
        AppConfig defaultConfig = new AppConfig();        // for fallback values
        boolean changed = false;
        JsonObject obj = null;

        try {
            if (Files.notExists(AppConstants.CONFIG_PATH)) {
                saveConfig(config);     // write full default config
                cachedConfig = config;
                return config;
            }

            String json = Files.readString(AppConstants.CONFIG_PATH);
            obj = JsonParser.parseString(json).getAsJsonObject();

            for (Field field : AppConfig.class.getDeclaredFields()) {
                String key = field.getName();
                field.setAccessible(true);

                if (!obj.has(key) || obj.get(key).isJsonNull()) {
                    Object defaultValue = field.get(defaultConfig);
                    obj.add(key, GSON.toJsonTree(defaultValue));
                    field.set(config, defaultValue);
                    changed = true;
                    continue;
                }

                JsonElement val = obj.get(key);

                try {
                    Class<?> type = field.getType();

                    /* --- STRING --- */
                    if (type == String.class) {
                        String strVal = val.getAsString();
                        if (strVal.isBlank()) throw new Exception();

                        /* --- exception parameters --- */
                        if (key.equals("export_path") && !Files.isDirectory(Path.of(strVal)))
                            throw new Exception();

                        field.set(config, strVal);

                        /* --- BOOLEAN --- */
                    } else if (type == boolean.class) {
                        field.setBoolean(config, val.getAsBoolean());

                        /* --- INTEGER --- */
                    } else if (type == int.class) {
                        int intVal = val.getAsInt();

                        /* --- exception parameters --- */
                        if (key.equals("auto_save_sec_interval") && intVal <= 0)
                            throw new Exception();

                        field.setInt(config, intVal);
                    }

                    /* === SETTING OTHER TYPES HERE === */
                    // ...

                } catch (Exception e) {
                    // use default value
                    Object defaultValue = field.get(defaultConfig);
                    obj.add(key, GSON.toJsonTree(defaultValue));
                    field.set(config, defaultValue);
                    changed = true;

                    LOGGER.info(String.format("Config field '%s' had invalid value and was replaced with default: \"%s\"", key, defaultValue));
                }
            }

        } catch (Exception ex) {
            AlertFX.showNotification(
                    AlertMessageType.ERROR,
                    "Configuration Error",
                    "Failed to load config:\n" + ex.getMessage()
            );
            changed = true;
        }

        if (changed) {
            if (obj != null) saveJsonConfig(obj);
            else             saveConfig(config);
        }

        cachedConfig = config;
        return config;
    }


    /* ================================ */



    /* ========== CORE ========== */

    private static void saveConfig(AppConfig config) {
        try {
            Files.createDirectories(AppConstants.CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(AppConstants.CONFIG_PATH)) { GSON.toJson(config, writer); }
        } catch (IOException ex) {
            AlertFX.showNotification(
                    AlertMessageType.ERROR,
                    "Configuration Error",
                    "Failed to save configuration:\n" + ex.getMessage()
            );
        }
    }

    private static void saveJsonConfig(JsonObject obj) {
        try {
            Files.createDirectories(AppConstants.CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(AppConstants.CONFIG_PATH)) { GSON.toJson(obj, writer); }
        } catch (IOException ex) {
            AlertFX.showNotification(
                    AlertMessageType.ERROR,
                    "Configuration Error",
                    "Failed to save configuration:\n" + ex.getMessage()
            );
        }
    }

    public static synchronized void updateConfig(Consumer<AppConfig> updater) {
        AppConfig config = safeLoadingConfig();

        AppConfig before = GSON.fromJson(GSON.toJson(config), AppConfig.class);

        try {
            JsonObject obj;
            if (Files.notExists(AppConstants.CONFIG_PATH))
                obj = new JsonObject();
            else {
                String json = Files.readString(AppConstants.CONFIG_PATH);
                obj = JsonParser.parseString(json).getAsJsonObject();
            }

            updater.accept(config);

            for (Field f : AppConfig.class.getDeclaredFields()) {
                f.setAccessible(true);
                Object oldVal = f.get(before);
                Object newVal = f.get(config);

                if (!Objects.equals(oldVal, newVal)) {

                    /* --- exception parameters --- */

                    if (f.getName().equals("export_path")) {
                        String newPath = (String) newVal;
                        if (!Files.isDirectory(Path.of(newPath))) {
                            f.set(config, oldVal);              // if there is no such directory
                            continue;
                        }
                    }


                    if (f.getName().equals("auto_save_sec_interval") && ((int)newVal) <= 0) {
                        f.set(config, oldVal);
                        continue;
                    }

                    LOGGER.info(String.format("Updated config field '%s': \"%s\" to \"%s\"", f.getName(), oldVal, newVal));

                    obj.add(f.getName(), GSON.toJsonTree(newVal));
                }
            }

            saveJsonConfig(obj);
            cachedConfig = config;

        } catch (Exception e) {
            AlertFX.showNotification(AlertMessageType.ERROR,
                    "Config Error",
                    "Failed to update config:\n" + e.getMessage());
        }
    }

}