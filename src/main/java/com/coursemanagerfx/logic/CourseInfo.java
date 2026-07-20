/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic;

import com.coursemanagerfx.logic.basic.Group;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public class CourseInfo {
    private final File courseFile;
    private final String courseName;
    private char[] seedPhrase;
    private final Group[] course;

    public CourseInfo(File courseFile, char[] seedPhrase, Group[] course) {
        this.courseFile = Objects.requireNonNull(courseFile, "courseFile")
                .getAbsoluteFile();
        String fileName = this.courseFile.getName();
        this.courseName = fileName.endsWith(".cman")
                ? fileName.substring(0, fileName.length() - 5)
                : fileName;
        Objects.requireNonNull(seedPhrase, "seedPhrase");
        this.seedPhrase = Arrays.copyOf(seedPhrase, seedPhrase.length);
        this.course = Objects.requireNonNull(course, "course");
    }

    public File getCourseFile() {
        return courseFile;
    }

    public String getCourseName() {
        return courseName;
    }

    public char[] copySeedPhrase() {
        if (seedPhrase == null) return null;
        return Arrays.copyOf(seedPhrase, seedPhrase.length);
    }

    public Group[] getCourse() {
        return course;
    }

    public void clearSeedPhrase() {
        if (seedPhrase != null) {
            Arrays.fill(seedPhrase, '\0');
            seedPhrase = null;
        }
    }

    public boolean isEmpty() {
        if (course == null) return true;
        for (Group group : course) {
            if (group != null && group.getStudents() != null && !group.getStudents().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
