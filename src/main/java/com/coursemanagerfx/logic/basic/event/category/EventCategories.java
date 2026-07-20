/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic.basic.event.category;

import com.coursemanagerfx.logic.config_api.ConfigManager;
import javafx.scene.paint.Color;

import java.util.Arrays;

public enum EventCategories {
    MOD_1     (1, new EventCategory(CategoryTag.SESSIYA, "Сесія", Color.rgb(223, 12, 12, 0.1))),
    MOD_2     (2, new EventCategory(CategoryTag.SFP, "СФП", Color.rgb(223, 107, 12, 0.1))),
    MOD_3     (3, new EventCategory(CategoryTag.SK_KG_KV, "СК, КГ, КВ", Color.rgb(223, 188, 12, 0.1))),
    MOD_4     (4, new EventCategory(CategoryTag.ZAOHOCHENNYA, "Заохочення", Color.rgb(163, 223, 12, 0.1))),
    MOD_5     (5, new EventCategory(CategoryTag.STYAGNENNYA, "Стягнення", Color.rgb(96, 223, 12, 0.1))),
    MOD_6     (6, new EventCategory(CategoryTag.SEKRETNIKI, "Секретники", Color.rgb(19, 223, 12, 0.1))),
    MOD_7     (7, new EventCategory(CategoryTag.REDKOLEGIYA, "Редколегія", Color.rgb(12, 223, 86, 0.1))),
    MOD_8     (8, new EventCategory(CategoryTag.JURNALISTI, "Журналісти", Color.rgb(12, 223, 198, 0.1))),
    MOD_9     (9, new EventCategory(CategoryTag.SPORTORGI, "Спорторги", Color.rgb(12, 167, 223, 0.1))),
    MOD_10    (10, new EventCategory(CategoryTag.NAUKA, "Наукова діял.", Color.rgb(12, 96, 223, 0.1))),
    MOD_11    (11, new EventCategory(CategoryTag.SERTIFICATI, "Сертифікати", Color.rgb(19, 12, 223, 0.1))),
    MOD_12    (12, new EventCategory(CategoryTag.ZMAGANNYA, "Призери змагань", Color.rgb(96, 12, 223, 0.1))),
    MOD_13    (13, new EventCategory(CategoryTag.VOLONTERI, "Волонтер. діял.", Color.rgb(209, 12, 223, 0.1))),
    MOD_14    (14, new EventCategory(CategoryTag.GROMAD_JITTYA, "Громад. життя", Color.rgb(223, 12, 139, 0.1))),

    CUSTOM    (15, new EventCategory(CategoryTag.DODAT_BAL, "Додаткові бали", Color.rgb(223, 12, 44, 0.1)));

    private final int code;
    private final EventCategory eventCategory;

    EventCategories(int code, EventCategory category) {
        this.code = code;
        this.eventCategory = category;
    }

    /** Stable value written to CMAN files. Never derive this value from ordinal(). */
    public int getCode() {
        return code;
    }

    public EventCategory getEventCategory() { return eventCategory; }

    public String getDisplayName() {
        return ConfigManager.getCategoryName(name(), eventCategory.name());
    }

    public static EventCategories fromCode(int code) {
        return Arrays.stream(values())
                .filter(category -> category.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown event category code: " + code));
    }
}
