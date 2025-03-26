package com.coursemanager;

import com.coursemanager.other.CmanParser;
import com.coursemanager.other.CryptoUtils;
import com.coursemanager.other.SecurityUtils;
import com.coursemanager.windows.MainCoursesWindow;
import com.coursemanager.windows.StartViewWindow;
//import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class Launcher {
    public static StartViewWindow startView;

    public static void main(String[] args) {
        if (!CM_HELPER.secretKeyDir.exists()) {
            CM_HELPER.secretKeyDir.mkdirs();
        }
        File envFile = new File(CM_HELPER.secretKeyDir, ".env");
        if (!envFile.exists()) {
            String randomKey = SecurityUtils.generateRandomKey(16);
            try (FileWriter writer = new FileWriter(envFile)) {
                writer.write("CMAN_SECRET_KEY=" + randomKey);
                System.out.println("Secret key successfully created");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Secret key already exists");
        }

        if (!CM_HELPER.courseDir.exists()) {
            CM_HELPER.courseDir.mkdirs();
        }
        if (!CM_HELPER.configDir.exists()) {
            CM_HELPER.configDir.mkdirs();
        }

        CM_HELPER.initTheme();  // Подключаем тему FlatLaf

        SwingUtilities.invokeLater(() -> {
            if (CM_HELPER.firstRunFile.exists()) {
                // Если файл FirstRun есть, читаем данные и открываем CourseView
                try {
                    CM_HELPER.setCourseName(Files.readString(CM_HELPER.firstRunFile.toPath()));

                    /*CourseManagerFileParser parser = new CourseManagerFileParser();
                    parser.parse(courseDir + "/" + courseName + ".cman");

                    //                      *** DEBUG ***
                    System.out.println("Количество групп: " + parser.groupCount);
                    int index = 1;
                    for (CourseManagerFileParser.Group group : parser.groups) {
                        System.out.println("Группа " + index + " (ожидается студентов: " + group.expectedStudentCount + "):");
                        for (CourseManagerFileParser.Student student : group.students) {
                            System.out.println(" - " + student.name);
                            // Если нужно вывести ивенты:
                            for (CourseManagerFileParser.StudentEvent event : student.events) {
                                System.out.printf("   * %d | %s | %s | %s\n",
                                        event.ordinal,
                                        event.creationDate,
                                        event.eventDescription,
                                        event.expiredDate
                                );
                            }
                        }
                        index++;
                    }*/
                    //                      *** DEBUG ***

                    //courseGroups = parser.groups;

                    CM_HELPER.setFileCoursePath(CM_HELPER.courseDir + "/" + CM_HELPER.getCourseName() + ".cman");

                    try {
                        CM_HELPER.setCourseGroupsList(CmanParser.parseFile(CM_HELPER.getFileCoursePath()));
                        //                      *** DEBUG ***
                        for (int i = 0; i < CM_HELPER.getCourseGroupsList().size(); i++) {
                            System.out.println("Группа " + (i + 1) + ": " + CM_HELPER.getCourseGroupsList().get(i));
                        }
                        //                      *** DEBUG ***
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (!CM_HELPER.getCourseGroupsList().isEmpty()) {
                        MainCoursesWindow mainCoursesWindow = new MainCoursesWindow();
                        mainCoursesWindow.populateTabs(CM_HELPER.getCourseGroupsList());
                        mainCoursesWindow.setExtendedState(Frame.MAXIMIZED_BOTH);
                        mainCoursesWindow.setTitle("CourseManager - " + CM_HELPER.getCourseName());
                        mainCoursesWindow.setVisible(true);
                    } else {
                        showStartView(); // если файл повреждён или пуст
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    showStartView(); // если файл повреждён, откроем StartView
                }
            } else {
                // Если FirstRun нет, показываем StartView
                showStartView();
            }
        });
    }

    private static void showStartView() {
        startView = new StartViewWindow();
        startView.setVisible(true);
    }
}
