package com.coursemanagerfx.logic.basic.event;

public enum EventCategories {
    MOD_1     (new EventCategory(CategoryTag.SESSIYA, "Сесія")),
    MOD_2     (new EventCategory(CategoryTag.SFP, "СФП")),
    MOD_3     (new EventCategory(CategoryTag.SK_KG_KV, "СК, КГ, КВ")),
    MOD_4     (new EventCategory(CategoryTag.ZAOHOCHENNYA, "Заохочення")),
    MOD_5     (new EventCategory(CategoryTag.STYAGNENNYA, "Стягнення")),
    MOD_6     (new EventCategory(CategoryTag.SEKRETNIKI, "Секретники")),
    MOD_7     (new EventCategory(CategoryTag.REDKOLEGIYA, "Редколегія")),
    MOD_8     (new EventCategory(CategoryTag.JURNALISTI, "Журналісти")),
    MOD_9     (new EventCategory(CategoryTag.SPORTORGI, "Спорторги")),
    MOD_10    (new EventCategory(CategoryTag.NAUKA, "Наукова діял.")),
    MOD_11    (new EventCategory(CategoryTag.SERTIFICATI, "Сертифікати")),
    MOD_12    (new EventCategory(CategoryTag.ZMAGANNYA, "Призери змагань")),
    MOD_13    (new EventCategory(CategoryTag.VOLONTERI, "Волонтер. діял.")),
    MOD_14    (new EventCategory(CategoryTag.GROMAD_JITTYA, "Громад. життя")),

    CUSTOM    (new EventCategory(CategoryTag.DODAT_BAL, "Додаткові бали"));

    private final EventCategory eventCategory;
    EventCategories(EventCategory category) { this.eventCategory = category; }
    public EventCategory getEventCategory() { return eventCategory; }
}