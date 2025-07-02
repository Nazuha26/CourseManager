/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic.basic;

import com.coursemanagerfx.logic.utilities.security.CmanSecurityUtility;

public class Course {
    private String password;
    private String courseName = "Empty";
    private Group[] groups;


    public String getPassword() {
        return password;
    }
    public String getCourseName() {
        return courseName;
    }
    public Group[] getGroups() {
        return groups;
    }
    public void setGroups(Group[] groups) {
        this.groups = groups;
    }

    public Course(Group[] groups) {
        this.groups = groups;
    }

    public Course(String courseName, Group[] groups) {
        this.courseName = courseName;
        this.groups = groups;
    }

    public void generateUniquePassword() {
        password = CmanSecurityUtility.generatePassword();
        System.out.println("password=" + password);
    }
}
