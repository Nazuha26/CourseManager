package com.coursemanagerfx.logic.utilities.config_api;

public class AppConfig {
    public String open_course;
    public String default_password;
    public String language;
    public boolean auto_update;
    public boolean auto_save;
    public int auto_save_sec_interval;

    /* ===== DEFAULT CONFIG ===== */
    public static AppConfig defaultConfig() {
        AppConfig config = new AppConfig();
        config.open_course = "none";
        config.default_password = "none";
        config.language = "en";
        config.auto_update = true;

        config.auto_save = true;
        config.auto_save_sec_interval = 90;

        return config;
    }
}