/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic.basic.event.category;

import javafx.scene.paint.Color;

public enum EventCategories {
    MOD_1     (new EventCategory(CategoryTag.SESSIYA, "Сесія", Color.rgb(223, 12, 12, 0.1))),
    MOD_2     (new EventCategory(CategoryTag.SFP, "СФП", Color.rgb(223, 107, 12, 0.1))),
    MOD_3     (new EventCategory(CategoryTag.SK_KG_KV, "СК, КГ, КВ", Color.rgb(223, 188, 12, 0.1))),
    MOD_4     (new EventCategory(CategoryTag.ZAOHOCHENNYA, "Заохочення", Color.rgb(163, 223, 12, 0.1))),
    MOD_5     (new EventCategory(CategoryTag.STYAGNENNYA, "Стягнення", Color.rgb(96, 223, 12, 0.1))),
    MOD_6     (new EventCategory(CategoryTag.SEKRETNIKI, "Секретники", Color.rgb(19, 223, 12, 0.1))),
    MOD_7     (new EventCategory(CategoryTag.REDKOLEGIYA, "Редколегія", Color.rgb(12, 223, 86, 0.1))),
    MOD_8     (new EventCategory(CategoryTag.JURNALISTI, "Журналісти", Color.rgb(12, 223, 198, 0.1))),
    MOD_9     (new EventCategory(CategoryTag.SPORTORGI, "Спорторги", Color.rgb(12, 167, 223, 0.1))),
    MOD_10    (new EventCategory(CategoryTag.NAUKA, "Наукова діял.", Color.rgb(12, 96, 223, 0.1))),
    MOD_11    (new EventCategory(CategoryTag.SERTIFICATI, "Сертифікати", Color.rgb(19, 12, 223, 0.1))),
    MOD_12    (new EventCategory(CategoryTag.ZMAGANNYA, "Призери змагань", Color.rgb(96, 12, 223, 0.1))),
    MOD_13    (new EventCategory(CategoryTag.VOLONTERI, "Волонтер. діял.", Color.rgb(209, 12, 223, 0.1))),
    MOD_14    (new EventCategory(CategoryTag.GROMAD_JITTYA, "Громад. життя", Color.rgb(223, 12, 139, 0.1))),

    CUSTOM    (new EventCategory(CategoryTag.DODAT_BAL, "Додаткові бали", Color.rgb(223, 12, 44, 0.1)));

    private final EventCategory eventCategory;
    EventCategories(EventCategory category) { this.eventCategory = category; }
    public EventCategory getEventCategory() { return eventCategory; }
}
