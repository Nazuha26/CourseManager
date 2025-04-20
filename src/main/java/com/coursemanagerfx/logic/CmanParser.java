package com.coursemanagerfx.logic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.coursemanagerfx.logic.CmanSaver.GET_GROUPS_SPACER;

public class CmanParser {

    public static Group[] parseFile(String filePath) throws IOException {
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
    }


    public static void main(String[] args) {
        String filePath = "C:\\Users\\Nazar\\AppData\\Local\\CourseManager\\Courses\\lol.cman";
        try {
            Group[] groups = parseFile(filePath);
            for (int i = 0; i < groups.length; i++) {
                System.out.println("Группа " + (i + 1) + ": " + groups[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}