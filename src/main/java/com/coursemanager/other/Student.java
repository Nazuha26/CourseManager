package com.coursemanager.other;

import java.util.ArrayList;
import java.util.List;

public class Student {
    public String name;                // Имя студента
    public List<StudentEvent> events;  // Список событий студента

    public Student(String name) {
        this.name = name;
        this.events = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Student{name='" + name + "', events=" + events + "}";
    }
}
