package com.coursemanagerfx.logic.notusages;

import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.basic.Student;
import com.coursemanagerfx.logic.basic.event.EventMods;
import com.coursemanagerfx.logic.basic.event.StudentEvent;
import com.coursemanagerfx.logic.basic.event.date.EventDate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/*
 * Формат бинарного .cman-файла:
 *
 * ┌──────────────┬─────────────────────────────────────────────────────────────────────┐
 * │ Секция       │ Описание                                                            │
 * ├──────────────┼─────────────────────────────────────────────────────────────────────┤
 * │ HEADER       │ • "HEAD" (4 байта, ASCII)                                           │
 * │ (32 байта)   │ • groupsCount (4 байта, int)                                        │
 * │              │ • offsetSTDS (4 байта, uint32)                                      │
 * │              │ • offsetTBLN (4 байта, uint32)                                      │
 * │              │ • offsetEVTS (4 байта, uint32)                                      │
 * │              │ • offsetTBLE (4 байта, uint32)                                      │
 * │              │ • резерв (8 байт нулей)                                             │
 * ├──────────────┼─────────────────────────────────────────────────────────────────────┤
 * │ STDS         │ • "STDS" (4 байта, ASCII)                                           │
 * │              │ Для каждой из groupsCount групп:                                    │
 * │              │   • studentCount (2 байта, short)                                   │
 * │              │   Для каждого студента:                                             │
 * │              │     – studentID (4 байта, int)                                      │
 * │              │     – nameOffset (4 байта, uint32, смещение в TBLN)                 │
 * ├──────────────┼─────────────────────────────────────────────────────────────────────┤
 * │ TBLN         │ • "TBLN" (4 байта, ASCII)                                           │
 * │ (таблица имён)│ • Нуль-терминированные строки:                                     │
 * │              │   – UTF-8 байты имени                                               │
 * │              │   – пробел заменяется байтом 0xFF                                   │
 * │              │   – завершение строки: 0x00                                         │
 * ├──────────────┼─────────────────────────────────────────────────────────────────────┤
 * │ EVTS         │ • "EVTS" (4 байта, ASCII)                                           │
 * │              │ • totalEvents (4 байта, int)                                        │
 * │              │ Для каждого события:                                                │
 * │              │   – eventID (4 байта, int)                                          │
 * │              │   – studentID (4 байта, int)                                        │
 * │              │   – creationOffset (4 байта, uint32, смещение в TBLE)               │
 * │              │   – mark (4 байта, int)                                             │
 * │              │   – expiredOffset (4 байта, uint32, смещение в TBLE)                │
 * ├──────────────┼─────────────────────────────────────────────────────────────────────┤
 * │ TBLE         │ • "TBLE" (4 байта, ASCII)                                           │
 * │ (тексты)     │ • Нуль-терминированные строки:                                      │
 * │              │   – UTF-8 байты (дата создания, описание, дата истечения)           │
 * │              │   – пробел заменяется байтом 0xFF                                   │
 * │              │   – завершение каждой строки: 0x00                                  │
 * └──────────────┴─────────────────────────────────────────────────────────────────────┘
 */

public class BinaryCmanSaver {
    private static class SaverEvent {
        int id, studentId, mark;
        String creationDate, description, expiredDate;

        SaverEvent(StudentEvent e, int studentId) {
            this.id = e.getID();
            this.studentId = studentId;
            this.creationDate = e.getCrtDate().toString();      // EventDate → String
            this.description = e.getDescription();
            this.mark = e.getMark();
            this.expiredDate = e.getExpDate().toString();        // EventDate → String
        }
    }

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
                    for (byte b : name.getBytes(StandardCharsets.UTF_8)) {
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
        List<SaverEvent> allEvents = new ArrayList<>();
        for (Group g : groups) {
            for (Student s : g.getStudents()) {
                for (StudentEvent e : s.getEvents()) {
                    allEvents.add(new SaverEvent(e, s.getID()));
                }
            }
        }
        for (SaverEvent e : allEvents) {
            for (String txt : Arrays.asList(e.creationDate, e.description, e.expiredDate)) {
                if (!textOffset.containsKey(e.id + "|" + txt)) {
                    int off = textsBaos.size();
                    textOffset.put(e.id + "|" + txt, off);
                    for (byte b : txt.getBytes(StandardCharsets.UTF_8)) {
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
        for (SaverEvent e : allEvents) {
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

    public static void main(String[] args) throws IOException {
        // Группа 1
        List<StudentEvent> events1 = new ArrayList<>();
        events1.add(new StudentEvent(10001, new EventDate(1, 1, 2025), "Math exam", EventMods.FIRST));
        events1.add(new StudentEvent(10002, new EventDate(3, 1, 2025), "Biology lab", 4, new EventDate(8, 1, 2025)));
        events1.add(new StudentEvent(10003, new EventDate(5, 1, 2025), "Literature essay: Lesya Ukrainka", EventMods.SECOND));
        events1.add(new StudentEvent(10004, new EventDate(7, 1, 2025), "Physics forces quiz", 5, new EventDate(14, 1, 2025)));
        events1.add(new StudentEvent(10005, new EventDate(10, 1, 2025), "World History group project", EventMods.THIRD));

        List<StudentEvent> events2 = new ArrayList<>();
        events2.add(new StudentEvent(20001, new EventDate(2, 1, 2025), "Computer graphics assignment", EventMods.FIRST));
        events2.add(new StudentEvent(20002, new EventDate(4, 1, 2025), "Music theory test", 3, new EventDate(9, 1, 2025)));
        events2.add(new StudentEvent(20003, new EventDate(6, 1, 2025), "Technology & Robotics demo", EventMods.SECOND));
        events2.add(new StudentEvent(20004, new EventDate(9, 1, 2025), "Art portfolio presentation", 5, new EventDate(16, 1, 2025)));
        events2.add(new StudentEvent(20005, new EventDate(11, 1, 2025), "Photography contest submission", EventMods.THIRD));

        List<StudentEvent> events3 = new ArrayList<>();
        events3.add(new StudentEvent(30001, new EventDate(1, 1, 2025), "English debate", EventMods.SECOND));
        events3.add(new StudentEvent(30002, new EventDate(3, 1, 2025), "Ukrainian dictation", 4, new EventDate(6, 1, 2025)));
        events3.add(new StudentEvent(30003, new EventDate(5, 1, 2025), "Ethics case discussion", EventMods.FIRST));
        events3.add(new StudentEvent(30004, new EventDate(8, 1, 2025), "Psychology quiz", 5, new EventDate(12, 1, 2025)));
        events3.add(new StudentEvent(30005, new EventDate(10, 1, 2025), "Public speaking workshop", EventMods.THIRD));


        Student student1 = new Student("Артем");
        student1.setEvents(events1);

        Student student2 = new Student("Оля");
        student2.setEvents(events2);

        Group group1 = new Group();
        group1.setStudents(List.of(student1, student2));

        // Группа 2
        Student student3 = new Student("Максим");
        student3.setEvents(events2);

        Group group2 = new Group();
        group2.setStudents(List.of(student3));

        // Группа 3
        Student student4 = new Student("Ірина");
        student4.setEvents(events1);

        Student student5 = new Student("Богдан");
        student5.setEvents(events3);

        Group group3 = new Group();
        group3.setStudents(List.of(student4, student5));

        // Собираем всё в массив
        Group[] course = new Group[3];
        course[0] = group1;
        course[1] = group2;
        course[2] = group3;

        File file = new File("C:\\Users\\Nazar\\.cmanfx\\Courses\\Test.cman");

        // Сохраняем
        BinaryCmanSaver.save(course, file);
    }
}
