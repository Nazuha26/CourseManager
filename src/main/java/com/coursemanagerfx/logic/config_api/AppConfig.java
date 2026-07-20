/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic.config_api;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class AppConfig {

    /* ===== DEFAULT CONFIG ===== */

    public String open_course = "none";
    public String language = "en";
    public boolean auto_save = true;
    public int auto_save_sec_interval = 60;
    public String export_path = System.getProperty("user.home") + File.separator + "Desktop";
    public Map<String, String> category_names = defaultCategoryNames();
    public ExcelSignature excel_signature = new ExcelSignature();

    /* ========================== */

    public static Map<String, String> defaultCategoryNames() {
        Map<String, String> names = new LinkedHashMap<>();
        names.put("MOD_1", "Сесія");
        names.put("MOD_2", "СФП");
        names.put("MOD_3", "СК, КГ, КВ");
        names.put("MOD_4", "Заохочення");
        names.put("MOD_5", "Стягнення");
        names.put("MOD_6", "Секретники");
        names.put("MOD_7", "Редколегія");
        names.put("MOD_8", "Журналісти");
        names.put("MOD_9", "Спорторги");
        names.put("MOD_10", "Наукова діял.");
        names.put("MOD_11", "Сертифікати");
        names.put("MOD_12", "Призери змагань");
        names.put("MOD_13", "Волонтер. діял.");
        names.put("MOD_14", "Громад. життя");
        names.put("CUSTOM", "Додаткові бали");
        return names;
    }

    public static class ExcelSignature {
        public String title = "Начальник курсу";
        public String rank = "\"звання\"";
        public String name = "Ім'я ПРІЗВИЩЕ";
    }

}
