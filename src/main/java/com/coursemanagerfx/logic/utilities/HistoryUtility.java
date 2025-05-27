package com.coursemanagerfx.logic.utilities;

import javafx.scene.control.Label;
import org.fxmisc.richtext.InlineCssTextArea;
import javafx.scene.paint.Color;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class HistoryUtility {

    public enum Types { SUCCESS, WARNING, ERROR, INFO }

    /**
     * Добавляет в InlineCssTextArea строку истории формата:
     * [TYPE] historyText [HH:mm:ss]
     * При этом:
     * - префикс (например, [SUCCESS]) оформляется цветом и жирным шрифтом,
     * - основное сообщение — белым,
     * - время — серым.
     *
     * @param area        экземпляр InlineCssTextArea
     * @param type        тип сообщения (SUCCESS, WARNING, ERROR, INFO)
     * @param historyText текст сообщения
     */
    public static void setHistory(InlineCssTextArea area, Label curHistoryLbl, Types type, String historyText) {
        // Получаем текущее время
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        // Собираем строку: [TYPE] message [HH:mm:ss]\n
        String prefix = "[" + type.name() + "] ";
        String suffix = " [" + time + "]\n";
        String fullMessage = prefix + historyText + suffix;

        // Запоминаем позицию, куда будем вставлять текст
        int start = area.getLength();
        area.appendText(fullMessage);
        int end = area.getLength();

        // Определяем цвет для префикса в зависимости от типа
        Color color = switch (type) {
            case SUCCESS -> Color.rgb(7, 213, 0);
            case WARNING -> Color.rgb(213, 202, 0);
            case ERROR   -> Color.rgb(213, 0, 0);
            case INFO    -> Color.rgb(0, 149, 213);
        };

        // Собираем CSS-строки для каждого сегмента
        String prefixStyle = String.format("-fx-fill: %s; -fx-font-weight: bold;", toWebColor(color));
        String mainStyle = "-fx-fill: white;";
        String suffixStyle = "-fx-fill: gray;";

        // Применяем стили к соответствующим диапазонам
        area.setStyle(start, start + prefix.length(), prefixStyle);
        area.setStyle(start + prefix.length(), start + prefix.length() + historyText.length(), mainStyle);
        area.setStyle(start + prefix.length() + historyText.length(), end, suffixStyle);

        // В label выводим только [TYPE] historyText
        String labelText = "[" + type.name() + "] " + historyText;
        curHistoryLbl.setText(labelText);

        curHistoryLbl.setTextFill(color);
    }

    // Вспомогательный метод для преобразования Color в строку вида "#RRGGBB"
    private static String toWebColor(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}