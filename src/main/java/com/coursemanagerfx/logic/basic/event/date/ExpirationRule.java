/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic.basic.event.date;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class ExpirationRule {
    private final int count;
    private final DateType unit;  // DAY, WEEK, MONTH, YEAR

    public int getCount() {
        return count;
    }
    public DateType getUnit() {
        return unit;
    }

    public ExpirationRule(int count, DateType unit) {
        this.count = count;
        this.unit = unit;
    }

    // Возвращает новую дату, сдвинутую на count unit от base
    public LocalDate apply(LocalDate base) {
        return switch (unit) {
            case DAY   -> base.plusDays(count);
            case WEEK  -> base.plusDays(7L * count);
            case MONTH -> base.plusMonths(count);
            case YEAR  -> base.plusYears(count);
        };
    }

    // Для красивого вывода: "dd.MM.yyyy (N days)"
    public String format(LocalDate base) {
        LocalDate exp = apply(base);
        long daysBetween = ChronoUnit.DAYS.between(base, exp);
        return exp.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) +
                " (" + daysBetween + " days)";
    }
}
