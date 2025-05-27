package com.coursemanagerfx.logic.basic;

import com.coursemanagerfx.logic.basic.event.StudentEvent;

import java.util.ArrayList;
import java.util.List;

public class Student {
    private String name;                // student name
    private List<StudentEvent> events;  // list of student's events
    private final int studentID;               // student id

    public Student(String name, int studentID) {
        this.name = name;
        this.studentID = studentID;
        this.events = new ArrayList<>();
    }

    public int getStudentID() {
        return studentID;
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
    public boolean equals(Object obj) {
        if (obj instanceof Student student) {
            return studentID == student.getStudentID();
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Student {\n")
                .append("  ID       : ").append(studentID).append("\n")
                .append("  Name     : ").append(name).append("\n")
                .append("  Events   :\n");

        if (events.isEmpty()) {
            sb.append("    <none>\n");
        } else {
            for (StudentEvent ev : events) {
                sb.append("    ")
                        .append(ev.toString().replace("\n", "\n    "))
                        .append("\n");
            }
        }

        sb.append("}");
        return sb.toString();
    }
}
