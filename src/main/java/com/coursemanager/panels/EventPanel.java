/*
 * Created by JFormDesigner on Thu Mar 20 20:53:50 EET 2025
 */

package com.coursemanager.panels;

import java.util.*;

import com.coursemanager.CM_HELPER;
import com.coursemanager.other.Student;
import com.coursemanager.other.StudentEvent;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import com.coursemanager.ui.*;

/**
 * @author Nazar
 */
public class EventPanel extends JPanel {
    public EventPanel() {
        initComponents();
        initTableModel();
        initTableSorter();
    }

    private void initTableModel() {
        tableListStds.setModel(new DefaultTableModel(
                new Object[][] {},
                new String[] {
                        "<html><b>№<html>",
                        "<html><b>" + CM_HELPER.getBundle().getString("table.header.date") + "<html>",
                        "<html><b>" + CM_HELPER.getBundle().getString("table.header.event") + "<html>",
                        "<html><b>" + CM_HELPER.getBundle().getString("table.header.marks") + "<html>",
                        "<html><b>" + CM_HELPER.getBundle().getString("table.header.expired") + "<html>",
                        "<html><b>" + CM_HELPER.getBundle().getString("table.header.type") + "<html>"
                }
        ){
            Class<?>[] columnTypes = new Class<?>[] {
                    Short.class, String.class, String.class, Byte.class, String.class, String.class
            };
            boolean[] columnEditable = new boolean[] {
                    false, false, false, false, false, false
            };
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnEditable[columnIndex];
            }
        });
        {
            TableColumnModel cm = tableListStds.getColumnModel();
            cm.getColumn(0).setResizable(false);
            cm.getColumn(0).setMinWidth(40);
            cm.getColumn(0).setMaxWidth(40);
            cm.getColumn(0).setPreferredWidth(40);
            cm.getColumn(1).setResizable(false);
            cm.getColumn(1).setMinWidth(100);
            cm.getColumn(1).setMaxWidth(100);
            cm.getColumn(1).setPreferredWidth(100);
            cm.getColumn(2).setResizable(false);
            cm.getColumn(3).setResizable(false);
            cm.getColumn(3).setMinWidth(60);
            cm.getColumn(3).setMaxWidth(60);
            cm.getColumn(3).setPreferredWidth(60);
            cm.getColumn(4).setResizable(false);
            cm.getColumn(4).setMinWidth(140);
            cm.getColumn(4).setMaxWidth(140);
            cm.getColumn(4).setPreferredWidth(140);
            cm.getColumn(5).setResizable(false);
            cm.getColumn(5).setMinWidth(90);
            cm.getColumn(5).setMaxWidth(90);
            cm.getColumn(5).setPreferredWidth(90);
        }
    }

    private void initTableSorter() {
        // --- Добавляем кастомный сортировщик ---
        TableRowSorter<DefaultTableModel> sorter =
                new TableRowSorter<>((DefaultTableModel) tableListStds.getModel());

        sorter.setSortable(0, false);   // Запрещаем сортировку 1ой колонки (№)

        // --- Сортировка 2ой колонки (Дата создания) ---
        sorter.setComparator(1, (o1, o2) -> {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate d1 = LocalDate.parse(o1.toString(), fmt);
            LocalDate d2 = LocalDate.parse(o2.toString(), fmt);
            return d1.compareTo(d2);
        });

        // --- Сортировка 3ей колонки (Название ивента) ---
        sorter.setComparator(2, (o1, o2) ->
                o1.toString().compareToIgnoreCase(o2.toString())
        );

        // --- Сортировка 4ой колонки (Оценки) ---
        sorter.setComparator(3, (o1, o2) -> {
            Integer i1 = ((Number) o1).intValue();
            Integer i2 = ((Number) o2).intValue();
            return i1.compareTo(i2);
        });

        // --- Сортировка 5ой колонки (Даты окончания) ---
        sorter.setComparator(4, (o1, o2) ->
                Integer.compare(parseDays(o1.toString()), parseDays(o2.toString()))
        );

        // --- Сортировка 6ой колонки (Типа ивента: активный/завершенный) ---
        sorter.setComparator(5, (o1, o2) -> {
            String s1 = o1.toString().toLowerCase();
            String s2 = o2.toString().toLowerCase();
            if (s1.contains("active") && s2.contains("completed")) return -1;
            if (s1.contains("completed") && s2.contains("active")) return 1;
            return s1.compareTo(s2);
        });

        // Сортировка по умолчанию колонки Type (6) по возрастанию
        sorter.setSortKeys(List.of(new RowSorter.SortKey(5, SortOrder.ASCENDING)));

        tableListStds.setRowSorter(sorter);

        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 0) {
                    setText(String.valueOf(row + 1)); // номер строки
                } else {
                    setText(value != null ? value.toString() : ""); // обычное значение
                }

                setHorizontalAlignment(SwingConstants.CENTER); // выравнивание

                return this;
            }
        };

        // Устанавливаем центрирование для всех колонок, кроме "Event"
        TableColumnModel columnModel = tableListStds.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            if (i != 2) { // пропускаем колонку и колонку "Event"
                columnModel.getColumn(i).setCellRenderer(customRenderer);
            }
        }

        // Цветной рендер для колонки "Type"
        tableListStds.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String text = value.toString().toLowerCase();
                if (!isSelected) {
                    if (text.contains(CM_HELPER.getBundle().getString("table.type.active").toLowerCase())) {
                        setBackground(new Color(0xb4e2bd));
                    } else if (text.contains(CM_HELPER.getBundle().getString("table.type.completed").toLowerCase())) {
                        setBackground(new Color(0xe2b4b4));
                    } else {
                        setBackground(Color.WHITE);
                    }
                }

                setHorizontalAlignment(SwingConstants.CENTER);
                //setOpaque(true);
                //setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, table.getGridColor())); // <<< это решает проблему

                return this;
            }
        });
    }

    private int parseDays(String text) {
        int start = text.indexOf('(');
        int end = text.indexOf(')');
        if (start < 0 || end < 0 || start >= end) return 0;
        String inside = text.substring(start + 1, end).trim().toLowerCase();
        String[] parts = inside.split("\\s+", 2);
        if (parts.length < 2) return 0;

        int value = Integer.parseInt(parts[0]);
        char unit = parts[1].charAt(0);
        return switch (unit) {
            case 'd' -> value;
            case 'w' -> value * 7;
            case 'm' -> value * 30;
            default -> value;
        };
    }

    public void updateTableData(List<StudentEvent> events) {
        // Получаем модели для обеих таблиц и очищаем их
        DefaultTableModel modelActive = (DefaultTableModel) tableListStds.getModel();
        modelActive.setRowCount(0);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate today = LocalDate.now();

        int countEvents = 1;

        for (StudentEvent event : events) {
            // Определяем дату окончания без дополнительного текста (например, " (5 days)")
            String datePart = event.expiredDate.contains(" (")
                    ? event.expiredDate.substring(0, event.expiredDate.indexOf(" ("))
                    : event.expiredDate;
            try {
                LocalDate expiredDate = LocalDate.parse(datePart, formatter);
                event.setExpired(expiredDate.isBefore(today));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // Добавляем ивент в таблицу
            modelActive.addRow(new Object[]{
                    countEvents++,
                    event.creationDate,
                    event.eventDescription,
                    event.grade,
                    event.expiredDate,
                    event.isExpired() ?  "<html><b><font color='#b40202'>" + CM_HELPER.getBundle().getString("table.type.completed") + "</font></b></html>"
                            : "<html><b><font color='#1ab403'>" + CM_HELPER.getBundle().getString("table.type.active") + "</font></b></html>"
            });
        }
    }

    private Student selectedStudent;

    public Student getSelectedStudent() {
        return this.selectedStudent;
    }

    public void setSelectedStudent(Student student) {
        this.selectedStudent = student;
    }

    private void btnAddEvent(ActionEvent e) {
        boolean isVisible = createStdEventPanel.isVisible();
        createStdEventPanel.setVisible(!isVisible);
        btnPanel.setVisible(isVisible);
    }

    public JPanel getBtnPanel() {
        return btnPanel;
    }

    public JButton getBtnAddEvent() {
        return btnAddEvent;
    }

    public JTable getTableListStds() {
        return tableListStds;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        ResourceBundle bundle = ResourceBundle.getBundle("strings");
        panelTable = new JPanel();
        scrollPane = new JScrollPane();
        tableListStds = new JTable();
        createStdEventPanel = new CreateStdEventPanel(this);
        btnPanel = new JPanel();
        lblInfo = new JLabel();
        btnAddEvent = new JButton();

        //======== this ========
        setLayout(new BorderLayout(0, 5));

        //======== panelTable ========
        {
            panelTable.setLayout(new BorderLayout(0, 10));

            //======== scrollPane ========
            {
                scrollPane.setMinimumSize(null);

                //---- tableListStds ----
                tableListStds.setModel(new DefaultTableModel(
                    new Object[][] {
                    },
                    new String[] {
                        "<html><b>\u2116<html>", "<html><b>Date<html>", "<html><b>Event<html>", "<html><b>Marks<html>", "<html><b>Expired time<html>", "<html><b>Type<html>"
                    }
                ) {
                    Class<?>[] columnTypes = new Class<?>[] {
                        Short.class, String.class, String.class, Byte.class, String.class, String.class
                    };
                    boolean[] columnEditable = new boolean[] {
                        false, false, false, false, false, false
                    };
                    @Override
                    public Class<?> getColumnClass(int columnIndex) {
                        return columnTypes[columnIndex];
                    }
                    @Override
                    public boolean isCellEditable(int rowIndex, int columnIndex) {
                        return columnEditable[columnIndex];
                    }
                });
                {
                    TableColumnModel cm = tableListStds.getColumnModel();
                    cm.getColumn(0).setResizable(false);
                    cm.getColumn(0).setMinWidth(40);
                    cm.getColumn(0).setMaxWidth(40);
                    cm.getColumn(0).setPreferredWidth(40);
                    cm.getColumn(1).setResizable(false);
                    cm.getColumn(1).setMinWidth(100);
                    cm.getColumn(1).setMaxWidth(100);
                    cm.getColumn(1).setPreferredWidth(100);
                    cm.getColumn(2).setResizable(false);
                    cm.getColumn(3).setResizable(false);
                    cm.getColumn(3).setMinWidth(60);
                    cm.getColumn(3).setMaxWidth(60);
                    cm.getColumn(3).setPreferredWidth(60);
                    cm.getColumn(4).setResizable(false);
                    cm.getColumn(4).setMinWidth(140);
                    cm.getColumn(4).setMaxWidth(140);
                    cm.getColumn(4).setPreferredWidth(140);
                    cm.getColumn(5).setResizable(false);
                    cm.getColumn(5).setMinWidth(90);
                    cm.getColumn(5).setMaxWidth(90);
                    cm.getColumn(5).setPreferredWidth(90);
                }
                tableListStds.setShowVerticalLines(true);
                tableListStds.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
                tableListStds.setShowHorizontalLines(true);
                tableListStds.setRowSelectionAllowed(false);
                tableListStds.getTableHeader().setReorderingAllowed(false);
                scrollPane.setViewportView(tableListStds);
            }
            panelTable.add(scrollPane, BorderLayout.CENTER);

            //---- createStdEventPanel ----
            createStdEventPanel.setVisible(false);
            panelTable.add(createStdEventPanel, BorderLayout.SOUTH);
        }
        add(panelTable, BorderLayout.CENTER);

        //======== btnPanel ========
        {
            btnPanel.setPreferredSize(new Dimension(97, 30));
            btnPanel.setMinimumSize(new Dimension(177, 30));
            btnPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
            btnPanel.setLayout(new GridBagLayout());
            ((GridBagLayout)btnPanel.getLayout()).rowHeights = new int[] {0, 0};
            ((GridBagLayout)btnPanel.getLayout()).columnWeights = new double[] {1.0, 0.0};
            ((GridBagLayout)btnPanel.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

            //---- lblInfo ----
            lblInfo.setText(bundle.getString("EventPanel.lblInfo.text"));
            lblInfo.setFont(lblInfo.getFont().deriveFont(lblInfo.getFont().getSize() + 3f));
            btnPanel.add(lblInfo, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 5), 0, 0));

            //---- btnAddEvent ----
            btnAddEvent.setText(bundle.getString("EventPanel.btnAddEvent.text"));
            btnAddEvent.setPreferredSize(new Dimension(200, 30));
            btnAddEvent.setMinimumSize(new Dimension(200, 30));
            btnAddEvent.setMaximumSize(new Dimension(200, 30));
            btnAddEvent.setFont(btnAddEvent.getFont().deriveFont(btnAddEvent.getFont().getSize() + 2f));
            btnAddEvent.setEnabled(false);
            btnAddEvent.addActionListener(e -> btnAddEvent(e));
            btnPanel.add(btnAddEvent, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 0, 0), 0, 0));
        }
        add(btnPanel, BorderLayout.SOUTH);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JPanel panelTable;
    private JScrollPane scrollPane;
    private JTable tableListStds;
    private CreateStdEventPanel createStdEventPanel;
    private JPanel btnPanel;
    private JLabel lblInfo;
    private JButton btnAddEvent;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
