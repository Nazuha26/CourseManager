/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic;

import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.basic.Student;
import com.coursemanagerfx.logic.basic.event.StudentEvent;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public class CourseInfo {
    private final File courseFile;
    private final String courseName;
    private char[] seedPhrase;
    private final Group[] course;
    private int nextStudentId;
    private int nextEventId;

    public CourseInfo(File courseFile, char[] seedPhrase, Group[] course) {
        this(
                courseFile,
                seedPhrase,
                course,
                deriveNextStudentId(course),
                deriveNextEventId(course));
    }

    public CourseInfo(
            File courseFile,
            char[] seedPhrase,
            Group[] course,
            int nextStudentId,
            int nextEventId) {

        this.courseFile = Objects.requireNonNull(courseFile, "courseFile")
                .getAbsoluteFile();
        String fileName = this.courseFile.getName();
        this.courseName = fileName.endsWith(".cman")
                ? fileName.substring(0, fileName.length() - 5)
                : fileName;
        Objects.requireNonNull(seedPhrase, "seedPhrase");
        this.seedPhrase = Arrays.copyOf(seedPhrase, seedPhrase.length);
        this.course = Objects.requireNonNull(course, "course");
        validateNextIds(course, nextStudentId, nextEventId);
        this.nextStudentId = nextStudentId;
        this.nextEventId = nextEventId;
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

    public synchronized int takeNextStudentId() {
        if (nextStudentId == Integer.MAX_VALUE) {
            throw new IllegalStateException("No more student IDs are available");
        }
        return nextStudentId++;
    }

    public synchronized int takeNextEventId() {
        if (nextEventId == Integer.MAX_VALUE) {
            throw new IllegalStateException("No more event IDs are available");
        }
        return nextEventId++;
    }

    public synchronized int getNextStudentId() {
        return nextStudentId;
    }

    public synchronized int getNextEventId() {
        return nextEventId;
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

    private static int deriveNextStudentId(Group[] groups) {
        int maximum = 999_999;
        if (groups != null) {
            for (Group group : groups) {
                if (group == null || group.getStudents() == null) continue;
                for (Student student : group.getStudents()) {
                    if (student != null) maximum = Math.max(maximum, student.getStudentID());
                }
            }
        }
        return increment(maximum, "student");
    }

    private static int deriveNextEventId(Group[] groups) {
        int maximum = 9_999;
        if (groups != null) {
            for (Group group : groups) {
                if (group == null || group.getStudents() == null) continue;
                for (Student student : group.getStudents()) {
                    if (student == null || student.getEvents() == null) continue;
                    for (StudentEvent event : student.getEvents()) {
                        if (event != null) maximum = Math.max(maximum, event.getID());
                    }
                }
            }
        }
        return increment(maximum, "event");
    }

    private static int increment(int value, String type) {
        if (value == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("No more " + type + " IDs are available");
        }
        return value + 1;
    }

    private static void validateNextIds(
            Group[] groups,
            int nextStudentId,
            int nextEventId) {

        int minimumStudentId = deriveNextStudentId(groups);
        int minimumEventId = deriveNextEventId(groups);
        if (nextStudentId < minimumStudentId || nextEventId < minimumEventId) {
            throw new IllegalArgumentException("Persisted next ID is lower than an existing ID");
        }
    }
}
