package com.coursemanager.other;

import java.util.ArrayList;
import java.util.List;

public class Group {
    public List<Student> students;        // Список студентов группы

    public Group() {
        this.students = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Group{students=" + students + "}";
    }
}
