package com.coursemanager.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CMTablePane extends JTable implements Serializable {

    private static final long serialVersionUID = 1L;

    // Пустой (дефолтный) конструктор для JavaBean
    public CMTablePane() {
        // Инициализируем таблицу с дефолтной моделью и колонками по умолчанию
        this(new ColoredTableModel(new Object[]{"№", "Дата", "Описание", "Оценка", "Истечение"}, 0));
    }

    // Конструктор, принимающий нашу кастомную модель
    public CMTablePane(ColoredTableModel model) {
        super(model);
        initialize();
    }

    // Инициализация таблицы – установка кастомного рендерера
    private void initialize() {
        setDefaultRenderer(Object.class, createCellRenderer());
    }

    // Создание рендерера, который подставляет для каждой строки цвета, заданные в модели
    private DefaultTableCellRenderer createCellRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                ColoredTableModel model = (ColoredTableModel) table.getModel();
                if (!isSelected) {
                    c.setBackground(model.getRowBackground(row));
                    c.setForeground(model.getRowForeground(row));
                }
                return c;
            }
        };
    }

    /**
     * Переопределяем updateUI, чтобы при смене LookAndFeel наш рендерер снова устанавливался.
     */
    @Override
    public void updateUI() {
        super.updateUI(); // обязательно!
        // Повторно устанавливаем кастомный рендерер после обновления UI
        setDefaultRenderer(Object.class, createCellRenderer());
    }

    /**
     * Удобный метод для добавления строки с указанием индивидуальных цветов.
     *
     * @param rowData данные строки
     * @param bg      цвет фона строки
     * @param fg      цвет текста строки
     */
    public void addRow(Object[] rowData, Color bg, Color fg) {
        ColoredTableModel model = (ColoredTableModel) getModel();
        model.addRow(rowData, bg, fg);
    }

    /**
     * Возвращает текущую модель таблицы в виде ColoredTableModel.
     *
     * @return ColoredTableModel модели таблицы
     */
    public ColoredTableModel getColoredModel() {
        return (ColoredTableModel) getModel();
    }

    /**
     * Кастомная модель таблицы, расширяющая DefaultTableModel, которая хранит цвета для каждой строки.
     */
    public static class ColoredTableModel extends DefaultTableModel {
        private final List<Color> rowBackgrounds = new ArrayList<>();
        private final List<Color> rowForegrounds = new ArrayList<>();

        // Пустой конструктор для JavaBean
        public ColoredTableModel() {
            super();
        }

        public ColoredTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        /**
         * Добавляет строку с заданными цветами.
         *
         * @param rowData данные строки
         * @param bg      цвет фона
         * @param fg      цвет текста (foreground)
         */
        public void addRow(Object[] rowData, Color bg, Color fg) {
            super.addRow(rowData);
            rowBackgrounds.add(bg);
            rowForegrounds.add(fg);
        }

        /**
         * Возвращает цвет фона для строки с индексом row.
         *
         * @param row номер строки
         * @return цвет фона или Color.WHITE, если не задан
         */
        public Color getRowBackground(int row) {
            if (row < rowBackgrounds.size()) {
                return rowBackgrounds.get(row);
            }
            return Color.WHITE;
        }

        /**
         * Возвращает цвет текста для строки с индексом row.
         *
         * @param row номер строки
         * @return цвет текста или Color.BLACK, если не задан
         */
        public Color getRowForeground(int row) {
            if (row < rowForegrounds.size()) {
                return rowForegrounds.get(row);
            }
            return Color.BLACK;
        }

        @Override
        public void removeRow(int row) {
            super.removeRow(row);
            if (row < rowBackgrounds.size()) {
                rowBackgrounds.remove(row);
            }
            if (row < rowForegrounds.size()) {
                rowForegrounds.remove(row);
            }
        }
    }
}
