/*
 * Created by JFormDesigner on Mon Mar 17 09:55:33 EET 2025
 */

package com.coursemanager.dialogs;

import java.util.*;
import com.coursemanager.CM_HELPER;
import com.coursemanager.Launcher;
import com.coursemanager.other.CourseFileSaver;
import com.coursemanager.other.Group;
import com.coursemanager.windows.MainCoursesWindow;
import raven.toast.Notifications;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author Nazar
 */
public class NewCourseDialog extends JDialog {
    public NewCourseDialog(Window owner) {
        super(owner);
        initComponents();
    }

    private void btnCreate(ActionEvent e) {
        String courseName = txtfieldCourseName.getText().trim();
        int groupCount = (int) spinCountGrps.getValue();

        if (courseName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "The course name cannot be empty!",
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File newCourseFile = new File(CM_HELPER.COURSES_DIR, courseName + ".cman");
        if (newCourseFile.exists()) {
            JOptionPane.showMessageDialog(this,
                    "A course with this name already exists!",
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Создаём файл, если его ещё нет
            if (!newCourseFile.createNewFile()) {
                JOptionPane.showMessageDialog(this,
                        "Failed to create the course...",
                        "ERROR", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Пишем в файл нужное количество групп
            /*try (FileWriter writer = new FileWriter(newCourseFile)) {
                for (int i = 0; i < groupCount; i++) {
                    writer.write("*****\n");
                }
            }*/

            // Записываем имя курса в файл "firstRun"
            try (FileWriter fr = new FileWriter(CM_HELPER.FIRST_RUN_FILE)) {
                fr.write(courseName);
            }

            // Открываем курс
            List<Group> groups = new ArrayList<>();
            for (int i = 0; i < groupCount; i++) {
                groups.add(new Group());
            }
            CM_HELPER.setCourseName(courseName);
            CM_HELPER.setCourseGroupsList(groups);
            CM_HELPER.setFileCoursePath(newCourseFile.getAbsolutePath());

            MainCoursesWindow mainCoursesWindow = new MainCoursesWindow();
            mainCoursesWindow.populateTabs(groups);
            mainCoursesWindow.setExtendedState(Frame.MAXIMIZED_BOTH);
            mainCoursesWindow.setTitle("CourseManager - " + courseName);
            mainCoursesWindow.setVisible(true);
            Launcher.startView.dispose();
            this.dispose();

            CourseFileSaver.save(newCourseFile.getAbsolutePath(), groups);     // СОХРАНЯЕМ
            Notifications.getInstance().show(Notifications.Type.SUCCESS,
                    CM_HELPER.getBundle().getString("NewCourseDialog.successCreatedFile.text"));

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error creating course: " + ex.getMessage(),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        ResourceBundle bundle = ResourceBundle.getBundle("strings");
        panel1 = new JPanel();
        lblCourseName = new JLabel();
        txtfieldCourseName = new JTextField();
        lblCountGrps = new JLabel();
        spinCountGrps = new JSpinner();
        btnPanel = new JPanel();
        btnCreate = new JButton();

        //======== this ========
        setModal(true);
        setTitle(bundle.getString("NewCourseDialog.this.title"));
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== panel1 ========
        {
            panel1.setBorder(new EmptyBorder(10, 10, 5, 10));
            panel1.setLayout(new GridBagLayout());

            //---- lblCourseName ----
            lblCourseName.setText(bundle.getString("NewCourseDialog.lblCourseName.text"));
            lblCourseName.setHorizontalAlignment(SwingConstants.RIGHT);
            panel1.add(lblCourseName, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 10, 10), 0, 0));

            //---- txtfieldCourseName ----
            txtfieldCourseName.setText("NewCourse 17.03");
            panel1.add(txtfieldCourseName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 10, 0), 0, 0));

            //---- lblCountGrps ----
            lblCountGrps.setText(bundle.getString("NewCourseDialog.lblCountGrps.text"));
            lblCountGrps.setHorizontalAlignment(SwingConstants.RIGHT);
            panel1.add(lblCountGrps, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 10, 10), 0, 0));

            //---- spinCountGrps ----
            spinCountGrps.setModel(new SpinnerNumberModel(1, 1, 100, 1));
            panel1.add(spinCountGrps, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 10, 0), 0, 0));
        }
        contentPane.add(panel1, BorderLayout.CENTER);

        //======== btnPanel ========
        {
            btnPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
            btnPanel.setLayout(new GridBagLayout());
            ((GridBagLayout)btnPanel.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
            ((GridBagLayout)btnPanel.getLayout()).rowHeights = new int[] {0, 0};
            ((GridBagLayout)btnPanel.getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0, 1.0E-4};
            ((GridBagLayout)btnPanel.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

            //---- btnCreate ----
            btnCreate.setText(bundle.getString("NewCourseDialog.btnCreate.text"));
            btnCreate.setMinimumSize(new Dimension(30, 34));
            btnCreate.setPreferredSize(new Dimension(30, 34));
            btnCreate.setFont(btnCreate.getFont().deriveFont(btnCreate.getFont().getSize() + 1f));
            btnCreate.addActionListener(e -> btnCreate(e));
            btnPanel.add(btnCreate, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        }
        contentPane.add(btnPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JPanel panel1;
    private JLabel lblCourseName;
    private JTextField txtfieldCourseName;
    private JLabel lblCountGrps;
    private JSpinner spinCountGrps;
    private JPanel btnPanel;
    private JButton btnCreate;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
