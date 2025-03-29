/*
 * Created by JFormDesigner on Thu Mar 20 12:11:04 EET 2025
 */

package com.coursemanager.panels;

import java.util.*;
import com.coursemanager.Launcher;
import com.coursemanager.other.EVENT_MODE_LIST_Class;
import com.coursemanager.other.Student;
import com.coursemanager.other.StudentEvent;
import com.coursemanager.windows.MainCoursesWindow;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

import com.toedter.calendar.*;

/**
 * @author Nazar
 */
public class CreateStdEventPanel extends JPanel {
    private EventPanel eventPanel;

    public CreateStdEventPanel() {
        initComponents();
        initComboBox();
    }

    public CreateStdEventPanel(EventPanel eventPanel) {
        this.eventPanel = eventPanel;
        initComponents();
        initComboBox();
    }

    private void btnCancelEvent(ActionEvent e) {
        this.setVisible(false);
        eventPanel.getBtnPanel().setVisible(true);
    }

    @SuppressWarnings("unchecked")
    private void initComboBox() {
        DefaultComboBoxModel<EVENT_MODE_LIST_Class.EventMode> model = new DefaultComboBoxModel<>();
        for (EVENT_MODE_LIST_Class.EventMode mode : EVENT_MODE_LIST_Class.getEventModeList().values()) {
            model.addElement(mode);
        }
        comboModeBox.setModel(model);

        // Добавляем слушатель выбора
        comboModeBox.addActionListener(e -> {
            EVENT_MODE_LIST_Class.EventMode selectedMode =
                    (EVENT_MODE_LIST_Class.EventMode) comboModeBox.getSelectedItem();
            customEventPanel.setVisible(selectedMode != null && "Інший івент".equals(selectedMode.getName()));
        });
    }

    private void btnCreateEvent(ActionEvent e) {
        if (textPane.getText().isEmpty()){
            JOptionPane.showMessageDialog(eventPanel, "Please enter the event name!");
            return;
        }

        // Получаем дату создания из календаря и преобразуем в LocalDate
        java.util.Date selectedDate = jcalendar.getDate();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy");
        java.time.LocalDate creationDateLocal = selectedDate.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();

        // Преобразуем в строку для сохранения
        String creationDate = creationDateLocal.format(formatter);

        // Берём описание события из textPane
        //String eventDescription = textPane.getText().trim();
        String eventDescription = textPane.getText().replaceAll("\\s*\\n\\s*", " ").replaceAll("\\s*\\r\\s*", " ").trim();

        // Получаем выбранный режим и его данные
        EVENT_MODE_LIST_Class.EventMode selectedMode =
                (EVENT_MODE_LIST_Class.EventMode) comboModeBox.getSelectedItem();

        int grade;
        String expiredTime;

        if (selectedMode != null && "Інший івент".equals(selectedMode.getName())) {
            int customGrade;
            int customAmount;
            customGrade = (int) spinMark.getValue();
            customAmount = (int) spinExpTime.getValue();

            grade = customGrade;

            // Определяем единицу измерения по выбору в cmbboxTypeExpTime
            String unitSelected = (String) cmbboxTypeExpTime.getSelectedItem();
            String unit;
            switch (unitSelected) {
                case "міс." -> unit = "m";
                case "нед." -> unit = "w";
                case "день" -> unit = "d";
                case null, default -> {
                    JOptionPane.showMessageDialog(this, "Неверный тип для expired time!");
                    return;
                }
            }

            // Расчет даты истечения для кастомного ивента
            java.time.LocalDate expiredDateLocal = switch (unit) {
                case "d" -> creationDateLocal.plusDays(customAmount);
                case "w" -> creationDateLocal.plusWeeks(customAmount);
                case "m" -> creationDateLocal.plusMonths(customAmount);
                default -> throw new IllegalArgumentException("Неверный формат срока");
            };

            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(creationDateLocal, expiredDateLocal);
            expiredTime = expiredDateLocal.format(formatter) + " (" + daysBetween + " days)";
        } else {
            // Для обычного режима
            grade = selectedMode.getRating();
            String[] parts = selectedMode.getExpiredTime().split(" %");
            int amount = Integer.parseInt(parts[0].trim());
            String unit = parts[1].trim();

            java.time.LocalDate expiredDateLocal = switch (unit) {
                case "d" -> creationDateLocal.plusDays(amount);
                case "w" -> creationDateLocal.plusWeeks(amount);
                case "m" -> creationDateLocal.plusMonths(amount);
                default -> throw new IllegalArgumentException("Неверный формат срока: " + selectedMode.getExpiredTime());
            };

            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(creationDateLocal, expiredDateLocal);
            expiredTime = expiredDateLocal.format(formatter) + " (" + daysBetween + " days)";
        }

        // Получаем выбранного студента из EventPanel
        Student selectedStudent = eventPanel.getSelectedStudent();
        if (selectedStudent == null) {
            JOptionPane.showMessageDialog(this, "Сначала выберите студента!");
            return;
        }

        // Создаём новый ивент
        StudentEvent newEvent = new StudentEvent(creationDate, eventDescription, grade, expiredTime);

        // Добавляем ивент в список выбранного студента
        selectedStudent.events.add(newEvent);

        // Обновляем таблицу событий для выбранного студента
        eventPanel.updateTableData(selectedStudent.events);

        // Скрываем панель создания и возвращаем панель кнопок
        this.setVisible(false);
        eventPanel.getBtnPanel().setVisible(true);
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        ResourceBundle bundle = ResourceBundle.getBundle("strings");
        panel1 = new JPanel();
        jcalendar = new JCalendar();
        panel2 = new JPanel();
        lblInfo1 = new JLabel();
        customEventPanel = new JPanel();
        lblMark = new JLabel();
        lblExpTime = new JLabel();
        spinMark = new JSpinner();
        panel6 = new JPanel();
        spinExpTime = new JSpinner();
        cmbboxTypeExpTime = new JComboBox<>();
        btnCreateEvent = new JButton();
        btnCancelEvent = new JButton();
        panel3 = new JPanel();
        panel4 = new JPanel();
        panel5 = new JPanel();
        lblInfo2 = new JLabel();
        scrollPane = new JScrollPane();
        textPane = new JTextPane();
        modePanel = new JPanel();
        comboModeBox = new JComboBox();
        lblInfo3 = new JLabel();

        //======== this ========
        setBorder(new CompoundBorder(
            new BevelBorder(BevelBorder.LOWERED),
            new EmptyBorder(10, 10, 0, 10)));
        setLayout(new BorderLayout(5, 5));

        //======== panel1 ========
        {
            panel1.setLayout(new GridBagLayout());
            ((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0};
            ((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
            ((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
            ((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 0.0, 0.0, 1.0E-4};

            //---- jcalendar ----
            jcalendar.setPreferredSize(new Dimension(200, 200));
            jcalendar.setMinimumSize(new Dimension(200, 200));
            jcalendar.setMaximumSize(new Dimension(200, 200));
            jcalendar.setToolTipText(bundle.getString("CreateStdEventPanel.jcalendar.toolTipText"));
            jcalendar.setDecorationBackgroundVisible(false);
            panel1.add(jcalendar, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //======== panel2 ========
            {
                panel2.setLayout(new GridBagLayout());
                ((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0};
                ((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0};
                ((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
                ((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

                //---- lblInfo1 ----
                lblInfo1.setText(bundle.getString("CreateStdEventPanel.lblInfo1.text"));
                panel2.add(lblInfo1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            panel1.add(panel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //======== customEventPanel ========
            {
                customEventPanel.setVisible(false);
                customEventPanel.setLayout(new GridBagLayout());
                ((GridBagLayout)customEventPanel.getLayout()).columnWidths = new int[] {0, 0, 0};
                ((GridBagLayout)customEventPanel.getLayout()).rowHeights = new int[] {0, 0, 0};
                ((GridBagLayout)customEventPanel.getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0E-4};
                ((GridBagLayout)customEventPanel.getLayout()).rowWeights = new double[] {1.0, 1.0, 1.0E-4};

                //---- lblMark ----
                lblMark.setText(bundle.getString("CreateStdEventPanel.lblMark.text"));
                customEventPanel.add(lblMark, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- lblExpTime ----
                lblExpTime.setText(bundle.getString("CreateStdEventPanel.lblExpTime.text"));
                customEventPanel.add(lblExpTime, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));

                //---- spinMark ----
                spinMark.setModel(new SpinnerNumberModel(1, 1, 5, 1));
                customEventPanel.add(spinMark, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //======== panel6 ========
                {
                    panel6.setLayout(new GridBagLayout());
                    ((GridBagLayout)panel6.getLayout()).columnWidths = new int[] {0, 0, 0};
                    ((GridBagLayout)panel6.getLayout()).rowHeights = new int[] {0, 0};
                    ((GridBagLayout)panel6.getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0E-4};
                    ((GridBagLayout)panel6.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

                    //---- spinExpTime ----
                    spinExpTime.setModel(new SpinnerNumberModel(1, 1, 1000, 1));
                    panel6.add(spinExpTime, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));

                    //---- cmbboxTypeExpTime ----
                    cmbboxTypeExpTime.setModel(new DefaultComboBoxModel<>(new String[] {
                        "\u043c\u0456\u0441.",
                        "\u043d\u0435\u0434.",
                        "\u0434\u0435\u043d\u044c"
                    }));
                    panel6.add(cmbboxTypeExpTime, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
                }
                customEventPanel.add(panel6, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            panel1.add(customEventPanel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- btnCreateEvent ----
            btnCreateEvent.setText(bundle.getString("CreateStdEventPanel.btnCreateEvent.text"));
            btnCreateEvent.setPreferredSize(new Dimension(72, 30));
            btnCreateEvent.setMinimumSize(new Dimension(72, 30));
            btnCreateEvent.setMaximumSize(new Dimension(72, 30));
            btnCreateEvent.setFont(btnCreateEvent.getFont().deriveFont(btnCreateEvent.getFont().getSize() + 2f));
            btnCreateEvent.addActionListener(e -> btnCreateEvent(e));
            panel1.add(btnCreateEvent, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- btnCancelEvent ----
            btnCancelEvent.setText(bundle.getString("CreateStdEventPanel.btnCancelEvent.text"));
            btnCancelEvent.setPreferredSize(new Dimension(200, 30));
            btnCancelEvent.setMinimumSize(new Dimension(200, 30));
            btnCancelEvent.setMaximumSize(new Dimension(200, 30));
            btnCancelEvent.setFont(btnCancelEvent.getFont().deriveFont(btnCancelEvent.getFont().getSize() + 2f));
            btnCancelEvent.setBackground(new Color(0xd30000));
            btnCancelEvent.setForeground(Color.white);
            btnCancelEvent.setBorderPainted(false);
            btnCancelEvent.addActionListener(e -> btnCancelEvent(e));
            panel1.add(btnCancelEvent, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        }
        add(panel1, BorderLayout.EAST);

        //======== panel3 ========
        {
            panel3.setLayout(new BorderLayout(5, 5));

            //======== panel4 ========
            {
                panel4.setLayout(new BorderLayout(5, 5));

                //======== panel5 ========
                {
                    panel5.setLayout(new BorderLayout());

                    //---- lblInfo2 ----
                    lblInfo2.setText(bundle.getString("CreateStdEventPanel.lblInfo2.text"));
                    panel5.add(lblInfo2, BorderLayout.NORTH);

                    //======== scrollPane ========
                    {

                        //---- textPane ----
                        textPane.setFont(textPane.getFont().deriveFont(textPane.getFont().getSize() + 3f));
                        textPane.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                        scrollPane.setViewportView(textPane);
                    }
                    panel5.add(scrollPane, BorderLayout.CENTER);
                }
                panel4.add(panel5, BorderLayout.CENTER);

                //======== modePanel ========
                {
                    modePanel.setPreferredSize(new Dimension(114, 60));
                    modePanel.setMinimumSize(new Dimension(114, 60));
                    modePanel.setLayout(new BorderLayout());

                    //---- comboModeBox ----
                    comboModeBox.setPreferredSize(new Dimension(72, 30));
                    comboModeBox.setMinimumSize(new Dimension(72, 30));
                    modePanel.add(comboModeBox, BorderLayout.CENTER);

                    //---- lblInfo3 ----
                    lblInfo3.setText(bundle.getString("CreateStdEventPanel.lblInfo3.text"));
                    modePanel.add(lblInfo3, BorderLayout.NORTH);
                }
                panel4.add(modePanel, BorderLayout.PAGE_END);
            }
            panel3.add(panel4, BorderLayout.CENTER);
        }
        add(panel3, BorderLayout.CENTER);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JPanel panel1;
    private JCalendar jcalendar;
    private JPanel panel2;
    private JLabel lblInfo1;
    private JPanel customEventPanel;
    private JLabel lblMark;
    private JLabel lblExpTime;
    private JSpinner spinMark;
    private JPanel panel6;
    private JSpinner spinExpTime;
    private JComboBox<String> cmbboxTypeExpTime;
    private JButton btnCreateEvent;
    private JButton btnCancelEvent;
    private JPanel panel3;
    private JPanel panel4;
    private JPanel panel5;
    private JLabel lblInfo2;
    private JScrollPane scrollPane;
    private JTextPane textPane;
    private JPanel modePanel;
    private JComboBox comboModeBox;
    private JLabel lblInfo3;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
