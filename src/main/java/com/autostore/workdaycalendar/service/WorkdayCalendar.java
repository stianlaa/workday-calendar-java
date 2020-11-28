package com.autostore.workdaycalendar.service;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;

/*
TODO fix issue with rounding
TODO create basic human input solution, and finish README.md,
TODO write more tests for negative cases
TODO write test cases for multiple years, and longer ranges
 */

@Slf4j
public class WorkdayCalendar {

    private static final Integer RECURRING_YEAR = 2000;
    private SortedSet<LocalDate> specificHolidays = new TreeSet<>();
    private SortedSet<LocalDate> recurringHolidays = new TreeSet<>();
    private LocalTime workdayStart, workdayStop;

    public LocalDateTime getWorkdayIncrement(LocalDateTime startDateTime, float incrementInWorkdays) {
        boolean isIncrementing = incrementInWorkdays >= 0;
        long wholeWorkdays = roundTowardsZero(incrementInWorkdays);
        float workdayRemainder = incrementInWorkdays - wholeWorkdays;

        LocalDate fromDate = findFirstWeekdayFrom(startDateTime, isIncrementing);
        LocalDate toDate = startDateTime.toLocalDate().plusDays(wholeWorkdays);

        LocalDate dateResult = calculateIncrementDate(fromDate, toDate, isIncrementing);
        LocalTime timeResult = calculateIncrementTime(workdayRemainder, isIncrementing);

        return dateResult.atTime(timeResult);
    }

    private LocalDate calculateIncrementDate(LocalDate initialDateA, LocalDate initialDateB, boolean isIncrementing) {
        LocalDate fromDate = initialDateA;
        LocalDate toDate = initialDateB;

        while (!fromDate.equals(toDate)) {
            log.info("Counting workdays from {} to {}", fromDate, toDate);

            long remainingUncheckedDays = nonWorkdaysBetween(fromDate, toDate, isIncrementing);

            fromDate = toDate;
            toDate = isIncrementing ? fromDate.plusDays(remainingUncheckedDays) : fromDate.minusDays(remainingUncheckedDays);
        }
        return toDate;
    }

    private LocalTime calculateIncrementTime(float workdayRemainder, boolean isIncrementing) {
        long workdayRemainderNanos = (long) (ChronoUnit.NANOS.between(workdayStart, workdayStop) * workdayRemainder);
        return isIncrementing ? workdayStart.plusNanos(workdayRemainderNanos) : workdayStop.plusNanos(workdayRemainderNanos);
    }

    private LocalDate findFirstWeekdayFrom(LocalDateTime localDateTime, boolean isIncrementing) {
        LocalDate localDate = localDateTime.toLocalDate();
        localDate = localDateTime.toLocalTime().isAfter(workdayStop) ? localDate.plusDays(1) : localDate;
        if (isWeekendDate(localDate)) {
            if (isIncrementing) {
                return localDate.plusDays(localDate.getDayOfWeek().equals(SATURDAY) ? 2 : 1);
            } else {
                return localDate.minusDays(localDate.getDayOfWeek().equals(SATURDAY) ? 1 : 2);
            }
        }
        return localDate;
    }

    private long nonWorkdaysBetween(LocalDate dateA, LocalDate dateB, boolean isIncrementing) {
        LocalDate start = dateA.isBefore(dateB) ? dateA : dateB;
        LocalDate end = dateA.isBefore(dateB) ? dateB : dateA;
        return weekendDaysBetween(start, end, isIncrementing) + specificHolidaysBetween(start, end) + recurringHolidaysBetween(start, end);
    }

    private long weekendDaysBetween(LocalDate start, LocalDate end, boolean isIncrementing) {

        long entireWeeksBetween = ChronoUnit.WEEKS.between(start, end);
        long weekendDaysCount = entireWeeksBetween * 2;
        LocalDate remainderEnd = end.minusWeeks(entireWeeksBetween);

        // TODO Temporary solution, very verbose.
        if (isIncrementing) {
            for (LocalDate date = start.plusDays(1); date.isBefore(remainderEnd) || date.equals(remainderEnd); date = date.plusDays(1)) {
                if (isWeekendDate(date)) {
                    weekendDaysCount++;
                }
            }
        } else {
            for (LocalDate date = remainderEnd.minusDays(1); date.isAfter(start) || date.equals(start); date = date.minusDays(1)) {
                if (isWeekendDate(date)) {
                    weekendDaysCount++;
                }
            }
        }
        log.info("Skipping {} days from weekends", weekendDaysCount);
        return weekendDaysCount;
    }

    private long specificHolidaysBetween(LocalDate start, LocalDate end) {
        long holidays = specificHolidays.subSet(start, end).stream()
                .filter(file -> !isWeekendDate(file))
                .count();
        log.info("Skipping {} days from specific holidays", holidays);
        return holidays;
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
        log.info("Skipping {} days from recurring holidays", recurringHolidaysCount);
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
        return (long) ((value < 0) ? Math.ceil(value) : Math.floor(value));
    }
}
