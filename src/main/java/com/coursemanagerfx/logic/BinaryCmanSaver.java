package com.coursemanagerfx.logic;

/*import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.coursemanagerfx.CM_HELPER.COURSES_DIR;

public class CmanSaver {
    private static final String GROUPS_SPACER = "*****";
    public static String GET_GROUPS_SPACER() { return GROUPS_SPACER; }

    public static void save(String filePath, Group[] course) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Group group : course) {
            for (Student student : group.getStudents()) {
                sb.append(student.getID()).append("\n");
                sb.append(student.getName()).append("\n");
                for (StudentEvent event : student.getEvents()) {
                    sb.append("---")
                            .append(event.getCreationDate()).append("&")
                            .append(event.getDescription()).append("&")
                            .append(event.getMark()).append("&")
                            .append(event.getExpiredDate()).append("\n");
                }
            }
            // Разделитель групп
            sb.append(GROUPS_SPACER + "\n");
        }
        try {
            Files.write(Paths.get(filePath), sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IOException("Error save course", e);
        }
    }

    *//**
     * Создаёт пустой .cman‑файл с указанным количеством разделителей групп.
     *
     * @param courseName  имя курса (без расширения)
     * @param groupsCount сколько групп нужно зарезервировать
     * @return созданный файл
     * @throws IOException если не удалось создать каталог или файл
     *//*
    public static File createEmptyCourseFile(String courseName, int groupsCount) throws IOException {
        File dir = COURSES_DIR;          // путь вида .../Courses
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Cannot create directory " + dir.getAbsolutePath());
        }

        File file = new File(dir, courseName + ".cman");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < groupsCount; i++) {
            sb.append(GROUPS_SPACER).append('\n');
        }
        Files.writeString(file.toPath(), sb.toString());

        return file;
    }


    public static void main(String[] args) throws IOException {
        // Группа 1
        List<StudentEvent> events1 = new ArrayList<>();
        events1.add(new StudentEvent("01.01.2025", "Math exam", 5, "22.02.2025"));
        events1.add(new StudentEvent("02.01.2025", "History test", 4, "25.02.2025"));

        Student student1 = new Student("Артем");
        student1.setEvents(events1);

        Student student2 = new Student("Оля");
        student2.setEvents(List.of(
                new StudentEvent("03.01.2025", "Biology quiz", 3, "26.02.2025")
        ));

        Group group1 = new Group();
        group1.setStudents(List.of(student1, student2));

        // Группа 2
        Student student3 = new Student("Максим");
        student3.setEvents(List.of(
                new StudentEvent("05.01.2025", "Physics lab", 5, "28.02.2025"),
                new StudentEvent("06.01.2025", "Chemistry exam", 2, "01.03.2025")
        ));

        Group group2 = new Group();
        group2.setStudents(List.of(student3));

        // Группа 3
        Student student4 = new Student("Ірина");
        student4.setEvents(List.of(
                new StudentEvent("07.01.2025", "English speaking", 4, "05.03.2025")
        ));

        Student student5 = new Student("Богдан");
        student5.setEvents(List.of(
                new StudentEvent("08.01.2025", "Programming test", 5, "10.03.2025")
        ));

        Group group3 = new Group();
        group3.setStudents(List.of(student4, student5));

        // Собираем всё в массив
        Group[] course = new Group[3];
        course[0] = group1;
        course[1] = group2;
        course[2] = group3;

        // Сохраняем
        CmanSaver.save("C:\\Users\\Nazar\\AppData\\Local\\CourseManager\\Courses\\lol.cman", course);
    }
}*/

import java.io.*;
        import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class BinaryCmanSaver {
    public static void save(Group[] groups, File outFile) throws IOException {
        // 1) Собираем таблицу имён
        ByteArrayOutputStream namesBaos = new ByteArrayOutputStream();
        Map<String, Integer> nameOffset = new HashMap<>();
        for (Group g : groups) {
            for (Student s : g.getStudents()) {
                String name = s.getName();
                if (!nameOffset.containsKey(name)) {
                    int off = namesBaos.size();
                    nameOffset.put(name, off);
                    // запись UTF-8, пробел -> 0xFF
                    for (byte b : name.getBytes("UTF-8")) {
                        namesBaos.write(b == ' ' ? 0xFF : b);
                    }
                    namesBaos.write(0x00);
                }
            }
        }
        byte[] namesTable = prependMagic("TBLN", namesBaos.toByteArray());

        // 2) Собираем таблицу текстов ивентов
        ByteArrayOutputStream textsBaos = new ByteArrayOutputStream();
        Map<String, Integer> textOffset = new HashMap<>();
        List<Event> allEvents = new ArrayList<>();
        for (Group g : groups) {
            for (Student s : g.getStudents()) {
                for (StudentEvent e : s.getEvents()) {
                    allEvents.add(new Event(e.getID(), s.getID(),
                            e.getCreationDate(), e.getDescription(),
                            e.getMark(), e.getExpiredDate()));
                }
            }
        }
        for (Event e : allEvents) {
            for (String txt : Arrays.asList(e.creationDate, e.description, e.expiredDate)) {
                if (!textOffset.containsKey(e.id + "|" + txt)) {
                    int off = textsBaos.size();
                    textOffset.put(e.id + "|" + txt, off);
                    for (byte b : txt.getBytes("UTF-8")) {
                        textsBaos.write(b == ' ' ? 0xFF : b);
                    }
                    textsBaos.write(0x00);
                }
            }
        }
        byte[] textTable = prependMagic("TBLE", textsBaos.toByteArray());

        // 3) Собираем секцию STDS
        ByteArrayOutputStream stdsBaos = new ByteArrayOutputStream();
        stdsBaos.write(ascii("STDS"));
        DataOutputStream dosStds = new DataOutputStream(stdsBaos);
        for (Group g : groups) {
            dosStds.writeShort(g.getStudents().size());
            for (Student s : g.getStudents()) {
                dosStds.writeInt(s.getID());
                dosStds.writeInt(nameOffset.get(s.getName()));
            }
        }
        byte[] stdsSection = stdsBaos.toByteArray();

        // 4) Собираем секцию EVTS
        ByteArrayOutputStream evtsBaos = new ByteArrayOutputStream();
        evtsBaos.write(ascii("EVTS"));
        DataOutputStream dosEvts = new DataOutputStream(evtsBaos);
        dosEvts.writeInt(allEvents.size());
        for (Event e : allEvents) {
            dosEvts.writeInt(e.id);
            dosEvts.writeInt(e.studentId);
            dosEvts.writeInt(textOffset.get(e.id + "|" + e.creationDate));
            dosEvts.writeInt(e.mark);
            dosEvts.writeInt(textOffset.get(e.id + "|" + e.expiredDate));
        }
        byte[] evtsSection = evtsBaos.toByteArray();

        // 5) Подсчитываем оффсеты
        int headerSize = 32;
        int offSTDS = headerSize;
        int offTBLN = offSTDS + stdsSection.length;
        int offEVTS = offTBLN + namesTable.length;
        int offTBLE = offEVTS + evtsSection.length;

        // 6) Пишем всё в файл
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(outFile))) {
            // HEADER
            out.write(ascii("HEAD"));
            out.writeInt(groups.length);
            out.writeInt(offSTDS);
            out.writeInt(offTBLN);
            out.writeInt(offEVTS);
            out.writeInt(offTBLE);
            out.writeLong(0L); // 8 байт нулей

            // Секции
            out.write(stdsSection);
            out.write(namesTable);
            out.write(evtsSection);
            out.write(textTable);
        }
    }

    private static byte[] ascii(String s) { return s.getBytes(); }

    private static byte[] prependMagic(String magic, byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(magic.getBytes());
        baos.write(data);
        return baos.toByteArray();
    }

    // Вспомогательный внутренний класс для упрощения
    private static class Event {
        int id, studentId, mark;
        String creationDate, description, expiredDate;
        Event(int id, int sid, String cd, String desc, int m, String ed) {
            this.id = id; this.studentId = sid;
            this.creationDate = cd; this.description = desc;
            this.mark = m; this.expiredDate = ed;
        }
    }

    public static void main(String[] args) throws IOException {
        // Группа 1
        List<StudentEvent> events1 = new ArrayList<>();
        events1.add(new StudentEvent(82923, "01.01.2025", "Math exam", 5, "22.02.2025"));
        events1.add(new StudentEvent(29842, "02.01.2025", "History test", 4, "25.02.2025"));

        Student student1 = new Student("Артем");
        student1.setEvents(events1);

        Student student2 = new Student("Оля");
        student2.setEvents(List.of(
                new StudentEvent(28475, "03.01.2025", "Biology quiz", 3, "26.02.2025")
        ));

        Group group1 = new Group();
        group1.setStudents(List.of(student1, student2));

        // Группа 2
        Student student3 = new Student("Максим");
        student3.setEvents(List.of(
                new StudentEvent(12345, "05.01.2025", "Physics lab", 5, "28.02.2025"),
                new StudentEvent(94875, "06.01.2025", "Chemistry exam", 2, "01.03.2025")
        ));

        Group group2 = new Group();
        group2.setStudents(List.of(student3));

        // Группа 3
        Student student4 = new Student("Ірина");
        student4.setEvents(List.of(
                new StudentEvent(93747, "07.01.2025", "English speaking", 4, "05.03.2025")
        ));

        Student student5 = new Student("Богдан");
        student5.setEvents(List.of(
                new StudentEvent(94875, "08.01.2025", "Programming test", 5, "10.03.2025")
        ));

        Group group3 = new Group();
        group3.setStudents(List.of(student4, student5));

        // Собираем всё в массив
        Group[] course = new Group[3];
        course[0] = group1;
        course[1] = group2;
        course[2] = group3;

        File file = new File("C:\\Users\\Nazar\\.cmanfx\\Courses\\BinaryTest.cman");

        // Сохраняем
        BinaryCmanSaver.save(course, file);
    }
}
