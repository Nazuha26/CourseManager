/*
 * Created by JFormDesigner on Mon Mar 17 13:13:56 EET 2025
 */

package com.coursemanager.windows;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.coursemanager.CM_HELPER;
import com.coursemanager.dialogs.NewCourseDialog;
import com.coursemanager.other.CmanParser;
import com.coursemanager.other.Group;
import jnafilechooser.api.JnaFileChooser;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author Nazar
 */
public class StartViewWindow extends JFrame {
    public StartViewWindow() {
        initComponents();
    }

    private void btnNewCourse(ActionEvent e) {
        JDialog newCourseDialog = new NewCourseDialog(this);
        newCourseDialog.setVisible(true);
    }

    private void btnExistingCourse(ActionEvent e) {
        JnaFileChooser chooser = new JnaFileChooser();

        chooser.setMode(JnaFileChooser.Mode.Files);
        chooser.setCurrentDirectory(CM_HELPER.COURSES_DIR.getAbsolutePath()); // Стартовая директория
        chooser.addFilter("Course Manager files (*.cman)", "cman"); // Фильтр по расширению

        boolean action = chooser.showOpenDialog(this);
        if (action) {
            File file = chooser.getSelectedFile();

            try {
                // Сохраняем путь и имя курса
                CM_HELPER.setFileCoursePath(file.getAbsolutePath());
                CM_HELPER.setCourseName(file.getName().replace(".cman", ""));

                // Сохраняем в файл для автозагрузки при следующем запуске
                try (FileWriter writer = new FileWriter(CM_HELPER.FIRST_RUN_FILE)) {
                    writer.write(CM_HELPER.getCourseName().trim());
                }

                // Парсим файл и обновляем UI
                List<Group> groups = CmanParser.parseFile(file.getAbsolutePath());
                CM_HELPER.setCourseGroupsList(groups);

                // Открываем главное окно
                MainCoursesWindow mainWindow = new MainCoursesWindow();
                mainWindow.populateTabs(groups);
                mainWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
                mainWindow.setTitle("CourseManager - " + CM_HELPER.getCourseName());
                mainWindow.setVisible(true);

                this.dispose();

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка при открытии файла: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        ResourceBundle bundle = ResourceBundle.getBundle("strings");
        panel1 = new JPanel();
        btnNewCourse = new JButton();
        btnExistingCourse = new JButton();

        //======== this ========
        setTitle(bundle.getString("StartViewWindow.this.title"));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(500, 300));
        setMinimumSize(new Dimension(500, 300));
        setResizable(false);
        setIconImage(new ImageIcon(getClass().getResource("/Icons/AppIcon.png")).getImage());
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== panel1 ========
        {
            panel1.setBorder(new EmptyBorder(5, 5, 5, 5));
            panel1.setLayout(new GridBagLayout());

            //---- btnNewCourse ----
            btnNewCourse.setText(bundle.getString("StartViewWindow.btnNewCourse.text"));
            btnNewCourse.setFont(btnNewCourse.getFont().deriveFont(btnNewCourse.getFont().getSize() + 5f));
            btnNewCourse.setAlignmentY(1.5F);
            btnNewCourse.setPreferredSize(new Dimension(250, 70));
            btnNewCourse.setMaximumSize(new Dimension(250, 70));
            btnNewCourse.setMinimumSize(new Dimension(250, 70));
            btnNewCourse.addActionListener(e -> btnNewCourse(e));
            panel1.add(btnNewCourse, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 15, 0), 0, 0));

            //---- btnExistingCourse ----
            btnExistingCourse.setText(bundle.getString("StartViewWindow.btnExistingCourse.text"));
            btnExistingCourse.setFont(btnExistingCourse.getFont().deriveFont(btnExistingCourse.getFont().getSize() + 5f));
            btnExistingCourse.setPreferredSize(new Dimension(270, 70));
            btnExistingCourse.setMaximumSize(new Dimension(270, 70));
            btnExistingCourse.setMinimumSize(new Dimension(270, 70));
            btnExistingCourse.addActionListener(e -> btnExistingCourse(e));
            panel1.add(btnExistingCourse, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 0, 0), 0, 0));
        }
        contentPane.add(panel1, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JPanel panel1;
    private JButton btnNewCourse;
    private JButton btnExistingCourse;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
