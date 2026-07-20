package com.coursemanagerfx.logic;

import com.coursemanagerfx.logic.basic.Group;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
}
