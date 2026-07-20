package com.coursemanagerfx.logic.session;

import com.coursemanagerfx.Launcher;

/** Allocates the monotonic IDs owned by the currently open course. */
public final class CourseIdGenerator {

    public int nextStudentId() {
        return Launcher.getCourseInfo().takeNextStudentId();
    }

    public int nextEventId() {
        return Launcher.getCourseInfo().takeNextEventId();
    }
}
