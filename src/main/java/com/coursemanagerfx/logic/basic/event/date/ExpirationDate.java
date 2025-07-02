/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic.basic.event.date;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ExpirationDate extends EventDate {
    private final EventDate creationDate;

    public ExpirationDate(int day, int month, int year, EventDate creationDate) {
        super(day, month, year);
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        LocalDate base = LocalDate.of(
                creationDate.getYear(), creationDate.getMonth(), creationDate.getDay()
        );
        LocalDate exp = LocalDate.of(
                getYear(), getMonth(), getDay()
        );
        if (exp.isBefore(base)) {
            return super.toString() + " (error)";
        }
        long days = ChronoUnit.DAYS.between(base, exp);
        return super.toString() + " (" + days + " days)";
    }
}
