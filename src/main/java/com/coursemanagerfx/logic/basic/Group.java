/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic.basic;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private List<Student> students;        // Список студентов группы

    public Group() {
        this.students = new ArrayList<>();
    }

    public List<Student> getStudents() {
        return students;
    }
    public void setStudents(List<Student> students) {
        this.students = students;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Group {\n");
        for (Student student : students) {
            // Добавляем отступы для каждого студента
            sb.append("  ").append(student.toString().replace("\n", "\n  ")).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
