package com.coursemanagerfx.logic.security;

import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.basic.Student;
import com.coursemanagerfx.logic.basic.event.EventCategories;
import com.coursemanagerfx.logic.basic.event.StudentEvent;
import com.coursemanagerfx.logic.basic.event.date.EventDate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestCmanUtility {
    private static void testCreate(String password) {
        // Группа 1
        List<StudentEvent> events1 = new ArrayList<>();
        events1.add(new StudentEvent(10001, new EventDate(1, 1, 2025), "Math exam", 5, new EventDate(2, 1, 2025), EventCategories.MOD_1));
        events1.add(new StudentEvent(10002, new EventDate(3, 1, 2025), "Biology lab", 4, new EventDate(5, 1, 2025), EventCategories.MOD_2));
        events1.add(new StudentEvent(10003, new EventDate(5, 1, 2025), "Chemistry quiz", 3, new EventDate(6, 1, 2025), EventCategories.MOD_3));
        Student student1 = new Student("Артем", 1);
        student1.setEvents(events1);

        List<StudentEvent> events2 = new ArrayList<>();
        events2.add(new StudentEvent(20001, new EventDate(2, 1, 2025), "History presentation", 5, new EventDate(3, 1, 2025), EventCategories.MOD_2));
        events2.add(new StudentEvent(20002, new EventDate(4, 1, 2025), "Physics forces", 4, new EventDate(6, 1, 2025), EventCategories.MOD_1));
        Student student2 = new Student("Оля", 2);
        student2.setEvents(events2);

        Group group1 = new Group();
        group1.setStudents(List.of(student1, student2));

        // Группа 2
        List<StudentEvent> events3 = new ArrayList<>();
        events3.add(new StudentEvent(30001, new EventDate(7, 1, 2025), "Art portfolio presentation", 5, new EventDate(8, 1, 2025), EventCategories.MOD_3));
        events3.add(new StudentEvent(30002, new EventDate(9, 1, 2025), "Music theory test", 3, new EventDate(10, 1, 2025), EventCategories.MOD_1));
        Student student3 = new Student("Максим", 3);
        student3.setEvents(events3);

        Group group2 = new Group();
        group2.setStudents(List.of(student3));

        // Группа 3
        List<StudentEvent> events4 = new ArrayList<>();
        events4.add(new StudentEvent(40001, new EventDate(11, 1, 2025), "Literature essay: Lesya Ukrainka", 4, new EventDate(13, 1, 2025), EventCategories.MOD_2));
        events4.add(new StudentEvent(40002, new EventDate(12, 1, 2025), "Geography quiz", 5, new EventDate(14, 1, 2025), EventCategories.MOD_3));
        Student student4 = new Student("Ірина", 4);
        student4.setEvents(events4);

        List<StudentEvent> events5 = new ArrayList<>();
        events5.add(new StudentEvent(50001, new EventDate(14, 1, 2025), "Economics case study", 3, new EventDate(15, 1, 2025), EventCategories.MOD_1));
        events5.add(new StudentEvent(50002, new EventDate(15, 1, 2025), "Programming contest", 5, new EventDate(16, 1, 2025), EventCategories.MOD_2));
        Student student5 = new Student("Богдан", 5);
        student5.setEvents(events5);

        Group group3 = new Group();
        group3.setStudents(List.of(student4, student5));

        // Группа 4
        List<StudentEvent> events6 = new ArrayList<>();
        events6.add(new StudentEvent(60001, new EventDate(17, 1, 2025), "Biology research project", 5, new EventDate(18, 1, 2025), EventCategories.MOD_3));
        events6.add(new StudentEvent(60002, new EventDate(18, 1, 2025), "Algebra exam", 4, new EventDate(19, 1, 2025), EventCategories.MOD_1));
        Student student6 = new Student("Наташа", 6);
        student6.setEvents(events6);

        Group group4 = new Group();
        group4.setStudents(List.of(student6));

        // Собираем всё в массив
        Group[] course = new Group[]{ group1, group2, group3, group4 };

        // Сохраняем в файл
        File out = new File("demo.cman");
        try {
            CmanSecurityUtility.createSecureFile(course, out, password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Файл успешно создан по пути: " + out.getAbsolutePath());
    }
    private static void testSave(String password) {
        // Группа 1
        List<StudentEvent> events1 = new ArrayList<>();
        events1.add(new StudentEvent(10001, new EventDate(1, 1, 2025), "Math exam", 5, new EventDate(2, 1, 2025), EventCategories.MOD_1));
        events1.add(new StudentEvent(10002, new EventDate(3, 1, 2025), "Biology lab", 4, new EventDate(5, 1, 2025), EventCategories.MOD_2));
        events1.add(new StudentEvent(10003, new EventDate(5, 1, 2025), "Chemistry quiz", 3, new EventDate(6, 1, 2025), EventCategories.MOD_3));
        Student student1 = new Student("Артем", 1);
        student1.setEvents(events1);

        List<StudentEvent> events2 = new ArrayList<>();
        events2.add(new StudentEvent(20001, new EventDate(2, 1, 2025), "History presentation", 5, new EventDate(3, 1, 2025), EventCategories.MOD_2));
        events2.add(new StudentEvent(20002, new EventDate(4, 1, 2025), "Physics forces", 4, new EventDate(6, 1, 2025), EventCategories.MOD_1));
        Student student2 = new Student("Оля", 2);
        student2.setEvents(events2);

        Group group1 = new Group();
        group1.setStudents(List.of(student1, student2));

        // Группа 2
        List<StudentEvent> events3 = new ArrayList<>();
        events3.add(new StudentEvent(30001, new EventDate(7, 1, 2025), "Art portfolio presentation", 5, new EventDate(8, 1, 2025), EventCategories.MOD_3));
        events3.add(new StudentEvent(30002, new EventDate(9, 1, 2025), "Music theory test", 3, new EventDate(10, 1, 2025), EventCategories.MOD_1));
        Student student3 = new Student("Максим", 3);
        student3.setEvents(events3);

        Group group2 = new Group();
        group2.setStudents(List.of(student3));

        // Группа 3
        List<StudentEvent> events4 = new ArrayList<>();
        events4.add(new StudentEvent(40001, new EventDate(11, 1, 2025), "Literature essay: Lesya Ukrainka", 4, new EventDate(13, 1, 2025), EventCategories.MOD_2));
        events4.add(new StudentEvent(40002, new EventDate(12, 1, 2025), "Geography quiz", 5, new EventDate(14, 1, 2025), EventCategories.MOD_3));
        Student student4 = new Student("Ірина", 4);
        student4.setEvents(events4);

        List<StudentEvent> events5 = new ArrayList<>();
        events5.add(new StudentEvent(50001, new EventDate(14, 1, 2025), "Economics case study", 3, new EventDate(15, 1, 2025), EventCategories.MOD_1));
        events5.add(new StudentEvent(50002, new EventDate(15, 1, 2025), "Programming contest", 5, new EventDate(16, 1, 2025), EventCategories.MOD_2));
        Student student5 = new Student("Богдан", 5);
        student5.setEvents(events5);

        Group group3 = new Group();
        group3.setStudents(List.of(student4, student5));

        // Группа 4
        /*List<StudentEvent> events6 = new ArrayList<>();
        events6.add(new StudentEvent(60001, new EventDate(17, 1, 2025), "Biology research project", 5, new EventDate(18, 1, 2025), EventTypes.MOD_3));
        events6.add(new StudentEvent(60002, new EventDate(18, 1, 2025), "Algebra exam", 4, new EventDate(19, 1, 2025), EventTypes.MOD_1));
        Student student6 = new Student("Наташа");
        student6.setEvents(events6);

        List<StudentEvent> events7 = new ArrayList<>();
        events7.add(new StudentEvent(60003, new EventDate(1, 12, 2027), "Obosrat vse unitazi", 50, new EventDate(21, 2, 2030), EventTypes.MOD_3));
        Student student7 = new Student("Генадич");
        student7.setEvents(events7);

        Group group4 = new Group();
        group4.setStudents(List.of(student6, student7));*/

        // Собираем всё в массив
        Group[] course = new Group[]{ group1, group2, group3, };//group4 };

        // Сохраняем в файл
        File out = new File("demo.cman");
        try {
            CmanSecurityUtility.updateSecureFile(course, out, password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Файл успешно сохранён по пути: " + out.getAbsolutePath());
    }

    private static void testParse(String password) {
        File file = new File("demo.cman");
        Group[] groups = null;
        try {
            groups = CmanSecurityUtility.readSecureFile(file, password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < groups.length; i++) {
            System.out.println("Группа " + (i + 1) + ": " + groups[i]);
        }
    }
    public static void main(String[] args) throws IOException {
        //testCreate("1234567890a");
        //testSave("1234567890a");
        testParse("1234567890a");
    }
}