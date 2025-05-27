package com.coursemanagerfx.logic;

import com.coursemanagerfx.logic.basic.Group;

public class CourseInfo {
    private final String courseName;
    private String password;
    private final Group[] course;

    public CourseInfo(String courseName, String password, Group[] course) {
        this.courseName = courseName;
        this.password = password;
        this.course = course;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public Group[] getCourse() {
        return course;
    }

    public void clearPassword() {
        this.password = null;
    }
}