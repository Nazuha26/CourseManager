package com.coursemanagerfx.logic.notusages;

public class JsonEventData {
    private final String creationDate;      // Дата создания ивента (строкой)
    private final String description;             // Название и текст ивента
    private final int mark;                       // Оценка за ивент
    private final String expiredDate;       // Дата истечения ивента (строкой)
    private final int StudentID;

    public JsonEventData(int StudentID, String creationDate, String description, int mark, String expiredDate) {
        this.StudentID = StudentID;
        this.creationDate = creationDate;
        this.description = description;
        this.mark = mark;
        this.expiredDate = expiredDate;
    }

    public int getStudentID() {
        return StudentID;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getDescription() {
        return description;
    }

    public int getMark() {
        return mark;
    }

    public String getExpiredDate() {
        return expiredDate;
    }
}