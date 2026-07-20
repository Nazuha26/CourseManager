package com.coursemanagerfx.logic.history;

import com.coursemanagerfx.controllers.main.Main_controller;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import org.fxmisc.richtext.InlineCssTextArea;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Supplier;

/** Writes user-visible actions to the history panel in the main window. */
public final class HistoryManager {

    public enum HistoryType { SUCCESS, WARNING, ERROR, INFO }

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int SHORT_MESSAGE_LENGTH = 40;

    private final Supplier<Main_controller> controllerSupplier;

    public HistoryManager(Supplier<Main_controller> controllerSupplier) {
        this.controllerSupplier = Objects.requireNonNull(
                controllerSupplier,
                "controllerSupplier");
    }

    public void add(HistoryType type, String historyText) {
        Main_controller controller = controllerSupplier.get();
        if (controller == null) return;

        InlineCssTextArea area = controller.getHistoryTxtArea();
        Label currentHistory = controller.getLblCurHistory();
        String prefix = "[" + type.name() + "] ";
        String suffix = " [" + LocalTime.now().format(TIME_FORMAT) + "]\n";
        String fullMessage = prefix + historyText + suffix;

        int start = area.getLength();
        area.appendText(fullMessage);
        int end = area.getLength();

        Color color = switch (type) {
            case SUCCESS -> Color.rgb(7, 213, 0);
            case WARNING -> Color.rgb(213, 202, 0);
            case ERROR -> Color.rgb(213, 0, 0);
            case INFO -> Color.rgb(0, 149, 213);
        };

        String prefixStyle = String.format(
                "-fx-fill: %s; -fx-font-weight: bold;",
                toWebColor(color));
        area.setStyle(start, start + prefix.length(), prefixStyle);
        area.setStyle(
                start + prefix.length(),
                start + prefix.length() + historyText.length(),
                "-fx-fill: #dddddd;");
        area.setStyle(
                start + prefix.length() + historyText.length(),
                end,
                "-fx-fill: gray;");

        String shortText = historyText.length() > SHORT_MESSAGE_LENGTH
                ? historyText.substring(0, SHORT_MESSAGE_LENGTH) + "..."
                : historyText;
        currentHistory.setText("[" + type.name() + "] " + shortText);
        currentHistory.setTextFill(color);
    }

    private static String toWebColor(Color color) {
        return String.format(
                "#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}
