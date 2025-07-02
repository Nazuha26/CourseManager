/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic.config_api;

import java.io.File;

public class AppConfig {

    /* ===== DEFAULT CONFIG ===== */

    public String open_course =          "none";
    public String default_password =     "none";
    public String language =             "en";
    public boolean auto_update =         true;
    public boolean auto_save =           true;
    public int auto_save_sec_interval =  60;
    public String export_path = System.getProperty("user.home") + File.separator + "Desktop";

    /* ========================== */

}
