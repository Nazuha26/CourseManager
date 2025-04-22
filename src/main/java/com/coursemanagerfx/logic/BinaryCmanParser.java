package com.coursemanagerfx.logic;

import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.basic.Student;
import com.coursemanagerfx.logic.basic.event.StudentEvent;
import com.coursemanagerfx.logic.basic.event.date.EventDate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BinaryCmanParser {

    public static Group[] parse(File file) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            // 1) HEADER
            byte[] magic = new byte[4];
            raf.readFully(magic);
            if (!"HEAD".equals(new String(magic, StandardCharsets.US_ASCII))) {
                throw new IOException("Некорректный файл: not HEAD magic");
            }
            int groupsCount = raf.readInt();
            int offSTDS = raf.readInt();
            int offTBLN = raf.readInt();
            int offEVTS = raf.readInt();
            int offTBLE = raf.readInt();
            raf.skipBytes(8); // зарезервированные нули

            // 2) Читаем секцию STDS
            raf.seek(offSTDS);
            raf.readFully(magic);
            if (!"STDS".equals(new String(magic, StandardCharsets.US_ASCII))) {
                throw new IOException("Ожидалась секция STDS");
            }
            List<List<HolderStudent>> rawGroups = new ArrayList<>();
            for (int gi = 0; gi < groupsCount; gi++) {
                int cnt = raf.readUnsignedShort();
                List<HolderStudent> lst = new ArrayList<>();
                for (int i = 0; i < cnt; i++) {
                    int sid      = raf.readInt();
                    int nameOff  = raf.readInt();
                    lst.add(new HolderStudent(sid, nameOff));
                }
                rawGroups.add(lst);
            }

            // 3) Читаем TBLN (имена)
            int namesLen = offEVTS - (offTBLN + 4);
            raf.seek(offTBLN + 4);
            byte[] namesBuf = new byte[namesLen];
            raf.readFully(namesBuf);

            // Строим реальные объекты Student
            List<List<Student>> groups = new ArrayList<>();
            for (var rawG : rawGroups) {
                List<Student> students = new ArrayList<>();
                for (var h : rawG) {
                    String name = readString(namesBuf, h.nameOffset);
                    students.add(new Student(name, h.id));
                }
                groups.add(students);
            }

            // 4) Читаем EVTS
            raf.seek(offEVTS);
            raf.readFully(magic);
            if (!"EVTS".equals(new String(magic, StandardCharsets.US_ASCII))) {
                throw new IOException("Ожидалась секция EVTS");
            }
            int totalEvents = raf.readInt();
            List<HolderEvent> rawEvents = new ArrayList<>();
            for (int i = 0; i < totalEvents; i++) {
                int eid        = raf.readInt();
                int sid        = raf.readInt();
                int creationOff= raf.readInt();
                int mark       = raf.readInt();
                int expiredOff = raf.readInt();
                rawEvents.add(new HolderEvent(eid, sid, creationOff, mark, expiredOff));
            }

            // 5) Читаем TBLE (тексты ивентов)
            long fileLen = raf.length();
            int textLen = (int)(fileLen - (offTBLE + 4));
            raf.seek(offTBLE + 4);
            byte[] textBuf = new byte[textLen];
            raf.readFully(textBuf);

            // 6) Распределяем события по студентам
            for (var ev : rawEvents) {
                // 6.1) дата создания
                String creationDate = readString(textBuf, ev.creationOffset);

                // 6.2) описание идёт сразу после нуля окончания первой строки
                int idx = ev.creationOffset;
                while (idx < textBuf.length && textBuf[idx] != 0) idx++;
                String description = "";
                if (idx + 1 < textBuf.length) {
                    description = readString(textBuf, idx + 1);
                }

                // 6.3) дата истечения
                String expiredDate = readString(textBuf, ev.expiredOffset);

                // 6.4) создаём StudentEvent и добавляем в нужный Student
                StudentEvent se = new StudentEvent(
                        ev.eventID,
                        parseEventDate(creationDate),
                        description,
                        ev.mark,
                        parseEventDate(expiredDate)
                );

                outer:
                for (List<Student> grp : groups) {
                    for (Student s : grp) {
                        if (s.getID() == ev.studentId) {
                            s.getEvents().add(se);
                            break outer;
                        }
                    }
                }
            }

            // 7) Формируем итоговый массив Group[]
            Group[] result = new Group[groupsCount];
            for (int i = 0; i < groupsCount; i++) {
                Group g = new Group();
                g.setStudents(groups.get(i));
                result[i] = g;
            }
            return result;
        }
    }

    private static EventDate parseEventDate(String s) {
        String[] parts = s.split("\\.");
        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int year = Integer.parseInt(parts[2]);
        return new EventDate(day, month, year);
    }

    // Читает строку, заменяя байт 0xFF → пробел и завершаясь на 0x00
    private static String readString(byte[] buf, int off) {
        int i = off;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (i < buf.length && buf[i] != 0) {
            baos.write(buf[i] == (byte)0xFF ? ' ' : buf[i]);
            i++;
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    // Вспомогательные хранилища до сборки объектов
    private static class HolderStudent {
        final int id, nameOffset;
        HolderStudent(int id, int nameOffset) {
            this.id = id; this.nameOffset = nameOffset;
        }
    }
    private static class HolderEvent {
        final int eventID, studentId, creationOffset, mark, expiredOffset;
        HolderEvent(int eid, int sid, int co, int mark, int eo) {
            this.eventID = eid;
            this.studentId = sid;
            this.creationOffset = co;
            this.mark = mark;
            this.expiredOffset = eo;
        }
    }

    public static void main(String[] args) {
        File file = new File("C:\\Users\\Nazar\\.cmanfx\\Courses\\Test.cman");
        try {
            Group[] groups = parse(file);
            for (int i = 0; i < groups.length; i++) {
                System.out.println("Группа " + (i + 1) + ": " + groups[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}