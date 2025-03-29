/*
 * Created by JFormDesigner on Thu Mar 20 21:03:16 EET 2025
 */

package com.coursemanager.panels;

import java.awt.event.*;
import java.text.MessageFormat;
import java.util.*;

import com.coursemanager.CM_HELPER;
import com.coursemanager.other.Group;
import com.coursemanager.other.Student;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author Nazar
 */
public class MainGroupPanel extends JPanel {
    public MainGroupPanel() {
        initComponents();
    }

    private Group currentGroup;

    public Group getCurrentGroup() {
        return currentGroup;
    }

    public void setGroupData(int groupIndex, Group group) {
        lblGroup.setText(MessageFormat.format(CM_HELPER.getBundle().getString("MainGroupPanel.lblGroup.text"), groupIndex));
        currentGroup = group; // запоминаем текущую группу

        if (!group.students.isEmpty()){
            panelBox.removeAll();   // Удаляем все компоненты из panelBox

            // Для каждого студента создаём NamePartPanel и добавляем его
            for (int i = 0; i < group.students.size(); i++) {
                NamePartPanel namePanel = new NamePartPanel();
                Student st = group.students.get(i);
                namePanel.setStudentData(st, i + 1);
                panelBox.add(namePanel);
            }

            panelBox.revalidate();
            panelBox.repaint();
        }
    }

    public EventPanel getEventPanel() {
        return eventPanel;
    }

    private void btnAddStudent(ActionEvent e) {
        // Запрашиваем имя студента через диалоговое окно
        String studentName = JOptionPane.showInputDialog(this, CM_HELPER.getBundle().getString("MainGroupPanel.addStudent.text"));
        if (studentName == null || studentName.trim().isEmpty()) {
            return; // если пользователь нажал "Отмена" или ввёл пустую строку
        }
        studentName = studentName.trim();

        // Проверка: имя должно состоять только из русских/украинских букв и пробелов
        if (!studentName.matches("^[А-Яа-яЁёІіЇїЄє ]+$")) {
            JOptionPane.showMessageDialog(this,
                    "Имя может содержать только русские или украинские буквы и пробелы.",
                    "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Проверка на дубликаты во всех группах
        if (isStudentDuplicate(studentName)) { return; }

        // Если всё ок – создаём нового студента и добавляем его в текущую группу
        Student newStudent = new Student(studentName);
        currentGroup.students.add(newStudent);
        currentGroup.students.sort(Comparator.comparing(s -> s.name, String.CASE_INSENSITIVE_ORDER));

        setGroupData(CM_HELPER.getCourseGroupsList().indexOf(currentGroup) + 1, currentGroup);

        panelBox.revalidate();
        panelBox.repaint();
    }

    // Метод для проверки дубликатов по всем группам
    public static boolean isStudentDuplicate(String studentName) {
        List<Group> groups = CM_HELPER.getCourseGroupsList();
        if (groups != null) {
            for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
                Group group = groups.get(groupIndex);
                for (int studentIndex = 0; studentIndex < group.students.size(); studentIndex++) {
                    Student s = group.students.get(studentIndex);
                    if (s.name.equalsIgnoreCase(studentName)) {
                        JOptionPane.showMessageDialog(null,
                                "Студент с таким именем уже существует!\nГруппа: " + (groupIndex + 1) +
                                        ", номер: " + (studentIndex + 1),
                                "Ошибка", JOptionPane.WARNING_MESSAGE);
                        return true;
                    }
                }
            }
        }
        return false;
    }



    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        ResourceBundle bundle = ResourceBundle.getBundle("strings");
        stdsListPanel = new JPanel();
        panel = new JPanel();
        lblGroup = new JLabel();
        btnAddStudent = new JButton();
        scrlPanelStdsList = new JScrollPane();
        scrlPanelStdsList.getVerticalScrollBar().setUnitIncrement(15);
        panelBox = new JPanel();
        vSpacer3 = new JPanel(null);
        lblStdsInfo = new JLabel();
        vSpacer2 = new JPanel(null);
        eventPanel = new EventPanel();

        //======== this ========
        setBorder(new EmptyBorder(5, 0, 5, 5));
        setLayout(new BorderLayout(5, 0));

        //======== stdsListPanel ========
        {
            stdsListPanel.setLayout(new BorderLayout());

            //======== panel ========
            {
                panel.setBorder(new EmptyBorder(5, 5, 5, 5));
                panel.setLayout(new BorderLayout());

                //---- lblGroup ----
                lblGroup.setText(bundle.getString("MainGroupPanel.lblGroup.text"));
                lblGroup.setFont(lblGroup.getFont().deriveFont(lblGroup.getFont().getSize() + 5f));
                lblGroup.setHorizontalAlignment(SwingConstants.CENTER);
                panel.add(lblGroup, BorderLayout.CENTER);

                //---- btnAddStudent ----
                btnAddStudent.setText(bundle.getString("MainGroupPanel.btnAddStudent.text"));
                btnAddStudent.setPreferredSize(new Dimension(25, 25));
                btnAddStudent.setMinimumSize(new Dimension(25, 25));
                btnAddStudent.setMaximumSize(new Dimension(25, 25));
                btnAddStudent.setFont(btnAddStudent.getFont().deriveFont(btnAddStudent.getFont().getSize() + 5f));
                btnAddStudent.addActionListener(e -> btnAddStudent(e));
                panel.add(btnAddStudent, BorderLayout.WEST);
            }
            stdsListPanel.add(panel, BorderLayout.NORTH);

            //======== scrlPanelStdsList ========
            {
                scrlPanelStdsList.setBorder(new CompoundBorder(
                    new EmptyBorder(0, 5, 0, 5),
                    new BevelBorder(BevelBorder.LOWERED)));

                //======== panelBox ========
                {
                    panelBox.setBackground(new Color(0xd8d8d8));
                    panelBox.setLayout(new BoxLayout(panelBox, BoxLayout.Y_AXIS));

                    //---- vSpacer3 ----
                    vSpacer3.setOpaque(false);
                    panelBox.add(vSpacer3);

                    //---- lblStdsInfo ----
                    lblStdsInfo.setText(bundle.getString("MainGroupPanel.lblStdsInfo.text"));
                    lblStdsInfo.setAlignmentX(0.5F);
                    lblStdsInfo.setBorder(new EmptyBorder(0, 20, 0, 20));
                    lblStdsInfo.setFont(lblStdsInfo.getFont().deriveFont(lblStdsInfo.getFont().getSize() + 4f));
                    lblStdsInfo.setEnabled(false);
                    panelBox.add(lblStdsInfo);

                    //---- vSpacer2 ----
                    vSpacer2.setOpaque(false);
                    panelBox.add(vSpacer2);
                }
                scrlPanelStdsList.setViewportView(panelBox);
            }
            stdsListPanel.add(scrlPanelStdsList, BorderLayout.CENTER);
        }
        add(stdsListPanel, BorderLayout.WEST);

        //---- eventPanel ----
        eventPanel.setFont(eventPanel.getFont().deriveFont(eventPanel.getFont().getSize() + 13f));
        eventPanel.setEnabled(false);
        add(eventPanel, BorderLayout.CENTER);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JPanel stdsListPanel;
    private JPanel panel;
    private JLabel lblGroup;
    private JButton btnAddStudent;
    private JScrollPane scrlPanelStdsList;
    private JPanel panelBox;
    private JPanel vSpacer3;
    private JLabel lblStdsInfo;
    private JPanel vSpacer2;
    private EventPanel eventPanel;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
