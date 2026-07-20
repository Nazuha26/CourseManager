/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic.config_api;

import com.coursemanagerfx.AppConstants;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX;
import com.coursemanagerfx.controllers.dialogs.alert.AlertMessageType;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.Objects;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.function.Consumer;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);

    /* ========== PUBLIC API ========== */



    /* ===== CONFIG GETTERS ===== */

    public static String getOpenCourse()         { return safeLoadingConfig().open_course; }

    public static String getLanguage()           { return safeLoadingConfig().language; }

    public static boolean isAutoSaveEnabled()    { return safeLoadingConfig().auto_save; }

    public static int getAutoSaveSecInterval()   { return safeLoadingConfig().auto_save_sec_interval; }

    public static File getExportPath()           { return new File(safeLoadingConfig().export_path); }

    public static String getCategoryName(String key, String defaultName) {
        String configured = safeLoadingConfig().category_names.get(key);
        return configured == null || configured.isBlank() ? defaultName : configured;
    }

    public static String getExcelSignatureTitle() { return safeLoadingConfig().excel_signature.title; }

    public static String getExcelSignatureRank()  { return safeLoadingConfig().excel_signature.rank; }

    public static String getExcelSignatureName()  { return safeLoadingConfig().excel_signature.name; }



    /* ===== CONFIG SETTERS ===== */

    public static void setOpenCourse(String courseName) {
        updateConfig(c -> c.open_course = courseName);
    }

    public static void setLanguage(String lang) {
        updateConfig(c -> c.language = lang);
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
        boolean rewriteRequired = false;

        try {
            if (Files.notExists(AppConstants.CONFIG_PATH)) {
                saveConfig(config);     // write full default config
                cachedConfig = config;
                return config;
            }

            String json = Files.readString(AppConstants.CONFIG_PATH);
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            // Only fields declared in AppConfig are read. Unknown fields are ignored.
            for (Field field : AppConfig.class.getDeclaredFields()) {
                String key = field.getName();
                field.setAccessible(true);

                if (!obj.has(key) || obj.get(key).isJsonNull()) {
                    Object defaultValue = field.get(defaultConfig);
                    field.set(config, defaultValue);
                    rewriteRequired = true;
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
                    } else {
                        Object parsed = GSON.fromJson(val, field.getGenericType());
                        if (parsed == null) throw new Exception();
                        field.set(config, parsed);
                    }

                    /* === SETTING OTHER TYPES HERE === */
                    // ...

                } catch (Exception e) {
                    // use default value
                    Object defaultValue = field.get(defaultConfig);
                    field.set(config, defaultValue);
                    rewriteRequired = true;

                    LOGGER.info(
                            "Config field '{}' had an invalid value and was replaced with default: {}",
                            key,
                            defaultValue);
                }
            }

            rewriteRequired |= normalizeStructuredFields(config, defaultConfig);

        } catch (Exception ex) {
            LOGGER.error("Failed to load configuration", ex);
            AlertFX.showNotification(
                    AlertMessageType.ERROR,
                    "Configuration Error",
                    "Failed to load config:\n" + ex.getMessage()
            );
            rewriteRequired = true;
        }

        if (rewriteRequired) saveConfig(config);

        cachedConfig = config;
        return config;
    }

    /* ================================ */



    /* ========== CORE ========== */

    private static boolean normalizeStructuredFields(
            AppConfig config,
            AppConfig defaults) {

        boolean changed = false;

        if (config.category_names == null) {
            config.category_names = AppConfig.defaultCategoryNames();
            changed = true;
        } else {
            Map<String, String> normalized = new LinkedHashMap<>();
            for (Map.Entry<String, String> entry : defaults.category_names.entrySet()) {
                String value = config.category_names.get(entry.getKey());
                if (value == null || value.isBlank()) {
                    value = entry.getValue();
                    changed = true;
                }
                normalized.put(entry.getKey(), value);
            }
            if (new HashSet<>(normalized.values()).size() != normalized.size()) {
                normalized = new LinkedHashMap<>(defaults.category_names);
                changed = true;
            }
            config.category_names = normalized;
        }

        if (config.excel_signature == null) {
            config.excel_signature = new AppConfig.ExcelSignature();
            changed = true;
        } else {
            changed |= replaceBlankSignatureFields(
                    config.excel_signature,
                    defaults.excel_signature);
        }

        return changed;
    }

    private static boolean replaceBlankSignatureFields(
            AppConfig.ExcelSignature signature,
            AppConfig.ExcelSignature defaults) {

        boolean changed = false;
        if (signature.title == null || signature.title.isBlank()) {
            signature.title = defaults.title;
            changed = true;
        }
        if (signature.rank == null || signature.rank.isBlank()) {
            signature.rank = defaults.rank;
            changed = true;
        }
        if (signature.name == null || signature.name.isBlank()) {
            signature.name = defaults.name;
            changed = true;
        }
        return changed;
    }

    private static void saveConfig(AppConfig config) {
        try {
            Files.createDirectories(AppConstants.CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(AppConstants.CONFIG_PATH)) { GSON.toJson(config, writer); }
        } catch (IOException ex) {
            LOGGER.error("Failed to save configuration", ex);
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

                    LOGGER.info(
                            "Updated config field '{}': '{}' to '{}'",
                            f.getName(),
                            oldVal,
                            newVal);
                }
            }

            // Rewriting from AppConfig intentionally removes unknown JSON fields.
            saveConfig(config);
            cachedConfig = config;

        } catch (Exception e) {
            LOGGER.error("Failed to update configuration", e);
            AlertFX.showNotification(AlertMessageType.ERROR,
                    "Config Error",
                    "Failed to update config:\n" + e.getMessage());
        }
    }

}
