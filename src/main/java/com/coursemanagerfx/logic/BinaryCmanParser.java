package com.coursemanagerfx.logic;

/*import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;*/

//import static com.coursemanagerfx.logic.CmanSaver.GET_GROUPS_SPACER;

/*public class CmanParser {

    public static Group[] parseFile(String filePath) throws IOException {

        return new Group[0];
    }
    *//*public static Group[] parseFile(String filePath) throws IOException {
        List<Group> groups = new ArrayList<>();
        String fileContent = new String(Files.readAllBytes(Paths.get(filePath)));
        String[] lines = fileContent.split("\n");

        Group currentGroup = new Group();
        Student currentStudent = null;
        Integer tempId = null; // временная переменная для хранения id студента

        for (String line : lines) {
            line = line.trim();
            if (line.equals(GET_GROUPS_SPACER())) {
                groups.add(currentGroup);
                currentGroup = new Group();
                currentStudent = null;
                tempId = null;
            } else if (line.startsWith("---")) {
                String eventData = line.substring(3);
                String[] parts = eventData.split("&");
                if (parts.length != 4) {
                    System.out.println("Неверный формат строки события: " + line);
                    continue;
                }
                String creationDate = parts[0].trim();
                String eventDescription = parts[1].trim();
                int grade;
                try {
                    grade = Integer.parseInt(parts[2].trim());
                } catch (NumberFormatException e) {
                    System.out.println("Неверный формат оценки в строке: " + line);
                    continue;
                }
                String expiredDate = parts[3].trim();
                StudentEvent event = new StudentEvent(creationDate, eventDescription, grade, expiredDate);
                if (currentStudent != null) {
                    currentStudent.getEvents().add(event);
                } else {
                    System.out.println("Найдено событие без студента: " + line);
                }
            } else {
                // Строки, не являющиеся разделителями или событиями
                if (tempId == null) {
                    // Пытаемся распарсить как id
                    try {
                        tempId = Integer.parseInt(line);
                        // Если строка успешно распознана как число, идём дальше
                        continue;
                    } catch (NumberFormatException e) {
                        // Если не число, значит, это имя студента без id (но по условию такого быть не должно)
                        System.out.println("Ожидался числовой ID, но получена строка: " + line);
                        tempId = 0; // можно присвоить 0 или сгенерировать случайный id
                    }
                } else {
                    // Если tempId уже есть, то текущая строка — это имя студента
                    Student student = new Student(line, tempId);
                    currentGroup.getStudents().add(student);
                    currentStudent = student;
                    tempId = null; // сбрасываем для следующего студента
                }
            }
        }
        if (!currentGroup.getStudents().isEmpty()) {
            groups.add(currentGroup);
        }
        return groups.toArray(new Group[groups.size()]);
    }*//*


    *//*public static void main(String[] args) {
        String filePath = "C:\\Users\\Nazar\\AppData\\Local\\CourseManager\\Courses\\lol.cman";
        try {
            Group[] groups = parseFile(filePath);
            for (int i = 0; i < groups.length; i++) {
                System.out.println("Группа " + (i + 1) + ": " + groups[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*//*
}*/

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
                StudentEvent se = new StudentEvent(ev.eventID, creationDate, description, ev.mark, expiredDate);
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

    // Читает строку, заменяя байт 0xFF → пробел и завершаясь на 0x00
    private static String readString(byte[] buf, int off) {
        int i = off;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (i < buf.length && buf[i] != 0) {
            baos.write(buf[i] == (byte)0xFF ? ' ' : buf[i]);
            i++;
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
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
        File file = new File("C:\\Users\\Nazar\\.cmanfx\\Courses\\BinaryTest.cman");
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