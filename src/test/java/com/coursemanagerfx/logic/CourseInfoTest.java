package com.coursemanagerfx.logic;

import com.coursemanagerfx.logic.basic.Group;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CourseInfoTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void keepsActualCoursePathAndOwnsSeedPhraseCopy() {
        Path coursePath = temporaryDirectory.resolve("external course.cman");
        char[] originalPhrase = "abacus abdomen abdominal abide abiding".toCharArray();
        char[] expectedPhrase = Arrays.copyOf(originalPhrase, originalPhrase.length);

        CourseInfo info = new CourseInfo(
                coursePath.toFile(),
                originalPhrase,
                new Group[0]);
        Arrays.fill(originalPhrase, '\0');

        assertEquals(coursePath.toAbsolutePath().toFile(), info.getCourseFile());
        assertEquals("external course", info.getCourseName());

        char[] storedPhrase = info.copySeedPhrase();
        assertNotNull(storedPhrase);
        try {
            assertArrayEquals(expectedPhrase, storedPhrase);
        } finally {
            Arrays.fill(storedPhrase, '\0');
            Arrays.fill(expectedPhrase, '\0');
        }

        info.clearSeedPhrase();
        assertNull(info.copySeedPhrase());
    }

    @Test
    void allocatesMonotonicIdsWithoutReusingDeletedObjects() {
        char[] phrase = "abacus abdomen abdominal abide abiding".toCharArray();
        CourseInfo info = new CourseInfo(
                temporaryDirectory.resolve("ids.cman").toFile(),
                phrase,
                new Group[] {new Group()});
        Arrays.fill(phrase, '\0');

        int firstStudent = info.takeNextStudentId();
        int secondStudent = info.takeNextStudentId();
        int firstEvent = info.takeNextEventId();
        int secondEvent = info.takeNextEventId();

        assertEquals(firstStudent + 1, secondStudent);
        assertEquals(firstEvent + 1, secondEvent);
        assertNotEquals(firstStudent, secondStudent);
        assertNotEquals(firstEvent, secondEvent);
        assertEquals(secondStudent + 1, info.getNextStudentId());
        assertEquals(secondEvent + 1, info.getNextEventId());
    }
}
