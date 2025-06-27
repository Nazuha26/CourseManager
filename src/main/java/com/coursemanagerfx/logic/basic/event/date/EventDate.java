package com.coursemanagerfx.logic.basic.event.date;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class EventDate {
    private int day;
    private int month;
    private int year;

    public EventDate(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public int getDay() {
        return day;
    }
    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }
    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }
    public void setYear(int year) {
        this.year = year;
    }

    public String toFormattedWithDaysFrom(EventDate from) {
        LocalDate thisDate = LocalDate.of(this.year, this.month, this.day);
        LocalDate fromDate = LocalDate.of(from.year, from.month, from.day);

        String formatted = thisDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        if (thisDate.isBefore(fromDate)) {
            return formatted + " (error)";
        }

        long days = ChronoUnit.DAYS.between(fromDate, thisDate);
        return formatted + " (" + days + " days)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventDate that = (EventDate) o;

        return day == that.day && month == that.month && year == that.year;
    }

    @Override
    public int hashCode()
        { return java.util.Objects.hash(day, month, year); }

    @Override
    public String toString() {
        return String.format("%02d.%02d.%04d", day, month, year);
    }
}
