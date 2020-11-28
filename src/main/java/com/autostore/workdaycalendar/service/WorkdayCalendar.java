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

        LocalDate dateResult = findIncrementedDate(startDateTime, wholeWorkdays, isIncrementing);
        LocalTime timeResult = findIncrementedTime(workdayRemainder, isIncrementing);

        return dateResult.atTime(timeResult);
    }

    private LocalDate findIncrementedDate(LocalDateTime startDateTime, long wholeWorkdays, boolean isIncrementing) {
        LocalDate fromDate = findStartingDate(startDateTime, isIncrementing);
        for (long countedWorkdays = 0; countedWorkdays != wholeWorkdays; countedWorkdays += (isIncrementing ? 1 : -1)) {
            fromDate = findNextWorkingdate(fromDate, isIncrementing);
        }
        return fromDate;
    }

    private LocalDate findStartingDate(LocalDateTime localDateTime, boolean isIncrementing) {
        LocalDate localDate = localDateTime.toLocalDate();
        if (localDateTime.toLocalTime().isAfter(workdayStop) && isIncrementing) {
            return localDate.plusDays(1);
        } else if (localDateTime.toLocalTime().isBefore(workdayStart) && !isIncrementing) {
            return localDate.minusDays(1);
        }
        return localDate;
    }

    private LocalTime findIncrementedTime(float workdayRemainder, boolean isIncrementing) {
        long workdayRemainderNanos = (long) (ChronoUnit.NANOS.between(workdayStart, workdayStop) * workdayRemainder);
        return isIncrementing ? workdayStart.plusNanos(workdayRemainderNanos) : workdayStop.plusNanos(workdayRemainderNanos);
    }

    private LocalDate findNextWorkingdate(LocalDate fromDate, boolean isIncrementing) {
        LocalDate nextCandidate = fromDate;
        while (true) {
            nextCandidate = nextCandidate.plusDays(isIncrementing ? 1 : -1);
            if (isWeekendDate(nextCandidate) ||
                    specificHolidays.contains(nextCandidate) ||
                    recurringHolidays.contains(nextCandidate.withYear(RECURRING_YEAR))) continue;
            return nextCandidate;
        }
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
