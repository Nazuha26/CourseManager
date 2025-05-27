package com.coursemanagerfx.logic.basic.event;

public enum EventTypes {
    MOD_1     (new EventType(TypeTag.SESSIYA, "Сесія")),
    MOD_2     (new EventType(TypeTag.SFP, "СФП")),
    MOD_3     (new EventType(TypeTag.SK_KG_KV, "СК, КГ, КВ")),
    MOD_4     (new EventType(TypeTag.ZAOHOCHENNYA, "Заохочення")),
    MOD_5     (new EventType(TypeTag.STYAGNENNYA, "Стягнення")),
    MOD_6     (new EventType(TypeTag.SEKRETNIKI, "Секретники")),
    MOD_7     (new EventType(TypeTag.REDKOLEGIYA, "Редколегія")),
    MOD_8     (new EventType(TypeTag.JURNALISTI, "Журналісти")),
    MOD_9     (new EventType(TypeTag.SPORTORGI, "Спорторги")),
    MOD_10    (new EventType(TypeTag.NAUKA, "Наукова діял.")),
    MOD_11    (new EventType(TypeTag.SERTIFICATI, "Сертифікати")),
    MOD_12    (new EventType(TypeTag.ZMAGANNYA, "Призери змагань")),
    MOD_13    (new EventType(TypeTag.VOLONTERI, "Волонтер. діял.")),
    MOD_14    (new EventType(TypeTag.GROMAD_JITTYA, "Громад. життя")),

    CUSTOM    (new EventType(TypeTag.DODAT_BAL, "Додаткові бали"));

    private final EventType eventType;
    EventTypes(EventType type) { this.eventType = type; }
    public EventType getEventType() { return eventType; }
}