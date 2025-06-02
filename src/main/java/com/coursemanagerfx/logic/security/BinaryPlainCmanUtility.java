package com.coursemanagerfx.logic.security;

import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.basic.Student;
import com.coursemanagerfx.logic.basic.event.EventCategories;
import com.coursemanagerfx.logic.basic.event.StudentEvent;
import com.coursemanagerfx.logic.basic.event.date.EventDate;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/*
 * Формат бинарного .cman‑файла:
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
 * │(таблица имён)│ • Нуль‑терминированные строки:                                      │
 * │              │   – UTF‑8 байты имени                                               │
 * │              │   – пробел заменяется байтом 0xFF                                   │
 * │              │   – завершение строки: 0x00                                         │
 * ├──────────────┼─────────────────────────────────────────────────────────────────────┤
 * │ EVTS         │ • "EVTS" (4 байта, ASCII)                                           │
 * │              │ • totalEvents (4 байта, int)                                        │
 * │              │ Для каждого события:                                                │
 * │              │   – eventID (4 байта, int)                                          │
 * │              │   – studentID (4 байта, int)                                        │
 * │              │   – creationOffset (4 байта, uint32, смещение в TBLE)               │
 * │              │   – mark (8 байт, double)                                           │
 * │              │   – expiredOffset (4 байта, uint32, смещение в TBLE)                │
 * │              │   – type (1 байт, порядковый номер типа из enum EventTypes)         │
 * ├──────────────┼─────────────────────────────────────────────────────────────────────┤
 * │ TBLE         │ • "TBLE" (4 байта, ASCII)                                           │
 * │ (тексты)     │ • Нуль‑терминированные строки:                                      │
 * │              │   – UTF‑8 байты (дата создания, описание, дата истечения)           │
 * │              │   – пробел заменяется байтом 0xFF                                   │
 * │              │   – завершение каждой строки: 0x00                                  │
 * └──────────────┴─────────────────────────────────────────────────────────────────────┘
 */

public class BinaryPlainCmanUtility {
    /**
     * Сохраняет массив групп в бинарный .cman‑файл.
     */
    public static byte[] buildPlainCman(Group[] groups) throws IOException {
        /* ────────────────────────── 1) TBLN — таблица имён ────────────────────────── */
        ByteArrayOutputStream namesBaos = new ByteArrayOutputStream();
        Map<String, Integer> nameOffset = new HashMap<>();
        for (Group g : groups) {
            for (Student s : g.getStudents()) {
                String name = s.getName();
                if (!nameOffset.containsKey(name)) {
                    int off = namesBaos.size();
                    nameOffset.put(name, off);
                    // UTF‑8 + пробелы → 0xFF
                    for (byte b : name.getBytes(StandardCharsets.UTF_8)) {
                        namesBaos.write(b == ' ' ? 0xFF : b);
                    }
                    namesBaos.write(0x00); // terminator
                }
            }
        }
        byte[] namesTable = prependMagic("TBLN", namesBaos.toByteArray());

        /* ────────────────────────── 2) TBLE — тексты событий ─────────────────────── */
        ByteArrayOutputStream textsBaos = new ByteArrayOutputStream();
        Map<String, Integer> textOffset = new HashMap<>();
        List<SaverEvent> allEvents = new ArrayList<>();
        for (Group g : groups) {
            for (Student s : g.getStudents()) {
                for (StudentEvent e : s.getEvents()) {
                    allEvents.add(new SaverEvent(e, s.getStudentID()));
                }
            }
        }
        for (SaverEvent e : allEvents) {
            for (String txt : Arrays.asList(e.creationDate, e.description, e.expiredDate)) {
                String key = e.id + "|" + txt;
                if (!textOffset.containsKey(key)) {
                    int off = textsBaos.size();
                    textOffset.put(key, off);
                    for (byte b : txt.getBytes(StandardCharsets.UTF_8)) {
                        textsBaos.write(b == ' ' ? 0xFF : b);
                    }
                    textsBaos.write(0x00);
                }
            }
        }
        byte[] textTable = prependMagic("TBLE", textsBaos.toByteArray());

        /* ────────────────────────── 3) STDS — информация о студентах ─────────────── */
        ByteArrayOutputStream stdsBaos = new ByteArrayOutputStream();
        stdsBaos.write(ascii("STDS"));
        DataOutputStream dosStds = new DataOutputStream(stdsBaos);
        for (Group g : groups) {
            dosStds.writeShort(g.getStudents().size());
            for (Student s : g.getStudents()) {
                dosStds.writeInt(s.getStudentID());
                dosStds.writeInt(nameOffset.get(s.getName()));
            }
        }
        byte[] stdsSection = stdsBaos.toByteArray();

        /* ────────────────────────── 4) EVTS — события ────────────────────────────── */
        ByteArrayOutputStream evtsBaos = new ByteArrayOutputStream();
        evtsBaos.write(ascii("EVTS"));
        DataOutputStream dosEvts = new DataOutputStream(evtsBaos);
        dosEvts.writeInt(allEvents.size());
        for (SaverEvent e : allEvents) {
            dosEvts.writeInt(e.id);
            dosEvts.writeInt(e.studentId);
            dosEvts.writeInt(textOffset.get(e.id + "|" + e.creationDate));
            dosEvts.writeDouble(e.mark);
            dosEvts.writeInt(textOffset.get(e.id + "|" + e.expiredDate));
            dosEvts.writeByte(e.type.ordinal()); // ← записываем тип события (1 байт)
        }
        byte[] evtsSection = evtsBaos.toByteArray();

        /* ────────────────────────── 5) вычисляем оффсеты ─────────────────────────── */
        int offSTDS = 32;
        int offTBLN = offSTDS + stdsSection.length;
        int offEVTS = offTBLN + namesTable.length;
        int offTBLE = offEVTS + evtsSection.length;
/*
        *//* ────────────────────────── 6) запись в файл ─────────────────────────────── *//*
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(outFile))) {
            // HEADER
            out.write(ascii("HEAD"));
            out.writeInt(groups.length);
            out.writeInt(offSTDS);
            out.writeInt(offTBLN);
            out.writeInt(offEVTS);
            out.writeInt(offTBLE);
            out.writeLong(0L); // резерв (8 байт нулей)

            // Секции
            out.write(stdsSection);
            out.write(namesTable);
            out.write(evtsSection);
            out.write(textTable);
        }*/

        ByteArrayOutputStream resultBaos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(resultBaos);

        // HEADER
        out.write(ascii("HEAD"));
        out.writeInt(groups.length);
        out.writeInt(offSTDS);
        out.writeInt(offTBLN);
        out.writeInt(offEVTS);
        out.writeInt(offTBLE);
        out.writeLong(0L); // резерв (8 байт нулей)

        // Секции
        out.write(stdsSection);
        out.write(namesTable);
        out.write(evtsSection);
        out.write(textTable);

        return resultBaos.toByteArray();
    }

    public static Group[] parsePlainCman(InputStream in) throws IOException {
        byte[] data = in.readAllBytes();
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.BIG_ENDIAN);

        // 1) HEADER
        byte[] magic = new byte[4];
        buffer.get(magic);
        if (!"HEAD".equals(new String(magic, StandardCharsets.US_ASCII))) {
            throw new IOException("Некорректный файл: not HEAD magic");
        }
        int groupsCount = buffer.getInt();
        int offSTDS = buffer.getInt();
        int offTBLN = buffer.getInt();
        int offEVTS = buffer.getInt();
        int offTBLE = buffer.getInt();
        buffer.position(buffer.position() + 8); // skip reserved

        // 2) STDS
        buffer.position(offSTDS);
        buffer.get(magic);
        if (!"STDS".equals(new String(magic, StandardCharsets.US_ASCII))) {
            throw new IOException("Ожидалась секция STDS");
        }
        List<List<HolderStudent>> rawGroups = new ArrayList<>();
        for (int gi = 0; gi < groupsCount; gi++) {
            int cnt = Short.toUnsignedInt(buffer.getShort());
            List<HolderStudent> lst = new ArrayList<>();
            for (int i = 0; i < cnt; i++) {
                int sid = buffer.getInt();
                int nameOff = buffer.getInt();
                lst.add(new HolderStudent(sid, nameOff));
            }
            rawGroups.add(lst);
        }

        // 3) TBLN
        byte[] namesBuf = Arrays.copyOfRange(data, offTBLN + 4, offEVTS);

        // Построение объектов Student
        List<List<Student>> groups = new ArrayList<>();
        for (var rawG : rawGroups) {
            List<Student> students = new ArrayList<>();
            for (var h : rawG) {
                String name = readString(namesBuf, h.nameOffset);
                Student s = new Student(name, h.id);
                // инициализируем пустой список событий
                s.setEvents(new ArrayList<>());
                students.add(s);
            }
            groups.add(students);
        }

        // 4) EVTS
        buffer.position(offEVTS);
        buffer.get(magic);
        if (!"EVTS".equals(new String(magic, StandardCharsets.US_ASCII))) {
            throw new IOException("Ожидалась секция EVTS");
        }
        int totalEvents = buffer.getInt();
        List<HolderEvent> rawEvents = new ArrayList<>();
        for (int i = 0; i < totalEvents; i++) {
            int eid = buffer.getInt();
            int sid = buffer.getInt();
            int creationOff = buffer.getInt();
            double mark = buffer.getDouble();
            int expiredOff = buffer.getInt();
            int typeOrd = Byte.toUnsignedInt(buffer.get());
            rawEvents.add(new HolderEvent(eid, sid, creationOff, mark, expiredOff, typeOrd));
        }

        // 5) TBLE
        byte[] textBuf = Arrays.copyOfRange(data, offTBLE + 4, data.length);

        // 6) Распределяем события
        for (var ev : rawEvents) {
            String creationDate = readString(textBuf, ev.creationOffset);

            String description = "";
            int idx = ev.creationOffset;
            while (idx < textBuf.length && textBuf[idx] != 0) idx++;
            idx++; // перейти через 0x00
            if (idx < textBuf.length) {
                description = readString(textBuf, idx);
            }

            String expiredDate = readString(textBuf, ev.expiredOffset);

            // Создаем StudentEvent с учётом типа
            StudentEvent se = new StudentEvent(
                    ev.eventID,
                    parseEventDate(creationDate),
                    description,
                    ev.mark,
                    parseEventDate(expiredDate),
                    EventCategories.values()[ev.typeOrdinal]
            );

            // Добавляем событие нужному студенту
            outer:
            for (List<Student> grp : groups) {
                for (Student s : grp) {
                    if (s.getStudentID() == ev.studentId) {
                        s.getEvents().add(se);
                        break outer;
                    }
                }
            }
        }

        // 7) Итоговый массив
        Group[] result = new Group[groupsCount];
        for (int i = 0; i < groupsCount; i++) {
            Group g = new Group();
            g.setStudents(groups.get(i));
            result[i] = g;
        }
        return result;
    }

    /* ────────────────────────── вспомогательные методы ──────────────────────────── */
    public static byte[] ascii(String s) {
        return s.getBytes();
    }

    public static byte[] prependMagic(String magic, byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(magic.getBytes());
        baos.write(data);
        return baos.toByteArray();
    }

    private static EventDate parseEventDate(String s) {
        String[] parts = s.split("\\.");
        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int year = Integer.parseInt(parts[2]);
        return new EventDate(day, month, year);
    }

    private static String readString(byte[] buf, int off) {
        int i = off;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (i < buf.length && buf[i] != 0) {
            baos.write(buf[i] == (byte)0xFF ? ' ' : buf[i]);
            i++;
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    private record HolderStudent(int id, int nameOffset) { }

    private static class HolderEvent {
        final int eventID, studentId, creationOffset, expiredOffset, typeOrdinal;
        final double mark;
        HolderEvent(int eid, int sid, int co, double mark, int eo, int typeOrdinal) {
            this.eventID = eid;
            this.studentId = sid;
            this.creationOffset = co;
            this.mark = mark;
            this.expiredOffset = eo;
            this.typeOrdinal = typeOrdinal;
        }
    }

    /** Внутренний переносчик данных, чтобы не тащить Student внутри EVTS‑пакета. */
    public static class SaverEvent {
        int id;
        int studentId;
        double mark;
        String creationDate;
        String description;
        String expiredDate;
        EventCategories type;

        SaverEvent(StudentEvent e, int studentId) {
            this.id = e.getID();
            this.studentId = studentId;
            this.creationDate = e.getCrtDate().toString();      // EventDate → String
            this.description = e.getDescription();
            this.mark = e.getMark();
            this.expiredDate = e.getExpDate().toString();        // EventDate → String
            this.type = e.getCategory();
        }
    }

    /*public static void main(String[] args) throws IOException {
        // Группа 1
        List<StudentEvent> events1 = new ArrayList<>();
        events1.add(new StudentEvent(10001, new EventDate(1, 1, 2025), "Math exam", 5, new EventDate(2, 1, 2025), EventTypes.MOD_1));
        events1.add(new StudentEvent(10002, new EventDate(3, 1, 2025), "Biology lab", 4, new EventDate(5, 1, 2025), EventTypes.MOD_2));
        events1.add(new StudentEvent(10003, new EventDate(5, 1, 2025), "Chemistry quiz", 3, new EventDate(6, 1, 2025), EventTypes.MOD_3));
        Student student1 = new Student("Артем");
        student1.setEvents(events1);

        List<StudentEvent> events2 = new ArrayList<>();
        events2.add(new StudentEvent(20001, new EventDate(2, 1, 2025), "History presentation", 5, new EventDate(3, 1, 2025), EventTypes.MOD_2));
        events2.add(new StudentEvent(20002, new EventDate(4, 1, 2025), "Physics forces", 4, new EventDate(6, 1, 2025), EventTypes.MOD_1));
        Student student2 = new Student("Оля");
        student2.setEvents(events2);

        Group group1 = new Group();
        group1.setStudents(List.of(student1, student2));

        // Группа 2
        List<StudentEvent> events3 = new ArrayList<>();
        events3.add(new StudentEvent(30001, new EventDate(7, 1, 2025), "Art portfolio presentation", 5, new EventDate(8, 1, 2025), EventTypes.MOD_3));
        events3.add(new StudentEvent(30002, new EventDate(9, 1, 2025), "Music theory test", 3, new EventDate(10, 1, 2025), EventTypes.MOD_1));
        Student student3 = new Student("Максим");
        student3.setEvents(events3);

        Group group2 = new Group();
        group2.setStudents(List.of(student3));

        // Группа 3
        List<StudentEvent> events4 = new ArrayList<>();
        events4.add(new StudentEvent(40001, new EventDate(11, 1, 2025), "Literature essay: Lesya Ukrainka", 4, new EventDate(13, 1, 2025), EventTypes.MOD_2));
        events4.add(new StudentEvent(40002, new EventDate(12, 1, 2025), "Geography quiz", 5, new EventDate(14, 1, 2025), EventTypes.MOD_3));
        Student student4 = new Student("Ірина");
        student4.setEvents(events4);

        List<StudentEvent> events5 = new ArrayList<>();
        events5.add(new StudentEvent(50001, new EventDate(14, 1, 2025), "Economics case study", 3, new EventDate(15, 1, 2025), EventTypes.MOD_1));
        events5.add(new StudentEvent(50002, new EventDate(15, 1, 2025), "Programming contest", 5, new EventDate(16, 1, 2025), EventTypes.MOD_2));
        Student student5 = new Student("Богдан");
        student5.setEvents(events5);

        Group group3 = new Group();
        group3.setStudents(List.of(student4, student5));

        // Группа 4
        List<StudentEvent> events6 = new ArrayList<>();
        events6.add(new StudentEvent(60001, new EventDate(17, 1, 2025), "Biology research project", 5, new EventDate(18, 1, 2025), EventTypes.MOD_3));
        events6.add(new StudentEvent(60002, new EventDate(18, 1, 2025), "Algebra exam", 4, new EventDate(19, 1, 2025), EventTypes.MOD_1));
        Student student6 = new Student("Наташа");
        student6.setEvents(events6);

        Group group4 = new Group();
        group4.setStudents(List.of(student6));

        // Собираем всё в массив
        Group[] course = new Group[]{ group1, group2, group3, group4 };

        // Сохраняем в файл
        File out = new File("demo.cman");
        buildPlainCman(course);
        System.out.println("Файл успешно сохранён по пути: " + out.getAbsolutePath());
    }*/
}