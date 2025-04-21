package com.coursemanagerfx.logic;

import java.util.ArrayList;
import java.util.List;

public class Student {
    private String name;                // Имя студента
    private List<StudentEvent> events;  // Список событий студента
    private final int ID;
    private boolean isSelected;

    public Student(String name) {
        this.name = name;
        this.events = new ArrayList<>();
        this.ID = (int)(Math.random() * 1000000);   // генерирует число от 0 до 999999 (шанс 0,0001%)
        this.isSelected = false;
    }

    public Student(String name, int ID) {
        this.name = name;
        this.ID = ID;
        this.events = new ArrayList<>();
        this.isSelected = false;
    }

    public int getID() {
        return ID;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public List<StudentEvent> getEvents() {
        return events;
    }
    public void setEvents(List<StudentEvent> events) {
        this.events = events;
    }

    @Override
    public String toString() {
        return "Student { " +
                "ID='" + ID + "'" +
                ", name='" + name + "'\n" +
                "\tevents=\n" + events + " }\n";
    }
}
