package com.coursemanagerfx.logic.basic.event;

import com.coursemanagerfx.logic.basic.event.date.EventDate;

import java.time.LocalDate;

public class StudentEvent {
    private final int eventID;
    private EventTypes type;
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
        this.type = before.type;
    }
    // ========================

    // === DEF CONSTRUCTOR ===
    public StudentEvent(int eventID, EventDate crtEventDate, String description, int mark, EventDate expEventDate, EventTypes type) {
        this.eventID = eventID;
        this.type = type;
        this.crtEventDate = crtEventDate;
        this.description = description;
        this.mark = mark;
        this.expEventDate = expEventDate;
    }
    // =======================

    public int getID() {
        return eventID;
    }

    public EventStatus getStatus() {
        LocalDate now = LocalDate.now();
        LocalDate expDate = LocalDate.of(expEventDate.getYear(), expEventDate.getMonth(), expEventDate.getDay());
        return expDate.isBefore(now) ? EventStatus.COMPLETED : EventStatus.ACTIVE;
    }

    public EventTypes getType() {
        return type;
    }
    public void setType(EventTypes type) {
        this.type = type;
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

    // В классе StudentEvent
    @Override
    public String toString() {
        String formattedType = type.getEventType().toString().replace("\n", "\n  ");
        return "Event {\n" +
                "  ID          : " + eventID + "\n" +
                "  Type        : " + formattedType + "\n" +
                "  Created     : " + crtEventDate + "\n" +
                "  Expires     : " + expEventDate + "\n" +
                "  Mark        : " + mark + "\n" +
                "  Description : \"" + description + "\"\n" +
                "  Status      : " + getStatus() + "\n" +
                "}";
    }
}