package com.coursemanager.other;

public class StudentEvent {
    private boolean isExpired;

    public String creationDate;    // Дата создания ивента (строкой)
    public String eventDescription;// Название и текст ивента
    public int grade;              // Оценка за ивент
    public String expiredDate;     // Дата истечения ивента (строкой)

    public StudentEvent(String creationDate, String eventDescription, int grade, String expiredDate) {
        this.creationDate = creationDate;
        this.eventDescription = eventDescription;
        this.grade = grade;
        this.expiredDate = expiredDate;
        this.isExpired = true;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }

    @Override
    public String toString() {
        return "StudentEvent{" +
                "creationDate='" + creationDate + '\'' +
                ", eventDescription='" + eventDescription + '\'' +
                ", grade=" + grade +
                ", expiredDate='" + expiredDate + '\'' +
                '}';
    }
}
