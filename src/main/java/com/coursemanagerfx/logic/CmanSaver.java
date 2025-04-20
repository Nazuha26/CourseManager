package com.coursemanagerfx.logic;

import java.io.File;
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

    /**
     * Создаёт пустой .cman‑файл с указанным количеством разделителей групп.
     *
     * @param courseName  имя курса (без расширения)
     * @param groupsCount сколько групп нужно зарезервировать
     * @return созданный файл
     * @throws IOException если не удалось создать каталог или файл
     */
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
}
