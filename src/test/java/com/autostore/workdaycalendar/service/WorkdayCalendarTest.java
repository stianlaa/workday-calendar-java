package com.autostore.workdaycalendar.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class WorkdayCalendarTest {

    private final LocalDateTime DEFAULT_START = LocalDateTime.of(2020, 11, 23, 8, 0);
    private final LocalTime DEFAULT_WORKDAY_START = LocalTime.of(8, 0, 0);
    private final LocalTime DEFAULT_WORKDAY_STOP = LocalTime.of(16, 0, 0);
    private final LocalDateTime FRIDAY_END = LocalDateTime.of(2020, 11, 27, 16, 0);

    private WorkdayCalendar workdayCalendar;

    @BeforeEach
    void setup() {
        workdayCalendar = new WorkdayCalendar();
        workdayCalendar.setWorkdayStartAndStop(DEFAULT_WORKDAY_START, DEFAULT_WORKDAY_STOP);
    }

    @ParameterizedTest
    @DisplayName("Predefined testcases should give expected results")
    @MethodSource("getPredefinedTestParameters")
    void predefinedTestcases(LocalDateTime startDateTime, float incrementBy, LocalDateTime expectedDateTime) {
        workdayCalendar.setWorkdayStartAndStop(DEFAULT_WORKDAY_START, DEFAULT_WORKDAY_STOP);

        workdayCalendar.setRecurringHoliday(LocalDate.of(2004, 5, 17));
        workdayCalendar.setHoliday(LocalDate.of(2004, 5, 27));

        LocalDateTime result = workdayCalendar.getWorkdayIncrement(startDateTime, incrementBy);
        result = result.withSecond(0).withNano(0);

        log.info("{} with the addition of {} working days is {}", startDateTime, incrementBy, expectedDateTime);
        assertResult(expectedDateTime, result, DEFAULT_WORKDAY_START, DEFAULT_WORKDAY_STOP);
    }

    @Test
    @DisplayName("Increment 3 workdays")
    void testPositiveIncrementWithoutHolidaysOrWeekends() {
        LocalDateTime result = workdayCalendar.getWorkdayIncrement(DEFAULT_START, 3.0f);

        // Thursday same week
        assertResult(DEFAULT_START.plusDays(3), result, DEFAULT_WORKDAY_START, DEFAULT_WORKDAY_STOP);
    }

    @Test
    @DisplayName("Increment 3 days including a holiday")
    void testPositiveIncrementWithHoliday() {
        workdayCalendar.setHoliday(DEFAULT_START.toLocalDate().plusDays(1));

        LocalDateTime result = workdayCalendar.getWorkdayIncrement(DEFAULT_START, 3.0f);

        // Friday same week
        assertResult(DEFAULT_START.plusDays(4), result, DEFAULT_WORKDAY_START, DEFAULT_WORKDAY_STOP);
    }

    @Test
    @DisplayName("Increment 5 days including a weekend")
    void testPositiveIncrementWithWeekend() {
        LocalDateTime result = workdayCalendar.getWorkdayIncrement(DEFAULT_START, 5.0f);

        // Monday next week
        assertResult(DEFAULT_START.plusDays(7), result, DEFAULT_WORKDAY_START, DEFAULT_WORKDAY_STOP);
    }

    @Test
    @DisplayName("Increment 3 days including a single recurring holiday")
    void testPositiveIncrementWithSingleRecurringHoliday() {
        workdayCalendar.setRecurringHoliday(DEFAULT_START.toLocalDate().plusDays(1));
        LocalDateTime result = workdayCalendar.getWorkdayIncrement(DEFAULT_START, 3.0f);

        // Friday same week
        assertResult(DEFAULT_START.plusDays(4), result, DEFAULT_WORKDAY_START, DEFAULT_WORKDAY_STOP);
    }

    @Test
    @DisplayName("Decrement 3.5 workdays")
    void testDecrementWithoutHolidaysOrWeekends() {
        LocalDateTime result = workdayCalendar.getWorkdayIncrement(FRIDAY_END, -3.5f);

        // Tuesday same week
        assertResult(FRIDAY_END.minusDays(3).withHour(12), result, DEFAULT_WORKDAY_START, DEFAULT_WORKDAY_STOP);
    }


    private void assertResult(LocalDateTime expected, LocalDateTime result, LocalTime workdayStart, LocalTime workdayStop) {
        result = result.withSecond(0).withNano(0);

        assertEquals(expected, result);
        assertNotEquals(DayOfWeek.SATURDAY, result.getDayOfWeek());
        assertNotEquals(DayOfWeek.SUNDAY, result.getDayOfWeek());

        LocalTime resultTime = result.toLocalTime();
        assertTrue(resultTime.isAfter(workdayStart) || resultTime.equals(workdayStart));
        assertTrue(resultTime.isBefore(workdayStop) || resultTime.equals(workdayStop));
    }

    private static Stream<Arguments> getPredefinedTestParameters() {

        return Stream.of(
                Arguments.of(LocalDateTime.of(2004, 5, 24, 18, 5),
                        -5.5f,
                        LocalDateTime.of(2004, 5, 14, 12, 0)),
                Arguments.of(LocalDateTime.of(2004, 5, 24, 19, 3),
                        44.723656f,
                        LocalDateTime.of(2004, 7, 27, 13, 47)),
                Arguments.of(LocalDateTime.of(2004, 5, 24, 18, 3),
                        -6.7470217f,
                        LocalDateTime.of(2004, 5, 13, 10, 2)),

                Arguments.of(LocalDateTime.of(2004, 5, 24, 8, 3),
                        12.782709f,
                        LocalDateTime.of(2004, 6, 10, 14, 18)),

                Arguments.of(LocalDateTime.of(2004, 5, 24, 7, 3),
                        8.276628f,
                        LocalDateTime.of(2004, 6, 4, 10, 12))
        );
    }
}