/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic.basic.event;

import com.coursemanagerfx.logic.basic.event.category.EventCategories;
import com.coursemanagerfx.logic.basic.event.date.EventDate;

import java.time.LocalDate;

public class StudentEvent {
    private final int eventID;
    private EventCategories category;
    private EventDate crtEventDate;         // Дата создания ивента
    private String description;             // Название и текст ивента
    private double mark;                    // Оценка за ивент
    private EventDate expEventDate;         // Дата истечения ивента

    // === COPY CONSTRUCTOR ===
    public StudentEvent(StudentEvent before) {
        this.eventID = before.eventID;
        this.crtEventDate = before.crtEventDate;
        this.description = before.description;
        this.mark = before.mark;
        this.expEventDate = before.expEventDate;
        this.category = before.category;
    }
    // ========================

    // === DEF CONSTRUCTOR ===
    public StudentEvent(int eventID, EventDate crtEventDate, String description, double mark, EventDate expEventDate, EventCategories category) {
        this.eventID = eventID;
        this.category = category;
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

    public EventCategories getCategory() {
        return category;
    }
    public void setCategory(EventCategories category) {
        this.category = category;
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

    public double getMark() {
        return mark;
    }
    public void setMark(double mark) {
        this.mark = mark;
    }

    public EventDate getExpDate() {
        return expEventDate;
    }
    public void setExpDate(EventDate expEventDate) {
        this.expEventDate = expEventDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StudentEvent that = (StudentEvent) o;

        return  eventID == that.eventID &&
                Double.compare(that.mark, mark) == 0 &&
                crtEventDate.equals(that.crtEventDate) &&
                description.equals(that.description) &&
                expEventDate.equals(that.expEventDate) &&
                category.equals(that.category);
    }

    @Override
    public int hashCode()
        { return java.util.Objects.hash(eventID, category, crtEventDate, description, mark, expEventDate); }

    @Override
    public String toString() {
        String formattedCategory = category.getEventCategory().toString().replace("\n", "\n  ");
        return "Event {\n" +
                "  ID          : " + eventID + "\n" +
                "  Category    : " + formattedCategory + "\n" +
                "  Created     : " + crtEventDate + "\n" +
                "  Expires     : " + expEventDate + "\n" +
                "  Mark        : " + mark + "\n" +
                "  Description : \"" + description + "\"\n" +
                "  Status      : " + getStatus() + "\n" +
                "}";
    }
}
