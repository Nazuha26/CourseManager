package com.coursemanagerfx.logic.utilities.security;

import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.basic.Student;
import com.coursemanagerfx.logic.basic.event.StudentEvent;
import com.coursemanagerfx.logic.basic.event.category.EventCategories;
import com.coursemanagerfx.logic.basic.event.date.EventDate;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BinaryPlainCmanUtilityTest {

    @Test
    void categoryCodesAreExplicitAndStable() {
        EventCategories[] categories = EventCategories.values();
        assertEquals(15, categories.length);
        for (int i = 0; i < categories.length; i++) {
            int expectedCode = i + 1;
            assertEquals(expectedCode, categories[i].getCode());
            assertEquals(categories[i], EventCategories.fromCode(expectedCode));
        }
        assertThrows(IllegalArgumentException.class,
                () -> EventCategories.fromCode(0));
    }

    @Test
    void roundTripKeepsIndependentTextOffsetsAndNextIds() throws Exception {
        Group group = new Group();
        Student student = new Student("Іван Петренко", 1_000_007);
        String descriptionEqualToDate = "01.02.2026";
        student.getEvents().add(new StudentEvent(
                10_042,
                new EventDate(1, 2, 2026),
                descriptionEqualToDate,
                4.5,
                new EventDate(1, 3, 2026),
                EventCategories.MOD_9));
        group.getStudents().add(student);

        byte[] payload = BinaryPlainCmanUtility.buildPlainCman(
                new Group[] {group},
                1_000_100,
                10_100);
        BinaryPlainCmanUtility.CourseData restored =
                BinaryPlainCmanUtility.parseCourseData(
                        new ByteArrayInputStream(payload));

        Student restoredStudent = restored.groups()[0].getStudents().get(0);
        StudentEvent restoredEvent = restoredStudent.getEvents().get(0);
        assertEquals("Іван Петренко", restoredStudent.getName());
        assertEquals(descriptionEqualToDate, restoredEvent.getDescription());
        assertEquals(EventCategories.MOD_9, restoredEvent.getCategory());
        assertEquals(1_000_100, restored.nextStudentId());
        assertEquals(10_100, restored.nextEventId());
    }

    @Test
    void everyCategoryRoundTripsThroughItsStableCode() throws Exception {
        Group group = new Group();
        Student student = new Student("Student", 1_000_000);
        EventCategories[] categories = EventCategories.values();
        for (int i = 0; i < categories.length; i++) {
            student.getEvents().add(new StudentEvent(
                    10_000 + i,
                    new EventDate(1, 1, 2026),
                    "Event " + i,
                    i + 1,
                    new EventDate(2, 1, 2026),
                    categories[i]));
        }
        group.getStudents().add(student);

        byte[] payload = BinaryPlainCmanUtility.buildPlainCman(
                new Group[] {group});
        Group[] restored = BinaryPlainCmanUtility.parsePlainCman(
                new ByteArrayInputStream(payload));

        for (int i = 0; i < categories.length; i++) {
            assertEquals(
                    categories[i],
                    restored[0].getStudents().get(0).getEvents().get(i).getCategory());
        }
    }

    @Test
    void rejectsCorruptedSectionOffsetsAndUnknownCategoryCodes() throws Exception {
        Group group = new Group();
        Student student = new Student("Student", 1_000_000);
        student.getEvents().add(new StudentEvent(
                10_000,
                new EventDate(1, 1, 2026),
                "Description",
                1,
                new EventDate(2, 1, 2026),
                EventCategories.MOD_1));
        group.getStudents().add(student);

        byte[] invalidOffset = BinaryPlainCmanUtility.buildPlainCman(
                new Group[] {group});
        ByteBuffer.wrap(invalidOffset)
                .order(ByteOrder.BIG_ENDIAN)
                .putInt(20, invalidOffset.length + 1);
        assertThrows(IOException.class,
                () -> BinaryPlainCmanUtility.parseCourseData(
                        new ByteArrayInputStream(invalidOffset)));

        byte[] invalidCategory = BinaryPlainCmanUtility.buildPlainCman(
                new Group[] {group});
        ByteBuffer header = ByteBuffer.wrap(invalidCategory).order(ByteOrder.BIG_ENDIAN);
        int eventsOffset = header.getInt(16);
        int categoryOffset = eventsOffset + 4 + 4 + 28;
        invalidCategory[categoryOffset] = 0;
        assertThrows(IOException.class,
                () -> BinaryPlainCmanUtility.parseCourseData(
                        new ByteArrayInputStream(invalidCategory)));
    }

    @Test
    void rejectsNextIdsThatCouldReuseExistingIds() {
        Group group = new Group();
        Student student = new Student("Student", 1_000_010);
        group.getStudents().add(student);

        assertThrows(IOException.class,
                () -> BinaryPlainCmanUtility.buildPlainCman(
                        new Group[] {group},
                        1_000_010,
                        BinaryPlainCmanUtility.FIRST_EVENT_ID));
    }
}
