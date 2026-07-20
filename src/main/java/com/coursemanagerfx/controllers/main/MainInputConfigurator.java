package com.coursemanagerfx.controllers.main;

import com.coursemanagerfx.logic.basic.event.category.EventCategories;
import com.coursemanagerfx.logic.basic.event.date.ExpDateStrings;
import javafx.collections.FXCollections;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.text.DecimalFormatSymbols;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/** Configures date fields, event selectors and editable numeric spinners. */
final class MainInputConfigurator {

    private MainInputConfigurator() {
    }

    static void configure(Main_controller controller) {
        configureDatePicker(controller.getDtpkCreationDate());
        configureDatePicker(controller.getDtpkExpirationDate());

        controller.getComBoxExpTimeType().setItems(FXCollections.observableArrayList(
                ExpDateStrings.DAYS,
                ExpDateStrings.WEEKS,
                ExpDateStrings.MONTHS));
        controller.getComBoxExpTimeType().getSelectionModel().selectFirst();

        controller.getComBoxEventCategory().setVisibleRowCount(15);
        for (EventCategories type : EventCategories.values()) {
            controller.getComBoxEventCategory().getItems().add(type.getDisplayName());
        }
        controller.getComBoxEventCategory().getSelectionModel().selectFirst();

        configureExpirationSpinner(controller.getSpinnerExpTimeCount());
        configureMarkSpinner(controller.getSpinnerMark());
    }

    private static void configureExpirationSpinner(Spinner<Integer> spinner) {
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1, 1);
        spinner.setValueFactory(valueFactory);

        UnaryOperator<TextFormatter.Change> integerFilter = change ->
                change.getControlNewText().matches("\\d*") ? change : null;
        TextFormatter<Integer> formatter = new TextFormatter<>(
                new IntegerStringConverter(),
                valueFactory.getValue(),
                integerFilter);
        spinner.getEditor().setTextFormatter(formatter);

        spinner.focusedProperty().addListener((observable, oldValue, focused) -> {
            if (!focused) spinner.increment(0);
        });
        formatter.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) formatter.setValue(valueFactory.getMin());
        });
    }

    private static void configureMarkSpinner(Spinner<Double> spinner) {
        Locale locale = Locale.getDefault();
        char separator = new DecimalFormatSymbols(locale).getDecimalSeparator();
        SpinnerValueFactory.DoubleSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(-999, 999, 1.0, 0.5);
        spinner.setValueFactory(valueFactory);
        spinner.setEditable(true);

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getControlNewText();
            String separatorPattern = Pattern.quote(String.valueOf(separator));
            return text.matches("-?\\d{0,3}(" + separatorPattern + "[05]?)?")
                    || text.isEmpty() ? change : null;
        };

        StringConverter<Double> converter = new StringConverter<>() {
            @Override
            public String toString(Double value) {
                return value == null ? "" : String.format(locale, "%.1f", value);
            }

            @Override
            public Double fromString(String text) {
                if (text == null || text.isBlank() || "-".equals(text)
                        || String.valueOf(separator).equals(text)) {
                    return null;
                }
                try {
                    return Double.parseDouble(text.replace(separator, '.'));
                } catch (NumberFormatException exception) {
                    return null;
                }
            }
        };

        TextFormatter<Double> formatter = new TextFormatter<>(
                converter,
                valueFactory.getValue(),
                filter);
        spinner.getEditor().setTextFormatter(formatter);

        formatter.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(valueFactory.getValue())) {
                valueFactory.setValue(newValue);
            }
        });
        valueFactory.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(formatter.getValue())) {
                formatter.setValue(newValue);
            }
        });
        spinner.focusedProperty().addListener((observable, oldValue, focused) -> {
            if (!focused) {
                if (formatter.getValue() == null) formatter.setValue(1.0);
                spinner.increment(0);
            }
        });
    }

    private static void configureDatePicker(DatePicker picker) {
        picker.setDayCellFactory(ignored -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("odd-month", "even-month", "sunday", "today");
                if (empty || item == null) return;
                if (item.getDayOfWeek() == DayOfWeek.SUNDAY) getStyleClass().add("sunday");
                if (item.equals(LocalDate.now())) getStyleClass().add("today");
                getStyleClass().add(item.getMonthValue() % 2 == 1 ? "odd-month" : "even-month");
            }
        });

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        picker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : formatter.format(date);
            }

            @Override
            public LocalDate fromString(String text) {
                return text == null || text.isBlank()
                        ? null
                        : LocalDate.parse(text, formatter);
            }
        });
    }
}
