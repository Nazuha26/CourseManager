package com.coursemanager.other;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CmanParser {

    public static List<Group> parseFile(String filePath) throws IOException {
        List<Group> groups = new ArrayList<>();
        byte[] encryptedData = Files.readAllBytes(Paths.get(filePath));
        byte[] decryptedData;
        try {
            decryptedData = CryptoUtils.decrypt(encryptedData);
        } catch (Exception e) {
            throw new IOException("Ошибка дешифрования файла", e);
        }
        List<String> lines = Arrays.asList(new String(decryptedData).split("\n"));

        System.out.println("\n--- PARSED FILE ---");
        for (String line : lines) {
            System.out.println(line);
        }
        System.out.println("\n-------------------");

        Group currentGroup = new Group();
        Student currentStudent = null;

        // Обрабатываем каждую строку
        for (String line : lines) {
            line = line.trim();
            if (line.equals("*****")) {
                groups.add(currentGroup);
                currentGroup = new Group();
                currentStudent = null;
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
                    currentStudent.events.add(event);
                } else {
                    System.out.println("Найдено событие без студента: " + line);
                }
            } else {
                Student student = new Student(line);
                currentGroup.students.add(student);
                currentStudent = student;
            }
        }
        if (!currentGroup.students.isEmpty()) {
            groups.add(currentGroup);
        }
        return groups;
    }

    public static void main(String[] args) {
        String filePath = "C:\\Users\\Nazar\\AppData\\Local\\CourseManager\\Courses\\NewFormatCourse.cman";
        try {
            List<Group> groups = parseFile(filePath);
            for (int i = 0; i < groups.size(); i++) {
                System.out.println("Группа " + (i + 1) + ": " + groups.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
