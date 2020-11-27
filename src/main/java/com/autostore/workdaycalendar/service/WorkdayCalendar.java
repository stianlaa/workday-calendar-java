package com.autostore.workdaycalendar.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;

/*
TODO split out preprocess/presearch stage
TODO pull out isIncrementing as variable
TODO improve findNextWeekday, with isIncrementing
TODO solve initialAdjustment better
TODO find better fix for initialadjustment

TODO fix issue with rounding
TODO create basic human input solution, and finish README.md,
TODO write more tests for negative cases
 */

public class WorkdayCalendar {

    private static final Integer RECURRING_YEAR = 2000;
    private SortedSet<LocalDate> specificHolidays = new TreeSet<>();
    private SortedSet<LocalDate> recurringHolidays = new TreeSet<>();
    private LocalTime workdayStart, workdayStop;

    public LocalDateTime getWorkdayIncrement(LocalDateTime startDateTime, float incrementInWorkdays) {
        long wholeWorkdays = roundTowardsZero(incrementInWorkdays);

        LocalDate fromDate = findFirstWeekdayFrom(startDateTime, incrementInWorkdays > 0);
        LocalDate toDate = findFirstWeekdayFrom(startDateTime.plusDays(wholeWorkdays), incrementInWorkdays > 0);

        long initialAdjustment = ChronoUnit.DAYS.between(fromDate, toDate) - wholeWorkdays;

        while (!fromDate.equals(toDate)) {
            long unaccountedDays = nonWorkdaysBetween(fromDate, toDate) - initialAdjustment;

            fromDate = toDate;
            toDate = (incrementInWorkdays > 0) ? fromDate.plusDays(unaccountedDays) : fromDate.minusDays(unaccountedDays);
            initialAdjustment = 0;
        }

        float workdayRemainder = incrementInWorkdays - wholeWorkdays;
        return accountForRemainder(toDate, workdayRemainder);
    }

    private LocalDate findFirstWeekdayFrom(LocalDateTime localDateTime, boolean positive) {
        LocalDate localDate = localDateTime.toLocalDate();
        localDate = localDateTime.toLocalTime().isAfter(workdayStop) ? localDate.plusDays(1) : localDate;
        if (isWeekendDate(localDate)) {
            if (positive) {
                return localDate.plusDays(localDate.getDayOfWeek().equals(SATURDAY) ? 2 : 1);
            } else {
                return localDate.minusDays(localDate.getDayOfWeek().equals(SATURDAY) ? 1 : 2);
            }
        }
        return localDate;
    }

    private LocalDateTime accountForRemainder(LocalDate fromDate, double workdayRemainder) {
        long workdayRemainderNanos = (long) (ChronoUnit.NANOS.between(workdayStart, workdayStop) * workdayRemainder);
        if (workdayRemainder >= 0) {
            return fromDate.atTime(workdayStart.plusNanos(workdayRemainderNanos));
        }
        return fromDate.atTime(workdayStop.plusNanos(workdayRemainderNanos));
    }

    private long nonWorkdaysBetween(LocalDate dateA, LocalDate dateB) {
        LocalDate start = dateA.isBefore(dateB) ? dateA : dateB;
        LocalDate end = dateA.isBefore(dateB) ? dateB : dateA;

        return weekdaysBetween(start, end) + specificHolidaysBetween(start, end) + recurringHolidaysBetween(start, end);
    }

    private long weekdaysBetween(LocalDate start, LocalDate end) {
        long entireWeeksBetween = ChronoUnit.WEEKS.between(start, end);
        long weekdaysCount = entireWeeksBetween * 2;
        LocalDate remainderEnd = end.minusWeeks(entireWeeksBetween);
        for (LocalDate date = start; date.isBefore(remainderEnd); date = date.plusDays(1)) {
            if (isWeekendDate(date)) {
                weekdaysCount++;
            }
        }
        return weekdaysCount;
    }

    private long specificHolidaysBetween(LocalDate start, LocalDate end) {
        return specificHolidays.subSet(start, end).stream()
                .filter(file -> !isWeekendDate(file))
                .count();
    }

    private long recurringHolidaysBetween(LocalDate start, LocalDate end) {
        long entireYearsBetween = ChronoUnit.YEARS.between(start.withYear(RECURRING_YEAR), end.withYear(RECURRING_YEAR));
        long recurringHolidaysCount = entireYearsBetween * recurringHolidays.size();
        LocalDate remainderEnd = end.minusYears(entireYearsBetween);
        for (LocalDate date = start; date.isBefore(remainderEnd); date = date.plusDays(1)) {
            if (!isWeekendDate(date) && recurringHolidays.contains(date.withYear(RECURRING_YEAR))) {
                recurringHolidaysCount++;
            }
        }
        return recurringHolidaysCount;
    }


    public void setHoliday(LocalDate holiday) {
        specificHolidays.add(holiday);
    }

    public void setRecurringHoliday(LocalDate recurringHoliday) {
        recurringHolidays.add(recurringHoliday.withYear(RECURRING_YEAR));
    }

    public void setWorkdayStartAndStop(LocalTime start, LocalTime stop) {
        workdayStart = start;
        workdayStop = stop;

    }

    private boolean isWeekendDate(LocalDate localDate) {
        return localDate.getDayOfWeek().equals(SATURDAY) || localDate.getDayOfWeek().equals(SUNDAY);
    }

    private long roundTowardsZero(float value) {
        if (value < 0) {
            return (long) Math.ceil(value);
        }
        return (long) Math.floor(value);
    }
}
