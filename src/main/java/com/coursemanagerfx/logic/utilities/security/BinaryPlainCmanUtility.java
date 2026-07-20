/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic.utilities.security;

import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.basic.Student;
import com.coursemanagerfx.logic.basic.event.StudentEvent;
import com.coursemanagerfx.logic.basic.event.category.EventCategories;
import com.coursemanagerfx.logic.basic.event.date.EventDate;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Builds and parses the authenticated plaintext payload stored inside CMAN v1.
 *
 * <p>The 32-byte header stores section offsets and the next student/event IDs.
 * TBLN and TBLE use length-prefixed UTF-8 strings. Every EVTS record contains
 * explicit offsets for creation date, description and expiration date, plus a
 * stable category code that is independent of enum declaration order.</p>
 */
public final class BinaryPlainCmanUtility {

    public static final int HEADER_SIZE = 32;
    public static final int FIRST_STUDENT_ID = 1_000_000;
    public static final int FIRST_EVENT_ID = 10_000;

    private static final int EVENT_RECORD_SIZE = 29;
    private static final int MAX_GROUPS = 10_000;
    private static final int MAX_EVENTS = 2_000_000;
    private static final int MAX_STRING_BYTES = 16 * 1024 * 1024;

    private BinaryPlainCmanUtility() {
    }

    public record CourseData(
            Group[] groups,
            int nextStudentId,
            int nextEventId) {

        public CourseData {
            Objects.requireNonNull(groups, "groups");
        }
    }

    public static byte[] buildPlainCman(Group[] groups) throws IOException {
        return buildPlainCman(
                groups,
                nextStudentIdFor(groups),
                nextEventIdFor(groups));
    }

    public static byte[] buildPlainCman(
            Group[] groups,
            int nextStudentId,
            int nextEventId) throws IOException {

        validateCourse(groups, nextStudentId, nextEventId);

        StringTable names = new StringTable();
        StringTable texts = new StringTable();
        List<SaverEvent> allEvents = new ArrayList<>();

        for (Group group : groups) {
            for (Student student : group.getStudents()) {
                names.offsetOf(student.getName());
                for (StudentEvent event : student.getEvents()) {
                    SaverEvent saved = new SaverEvent(event, student.getStudentID());
                    saved.creationOffset = texts.offsetOf(saved.creationDate);
                    saved.descriptionOffset = texts.offsetOf(saved.description);
                    saved.expirationOffset = texts.offsetOf(saved.expirationDate);
                    allEvents.add(saved);
                }
            }
        }

        byte[] stdsSection = buildStudentsSection(groups, names);
        byte[] namesSection = prependMagic("TBLN", names.toByteArray());
        byte[] eventsSection = buildEventsSection(allEvents);
        byte[] textsSection = prependMagic("TBLE", texts.toByteArray());

        int offsetSTDS = HEADER_SIZE;
        int offsetTBLN = checkedAdd(offsetSTDS, stdsSection.length);
        int offsetEVTS = checkedAdd(offsetTBLN, namesSection.length);
        int offsetTBLE = checkedAdd(offsetEVTS, eventsSection.length);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.write(ascii("HEAD"));
            out.writeInt(groups.length);
            out.writeInt(offsetSTDS);
            out.writeInt(offsetTBLN);
            out.writeInt(offsetEVTS);
            out.writeInt(offsetTBLE);
            out.writeInt(nextStudentId);
            out.writeInt(nextEventId);
            out.write(stdsSection);
            out.write(namesSection);
            out.write(eventsSection);
            out.write(textsSection);
        }
        return bytes.toByteArray();
    }

    public static Group[] parsePlainCman(InputStream input) throws IOException {
        return parseCourseData(input).groups();
    }

    public static CourseData parseCourseData(InputStream input) throws IOException {
        Objects.requireNonNull(input, "input");
        byte[] data = input.readAllBytes();
        if (data.length < HEADER_SIZE + 16) {
            throw new IOException("CMAN payload is too short");
        }

        ByteBuffer header = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
        requireMagic(header, "HEAD");
        int groupsCount = header.getInt();
        int offsetSTDS = header.getInt();
        int offsetTBLN = header.getInt();
        int offsetEVTS = header.getInt();
        int offsetTBLE = header.getInt();
        int nextStudentId = header.getInt();
        int nextEventId = header.getInt();

        validateSectionOffsets(
                data,
                groupsCount,
                offsetSTDS,
                offsetTBLN,
                offsetEVTS,
                offsetTBLE);

        ByteBuffer studentsBuffer = sectionPayload(
                data, offsetSTDS, offsetTBLN, "STDS");
        byte[] namesBuffer = sectionBytes(
                data, offsetTBLN, offsetEVTS, "TBLN");
        ByteBuffer eventsBuffer = sectionPayload(
                data, offsetEVTS, offsetTBLE, "EVTS");
        byte[] textsBuffer = sectionBytes(
                data, offsetTBLE, data.length, "TBLE");

        List<List<Student>> parsedGroups = new ArrayList<>(groupsCount);
        Map<Integer, Student> studentsById = new HashMap<>();
        int maximumStudentId = FIRST_STUDENT_ID - 1;

        for (int groupIndex = 0; groupIndex < groupsCount; groupIndex++) {
            requireRemaining(studentsBuffer, Short.BYTES, "student count");
            int studentCount = Short.toUnsignedInt(studentsBuffer.getShort());
            requireRemaining(
                    studentsBuffer,
                    Math.multiplyExact(studentCount, Integer.BYTES * 2),
                    "student records");

            List<Student> students = new ArrayList<>(studentCount);
            for (int i = 0; i < studentCount; i++) {
                int studentId = studentsBuffer.getInt();
                int nameOffset = studentsBuffer.getInt();
                if (studentId < FIRST_STUDENT_ID) {
                    throw new IOException("Invalid student ID: " + studentId);
                }
                Student student = new Student(
                        readString(namesBuffer, nameOffset),
                        studentId);
                student.setEvents(new ArrayList<>());
                if (studentsById.putIfAbsent(studentId, student) != null) {
                    throw new IOException("Duplicate student ID: " + studentId);
                }
                maximumStudentId = Math.max(maximumStudentId, studentId);
                students.add(student);
            }
            parsedGroups.add(students);
        }
        if (studentsBuffer.hasRemaining()) {
            throw new IOException("Unexpected data at the end of STDS");
        }

        requireRemaining(eventsBuffer, Integer.BYTES, "event count");
        int totalEvents = eventsBuffer.getInt();
        if (totalEvents < 0 || totalEvents > MAX_EVENTS) {
            throw new IOException("Invalid event count: " + totalEvents);
        }
        int expectedEventBytes;
        try {
            expectedEventBytes = Math.multiplyExact(totalEvents, EVENT_RECORD_SIZE);
        } catch (ArithmeticException exception) {
            throw new IOException("Event section is too large", exception);
        }
        if (eventsBuffer.remaining() != expectedEventBytes) {
            throw new IOException("Invalid EVTS section length");
        }

        Set<Integer> eventIds = new HashSet<>();
        int maximumEventId = FIRST_EVENT_ID - 1;
        for (int i = 0; i < totalEvents; i++) {
            int eventId = eventsBuffer.getInt();
            int studentId = eventsBuffer.getInt();
            int creationOffset = eventsBuffer.getInt();
            int descriptionOffset = eventsBuffer.getInt();
            double mark = eventsBuffer.getDouble();
            int expirationOffset = eventsBuffer.getInt();
            int categoryCode = Byte.toUnsignedInt(eventsBuffer.get());

            if (eventId < FIRST_EVENT_ID || !eventIds.add(eventId)) {
                throw new IOException("Invalid or duplicate event ID: " + eventId);
            }
            Student student = studentsById.get(studentId);
            if (student == null) {
                throw new IOException(
                        "Event " + eventId + " references unknown student " + studentId);
            }

            EventCategories category;
            try {
                category = EventCategories.fromCode(categoryCode);
            } catch (IllegalArgumentException exception) {
                throw new IOException(exception.getMessage(), exception);
            }

            StudentEvent event = new StudentEvent(
                    eventId,
                    parseEventDate(readString(textsBuffer, creationOffset)),
                    readString(textsBuffer, descriptionOffset),
                    mark,
                    parseEventDate(readString(textsBuffer, expirationOffset)),
                    category);
            student.getEvents().add(event);
            maximumEventId = Math.max(maximumEventId, eventId);
        }

        validateNextId(nextStudentId, maximumStudentId, FIRST_STUDENT_ID, "student");
        validateNextId(nextEventId, maximumEventId, FIRST_EVENT_ID, "event");

        Group[] groups = new Group[groupsCount];
        for (int i = 0; i < groupsCount; i++) {
            Group group = new Group();
            group.setStudents(parsedGroups.get(i));
            groups[i] = group;
        }
        return new CourseData(groups, nextStudentId, nextEventId);
    }

    public static int nextStudentIdFor(Group[] groups) throws IOException {
        return nextIdAfterMaximum(groups, true);
    }

    public static int nextEventIdFor(Group[] groups) throws IOException {
        return nextIdAfterMaximum(groups, false);
    }

    private static int nextIdAfterMaximum(Group[] groups, boolean students)
            throws IOException {

        Objects.requireNonNull(groups, "groups");
        int maximum = students ? FIRST_STUDENT_ID - 1 : FIRST_EVENT_ID - 1;
        for (Group group : groups) {
            if (group == null || group.getStudents() == null) continue;
            for (Student student : group.getStudents()) {
                if (student == null) continue;
                if (students) {
                    maximum = Math.max(maximum, student.getStudentID());
                } else if (student.getEvents() != null) {
                    for (StudentEvent event : student.getEvents()) {
                        if (event != null) maximum = Math.max(maximum, event.getID());
                    }
                }
            }
        }
        if (maximum == Integer.MAX_VALUE) {
            throw new IOException("No more " + (students ? "student" : "event") + " IDs available");
        }
        return maximum + 1;
    }

    private static byte[] buildStudentsSection(Group[] groups, StringTable names)
            throws IOException {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.write(ascii("STDS"));
            for (Group group : groups) {
                List<Student> students = group.getStudents();
                if (students.size() > 0xFFFF) {
                    throw new IOException("A group contains too many students");
                }
                out.writeShort(students.size());
                for (Student student : students) {
                    out.writeInt(student.getStudentID());
                    out.writeInt(names.offsetOf(student.getName()));
                }
            }
        }
        return bytes.toByteArray();
    }

    private static byte[] buildEventsSection(List<SaverEvent> events)
            throws IOException {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.write(ascii("EVTS"));
            out.writeInt(events.size());
            for (SaverEvent event : events) {
                out.writeInt(event.id);
                out.writeInt(event.studentId);
                out.writeInt(event.creationOffset);
                out.writeInt(event.descriptionOffset);
                out.writeDouble(event.mark);
                out.writeInt(event.expirationOffset);
                out.writeByte(event.category.getCode());
            }
        }
        return bytes.toByteArray();
    }

    private static void validateCourse(
            Group[] groups,
            int nextStudentId,
            int nextEventId) throws IOException {

        Objects.requireNonNull(groups, "groups");
        if (groups.length > MAX_GROUPS) {
            throw new IOException("Too many groups: " + groups.length);
        }

        Set<Integer> studentIds = new HashSet<>();
        Set<Integer> eventIds = new HashSet<>();
        int maximumStudentId = FIRST_STUDENT_ID - 1;
        int maximumEventId = FIRST_EVENT_ID - 1;
        int eventCount = 0;

        for (Group group : groups) {
            if (group == null || group.getStudents() == null) {
                throw new IOException("Course contains a null group or student list");
            }
            if (group.getStudents().size() > 0xFFFF) {
                throw new IOException("A group contains too many students");
            }
            for (Student student : group.getStudents()) {
                if (student == null || student.getName() == null || student.getEvents() == null) {
                    throw new IOException("Course contains incomplete student data");
                }
                int studentId = student.getStudentID();
                if (studentId < FIRST_STUDENT_ID || !studentIds.add(studentId)) {
                    throw new IOException("Invalid or duplicate student ID: " + studentId);
                }
                maximumStudentId = Math.max(maximumStudentId, studentId);

                for (StudentEvent event : student.getEvents()) {
                    if (event == null
                            || event.getCrtDate() == null
                            || event.getDescription() == null
                            || event.getExpDate() == null
                            || event.getCategory() == null) {
                        throw new IOException("Course contains incomplete event data");
                    }
                    int eventId = event.getID();
                    if (eventId < FIRST_EVENT_ID || !eventIds.add(eventId)) {
                        throw new IOException("Invalid or duplicate event ID: " + eventId);
                    }
                    maximumEventId = Math.max(maximumEventId, eventId);
                    if (++eventCount > MAX_EVENTS) {
                        throw new IOException("Too many events");
                    }
                }
            }
        }

        validateNextId(nextStudentId, maximumStudentId, FIRST_STUDENT_ID, "student");
        validateNextId(nextEventId, maximumEventId, FIRST_EVENT_ID, "event");
    }

    private static void validateNextId(
            int nextId,
            int maximumId,
            int firstId,
            String type) throws IOException {

        if (nextId < firstId || nextId <= maximumId) {
            throw new IOException("Invalid next " + type + " ID: " + nextId);
        }
    }

    private static void validateSectionOffsets(
            byte[] data,
            int groupsCount,
            int offsetSTDS,
            int offsetTBLN,
            int offsetEVTS,
            int offsetTBLE) throws IOException {

        if (groupsCount < 0 || groupsCount > MAX_GROUPS) {
            throw new IOException("Invalid group count: " + groupsCount);
        }
        if (offsetSTDS != HEADER_SIZE
                || offsetTBLN < offsetSTDS + 4
                || offsetEVTS < offsetTBLN + 4
                || offsetTBLE < offsetEVTS + 8
                || offsetTBLE + 4 > data.length) {
            throw new IOException("Invalid CMAN section offsets");
        }
    }

    private static ByteBuffer sectionPayload(
            byte[] data,
            int start,
            int end,
            String magic) throws IOException {

        requireSection(data, start, end, magic);
        return ByteBuffer.wrap(data, start + 4, end - start - 4)
                .slice()
                .order(ByteOrder.BIG_ENDIAN);
    }

    private static byte[] sectionBytes(
            byte[] data,
            int start,
            int end,
            String magic) throws IOException {

        requireSection(data, start, end, magic);
        byte[] bytes = new byte[end - start - 4];
        System.arraycopy(data, start + 4, bytes, 0, bytes.length);
        return bytes;
    }

    private static void requireSection(
            byte[] data,
            int start,
            int end,
            String expectedMagic) throws IOException {

        if (start < HEADER_SIZE || end < start + 4 || end > data.length) {
            throw new IOException("Invalid " + expectedMagic + " section bounds");
        }
        for (int i = 0; i < 4; i++) {
            if (data[start + i] != expectedMagic.charAt(i)) {
                throw new IOException("Expected " + expectedMagic + " section");
            }
        }
    }

    private static void requireMagic(ByteBuffer buffer, String expected)
            throws IOException {

        requireRemaining(buffer, expected.length(), expected + " magic");
        byte[] actual = new byte[expected.length()];
        buffer.get(actual);
        if (!expected.equals(new String(actual, StandardCharsets.US_ASCII))) {
            throw new IOException("Expected " + expected + " magic");
        }
    }

    private static void requireRemaining(
            ByteBuffer buffer,
            int required,
            String field) throws IOException {

        if (required < 0 || buffer.remaining() < required) {
            throw new IOException("Truncated CMAN " + field);
        }
    }

    private static int checkedAdd(int left, int right) throws IOException {
        try {
            return Math.addExact(left, right);
        } catch (ArithmeticException exception) {
            throw new IOException("CMAN payload is too large", exception);
        }
    }

    private static EventDate parseEventDate(String value) throws IOException {
        try {
            String[] parts = value.split("\\.", -1);
            if (parts.length != 3) throw new NumberFormatException();
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);
            LocalDate.of(year, month, day);
            return new EventDate(day, month, year);
        } catch (RuntimeException exception) {
            throw new IOException("Invalid event date: " + value, exception);
        }
    }

    private static String readString(byte[] bytes, int offset) throws IOException {
        if (offset < 0 || offset > bytes.length - Integer.BYTES) {
            throw new IOException("Invalid string offset: " + offset);
        }
        int length = ByteBuffer.wrap(bytes, offset, Integer.BYTES)
                .order(ByteOrder.BIG_ENDIAN)
                .getInt();
        if (length < 0
                || length > MAX_STRING_BYTES
                || offset + Integer.BYTES + length > bytes.length) {
            throw new IOException("Invalid UTF-8 string length: " + length);
        }
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes, offset + Integer.BYTES, length))
                    .toString();
        } catch (CharacterCodingException exception) {
            throw new IOException("Invalid UTF-8 string", exception);
        }
    }

    public static byte[] ascii(String value) {
        return value.getBytes(StandardCharsets.US_ASCII);
    }

    public static byte[] prependMagic(String magic, byte[] data) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.write(ascii(magic));
        bytes.write(data);
        return bytes.toByteArray();
    }

    private static final class StringTable {
        private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        private final Map<String, Integer> offsets = new HashMap<>();

        int offsetOf(String value) throws IOException {
            Integer existing = offsets.get(value);
            if (existing != null) return existing;

            byte[] encoded = value.getBytes(StandardCharsets.UTF_8);
            if (encoded.length > MAX_STRING_BYTES) {
                throw new IOException("CMAN text value is too long");
            }
            int offset = bytes.size();
            try (DataOutputStream out = new DataOutputStream(bytes)) {
                out.writeInt(encoded.length);
                out.write(encoded);
                out.flush();
            }
            offsets.put(value, offset);
            return offset;
        }

        byte[] toByteArray() {
            return bytes.toByteArray();
        }
    }

    private static final class SaverEvent {
        final int id;
        final int studentId;
        final double mark;
        final String creationDate;
        final String description;
        final String expirationDate;
        final EventCategories category;
        int creationOffset;
        int descriptionOffset;
        int expirationOffset;

        SaverEvent(StudentEvent event, int studentId) {
            this.id = event.getID();
            this.studentId = studentId;
            this.creationDate = event.getCrtDate().toString();
            this.description = event.getDescription();
            this.mark = event.getMark();
            this.expirationDate = event.getExpDate().toString();
            this.category = event.getCategory();
        }
    }
}
