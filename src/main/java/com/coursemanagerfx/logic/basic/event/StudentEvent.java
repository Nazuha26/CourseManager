package com.coursemanagerfx.logic.basic.event;

import com.coursemanagerfx.logic.basic.event.date.EventDate;

import java.time.LocalDate;

public class StudentEvent {
    private boolean isExpired;

    private final int eventID;
    private EventDate crtEventDate;         // Дата создания ивента
    private String description;             // Название и текст ивента
    private int mark;                       // Оценка за ивент
    private EventDate expEventDate;         // Дата истечения ивента

    // === COPY CONSTRUCTOR ===
    public StudentEvent(StudentEvent before) {
        this.eventID = before.eventID;
        this.crtEventDate = before.crtEventDate;
        this.description = before.description;
        this.mark = before.mark;
        this.expEventDate = before.expEventDate;

        this.isExpired = before.isExpired;
    }
    // ========================

    // === PRESET CONSTRUCTOR ===
    public StudentEvent(int eventID, EventDate crtEventDate, String description, EventMods mod) {
        this.eventID = eventID;
        this.crtEventDate = crtEventDate;
        this.description = description;
        this.mark = mod.getPreset().getMark();
        this.isExpired = false;

        LocalDate base = LocalDate.of(
                crtEventDate.getYear(), crtEventDate.getMonth(), crtEventDate.getDay()
        );
        LocalDate expiration = mod.getPreset().getRule().apply(base);
        this.expEventDate = new EventDate(
                expiration.getDayOfMonth(),
                expiration.getMonthValue(),
                expiration.getYear()
        );
        updateExpiredStatus();
    }
    // ==========================

    // === DEF CONSTRUCTOR ===
    public StudentEvent(int eventID, EventDate crtEventDate, String description, int mark, EventDate expEventDate) {
        this.eventID = eventID;
        this.crtEventDate = crtEventDate;
        this.description = description;
        this.mark = mark;
        this.expEventDate = expEventDate;

        this.isExpired = false;
    }
    // =======================


    public int getID() {
        return eventID;
    }

    public EventStatus getStatus() {
        return isExpired ? EventStatus.COMPLETED : EventStatus.ACTIVE;
    }

    public boolean isExpired() {
        return isExpired;
    }
    public void setExpired(boolean expired) {
        isExpired = expired;
    }
    public void updateExpiredStatus() {
        LocalDate now = LocalDate.now();
        LocalDate exp = LocalDate.of(expEventDate.getYear(),
                                    expEventDate.getMonth(),
                                    expEventDate.getDay());
        this.isExpired = exp.isBefore(now);
    }

    public EventDate getCrtDate() {
        return crtEventDate;
    }
    public void setCrtDate(EventDate crtEventDate) {
        this.crtEventDate = crtEventDate;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public int getMark() {
        return mark;
    }
    public void setMark(int mark) {
        this.mark = mark;
    }

    public EventDate getExpDate() {
        return expEventDate;
    }
    public void setExpDate(EventDate expEventDate) {
        this.expEventDate = expEventDate;
    }

    @Override
    public String toString() {
        return "\t\tEvent {\n" +
                "\t\t\teventID = " + eventID + ",\n" +
                "\t\t\tcreationDate = '" + crtEventDate + "',\n" +
                "\t\t\tdescription = \"" + description + "\",\n" +
                "\t\t\tmark = " + mark + ",\n" +
                "\t\t\texpiredDate = '" + expEventDate + "'\n" +
                "\t\t}";
    }
}
