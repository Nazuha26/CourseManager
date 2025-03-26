package com.coursemanager.other;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CourseFileSaver {
    public static void save(String filePath, List<Group> groups) throws IOException {
        StringBuilder sb = new StringBuilder();
        // Для каждой группы
        for (Group group : groups) {
            // Для каждого студента в группе
            for (Student student : group.students) {
                // Записываем имя студента
                sb.append(student.name).append("\n");
                // Если у студента есть события, записываем их
                for (StudentEvent event : student.events) {
                    sb.append("---")
                            .append(event.creationDate).append("&")
                            .append(event.eventDescription).append("&")
                            .append(event.grade).append("&")
                            .append(event.expiredDate).append("\n");
                }
            }
            // Разделитель групп
            sb.append("*****\n");
        }
        try {
            byte[] encryptedData = CryptoUtils.encrypt(sb.toString().getBytes());
            Files.write(Paths.get(filePath), encryptedData);
        } catch (Exception e) {
            throw new IOException("Ошибка шифрования файла", e);
        }
    }
}
