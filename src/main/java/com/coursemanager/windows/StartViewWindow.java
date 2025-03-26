/*
 * Created by JFormDesigner on Mon Mar 17 13:13:56 EET 2025
 */

package com.coursemanager.windows;

import com.coursemanager.dialogs.NewCourseDialog;

import java.awt.*;
import java.awt.event.*;
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
        JOptionPane.showMessageDialog(this, "Soon...");

        // TODO Something
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        panel1 = new JPanel();
        btnNewCourse = new JButton();
        btnExistingCourse = new JButton();

        //======== this ========
        setTitle("Course Manager - Start");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(500, 300));
        setMinimumSize(new Dimension(500, 300));
        setResizable(false);
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== panel1 ========
        {
            panel1.setBorder(new EmptyBorder(5, 5, 5, 5));
            panel1.setLayout(new GridBagLayout());

            //---- btnNewCourse ----
            btnNewCourse.setText("Create a new course");
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
            btnExistingCourse.setText("Open an existing course  ");
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
