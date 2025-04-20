package com.coursemanagerfx.logic.notusages;

import java.util.ArrayList;
import java.util.List;

public class DataHolder {
    private List<JsonStudentData> students;
    private List<JsonEventData> events;

    public DataHolder() {
        this.students = new ArrayList<>();
        this.events = new ArrayList<>();
    }

    public DataHolder(List<JsonStudentData> students, List<JsonEventData> events) {
        this.students = students;
        this.events = events;
    }

    public List<JsonStudentData> getStudents() {
        return students;
    }

    public void setStudents(List<JsonStudentData> students) {
        this.students = students;
    }

    public List<JsonEventData> getEvents() {
        return events;
    }

    public void setEvents(List<JsonEventData> events) {
        this.events = events;
    }
}