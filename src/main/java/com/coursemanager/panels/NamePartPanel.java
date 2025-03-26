/*
 * Created by JFormDesigner on Mon Mar 17 21:00:50 EET 2025
 */

package com.coursemanager.panels;

import java.awt.*;
import java.awt.event.*;
import java.util.Comparator;
import javax.swing.*;
import javax.swing.border.*;

import com.coursemanager.CM_HELPER;
import com.coursemanager.other.Student;
import com.coursemanager.ui.*;
import com.formdev.flatlaf.extras.*;
import raven.toast.Notifications;

/**
 * @author Nazar
 */
public class NamePartPanel extends JPanel {
    public NamePartPanel() {
        initComponents();
    }

    private boolean isStudentSelected = false;
    private Student student;

    private void miRename(ActionEvent e) {
        // Спрашиваем новое имя с текущим по умолчанию
        String newName = JOptionPane.showInputDialog(null, "Введите новое имя студента:", student.name);
        if (newName == null || newName.trim().isEmpty()) {
            return; // отмена или пустой ввод
        }
        newName = newName.trim();

        // Проверка: только рус/укр буквы и пробелы
        if (!newName.matches("^[А-Яа-яЁёІіЇїЄє ]+$")) {
            JOptionPane.showMessageDialog(null,
                    "Имя может содержать только русские или украинские буквы и пробелы.",
                    "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Если имя не изменилось — ничего не делаем
        if (newName.equalsIgnoreCase(student.name)) {
            return;
        }

        // Проверка на дубликат
        if (MainGroupPanel.isStudentDuplicate(newName)) {
            return; // внутри уже показывается сообщение об ошибке
        }

        // Всё ок — переименовываем
        student.name = newName;
        lblNameofDude.setText(newName);

        MainGroupPanel mainGroup = (MainGroupPanel) SwingUtilities.getAncestorOfClass(MainGroupPanel.class, this);
        if (mainGroup != null) {
            mainGroup.getCurrentGroup().students.sort(Comparator.comparing(s -> s.name, String.CASE_INSENSITIVE_ORDER));
            mainGroup.setGroupData(CM_HELPER.getCourseGroupsList().indexOf(mainGroup.getCurrentGroup()) + 1, mainGroup.getCurrentGroup());
        }
    }

    private void miChangeGroup(ActionEvent e) {
        JOptionPane.showMessageDialog(null, "Soon...");
    }

    private void miDelete(ActionEvent e) {
        // Запрос подтверждения удаления
        int choice = JOptionPane.showConfirmDialog(
                null,
                "Вы действительно хотите удалить студента " + student.name + " и все его ивенты?",
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION
        );
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        // Находим родительскую панель (MainGroupPanel)
        MainGroupPanel mainGroup = (MainGroupPanel) SwingUtilities.getAncestorOfClass(MainGroupPanel.class, this);
        if (mainGroup != null) {
            // Удаляем студента из текущей группы
            mainGroup.getCurrentGroup().students.remove(student);

            // Удаляем панель этого студента из контейнера списка
            Container parent = this.getParent();
            if (parent != null) {
                parent.remove(this);
                parent.revalidate();
                parent.repaint();

                // Обновляем нумерацию оставшихся NamePartPanel
                int num = 1;
                for (Component comp : parent.getComponents()) {
                    if (comp instanceof NamePartPanel) {
                        ((NamePartPanel) comp).updateStudentNumber(num);
                        num++;
                    }
                }
            }

            // Если удалённый студент был выбран, очищаем таблицу ивентов
            mainGroup.getEventPanel().updateTableData(new java.util.ArrayList<>());
        }
        Notifications.getInstance().show(Notifications.Type.SUCCESS, "Student removed successfully");
    }

    public void updateStudentNumber(int number) {
        lblNumOfDude.setText(String.valueOf(number));
    }

    public void setStudentData(Student student, int number) {
        this.student = student;
        lblNameofDude.setText(student.name);
        lblNumOfDude.setText(String.valueOf(number));
    }

    public void setSelected(boolean selected) {
        this.isStudentSelected = selected;
        if (selected) {
            roundedPanel.setPanelColor(new Color(0, 0, 0, 128));
            lblNameofDude.setForeground(Color.WHITE);
        } else {
            roundedPanel.setPanelColor(new Color(0x32000000, true));
            lblNameofDude.setForeground(Color.BLACK);
        }
        roundedPanel.repaint();
    }

    public void clearSelection() {
        setSelected(false);
    }


    public RoundedPanel getRoundedPanel() {
        return roundedPanel;
    }

    public JLabel getLblNameofDude() {
        return lblNameofDude;
    }

    public JLabel getLblNumOfDude() {
        return lblNumOfDude;
    }

    private void roundedPanelMouseClicked(MouseEvent e) {
        if (isStudentSelected){
            Notifications.getInstance().show(Notifications.Type.WARNING, "The student has already been allocated");
            return; // если уже выделена, ничего не делаем
        }
        Container parent = this.getParent();
        if (parent != null) {
            for (Component comp : parent.getComponents()) {
                if (comp instanceof NamePartPanel && comp != this) {
                    ((NamePartPanel) comp).clearSelection();
                }
            }
        }
        setSelected(true);

        // Находим MainGroupPanel через предка и обновляем таблицу ивентов
        MainGroupPanel mainGroup = (MainGroupPanel) SwingUtilities.getAncestorOfClass(MainGroupPanel.class, this);
        if(mainGroup != null && student != null) {
            mainGroup.getEventPanel().setSelectedStudent(student); // Сохраняем выбранного студента
            mainGroup.getEventPanel().getBtnAddEvent().setEnabled(true);
            mainGroup.getEventPanel().updateTableData(student.events);
        }
    }

    private void roundedPanelMouseEntered(MouseEvent e) {
        if (!isStudentSelected) {
            roundedPanel.setPanelColor(new Color(0x4D000000, true)); // hover
        }
    }

    private void roundedPanelMouseExited(MouseEvent e) {
        if (!isStudentSelected) {
            roundedPanel.setPanelColor(new Color(0x32000000, true)); // normal
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        lblNumOfDude = new JLabel();
        roundedPanel = new RoundedPanel();
        lblNameofDude = new JLabel();
        popMoreActionsMenu = new JPopupMenu();
        miRename = new JMenuItem();
        miChangeGroup = new JMenuItem();
        miDelete = new JMenuItem();

        //======== this ========
        setBorder(new EmptyBorder(5, 10, 5, 10));
        setPreferredSize(new Dimension(329, 55));
        setMinimumSize(new Dimension(329, 55));
        setBackground(new Color(0xff3333));
        setMaximumSize(new Dimension(2147483647, 55));
        setOpaque(false);
        setLayout(new BorderLayout(5, 0));

        //---- lblNumOfDude ----
        lblNumOfDude.setText("1");
        lblNumOfDude.setHorizontalAlignment(SwingConstants.CENTER);
        lblNumOfDude.setFont(lblNumOfDude.getFont().deriveFont(lblNumOfDude.getFont().getSize() + 4f));
        add(lblNumOfDude, BorderLayout.WEST);

        //======== roundedPanel ========
        {
            roundedPanel.setPanelColor(new Color(0x32000000, true));
            roundedPanel.setBorderColor(new Color(0x00000000, true));
            roundedPanel.setBorder(new EmptyBorder(0, 0, 0, 5));
            roundedPanel.setComponentPopupMenu(popMoreActionsMenu);
            roundedPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    roundedPanelMouseClicked(e);
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    roundedPanelMouseEntered(e);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    roundedPanelMouseExited(e);
                }
            });
            roundedPanel.setLayout(new BorderLayout());

            //---- lblNameofDude ----
            lblNameofDude.setText("\u0418\u0432\u0430\u043d\u043e\u0432 \u0418\u0432\u0430\u043d \u0418\u0432\u0430\u043d\u044b\u0447");
            lblNameofDude.setHorizontalAlignment(SwingConstants.CENTER);
            lblNameofDude.setFont(lblNameofDude.getFont().deriveFont(lblNameofDude.getFont().getSize() + 4f));
            roundedPanel.add(lblNameofDude, BorderLayout.CENTER);
        }
        add(roundedPanel, BorderLayout.CENTER);

        //======== popMoreActionsMenu ========
        {

            //---- miRename ----
            miRename.setText("Rename");
            miRename.setIcon(new FlatSVGIcon("Icons/edit_icon.svg"));
            miRename.addActionListener(e -> miRename(e));
            popMoreActionsMenu.add(miRename);
            popMoreActionsMenu.addSeparator();

            //---- miChangeGroup ----
            miChangeGroup.setText("Change group");
            miChangeGroup.setIcon(new FlatSVGIcon("Icons/arrow_icon_24x24.svg"));
            miChangeGroup.addActionListener(e -> miChangeGroup(e));
            popMoreActionsMenu.add(miChangeGroup);
            popMoreActionsMenu.addSeparator();

            //---- miDelete ----
            miDelete.setText("Delete");
            miDelete.setIcon(new FlatSVGIcon("Icons/bin_icon_24x24.svg"));
            miDelete.addActionListener(e -> miDelete(e));
            popMoreActionsMenu.add(miDelete);
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JLabel lblNumOfDude;
    private RoundedPanel roundedPanel;
    private JLabel lblNameofDude;
    private JPopupMenu popMoreActionsMenu;
    private JMenuItem miRename;
    private JMenuItem miChangeGroup;
    private JMenuItem miDelete;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}

