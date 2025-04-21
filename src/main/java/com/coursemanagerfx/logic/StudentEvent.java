package com.coursemanagerfx.logic;

import com.coursemanagerfx.logic.notusages.JsonEventData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class StudentEvent {
    private boolean isExpired;

    private final int eventID;
    private final String creationDate;      // Дата создания ивента (строкой)
    private String description;             // Название и текст ивента
    private int mark;                       // Оценка за ивент
    private final String expiredDate;       // Дата истечения ивента (строкой)

    public StudentEvent(int eventID, String creationDate, String description, int mark, String expiredDate) {
        this.eventID = eventID;
        this.creationDate = creationDate;
        this.description = description;
        this.mark = mark;
        this.expiredDate = expiredDate;
        this.isExpired = false;
    }

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
        try {
            String rawDate = expiredDate.split(" ")[0];
            LocalDate expDate = LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            this.isExpired = expDate.isBefore(LocalDate.now());
        } catch (Exception e) {
            this.isExpired = false;
        }
    }

    public String getCreationDate() {
        return creationDate;
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

    public String getExpiredDate() {
        return expiredDate;
    }

    public JsonEventData toJsonEventDataEvent(int studentID) {
        return new JsonEventData(
                studentID,
                this.getCreationDate(),
                this.getDescription(),
                this.getMark(),
                this.getExpiredDate()
        );
    }

    @Override
    public String toString() {
        return "\t\tEvent {\n" +
                "\t\t\teventID='" + eventID + '\n' +
                "\t\t\tcreationDate='" + creationDate + '\n' +
                "\t\t\tdescription='" + description + '\n' +
                "\t\t\tmark=" + mark + '\n' +
                "\t\t\texpiredDate='" + expiredDate + '\n' +
                "}\n";
    }
}
