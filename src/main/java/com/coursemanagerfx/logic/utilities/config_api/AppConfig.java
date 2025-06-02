package com.coursemanagerfx.logic.utilities.config_api;

public class AppConfig {
    public String open_course;
    public String default_password;
    public String language;
    public boolean autoSave;

    /* ===== DEFAULT CONFIG ===== */
    public static AppConfig defaultConfig() {
        AppConfig config = new AppConfig();
        config.open_course = "none";
        config.default_password = "none";
        config.language = "en";
        config.autoSave = true;

        return config;
    }
}