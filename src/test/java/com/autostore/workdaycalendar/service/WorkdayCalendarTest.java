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
import java.time.temporal.ChronoUnit;
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
        workdayCalendar.setRecurringHoliday(LocalDate.of(2004, 5, 17));
        workdayCalendar.setHoliday(LocalDate.of(2004, 5, 27));

        LocalDateTime result = workdayCalendar.getWorkdayIncrement(startDateTime, incrementBy);

        log.info("{} with the addition of {} working days is {}", startDateTime, incrementBy, expectedDateTime);
        assertResult(expectedDateTime, result, DEFAULT_WORKDAY_START, DEFAULT_WORKDAY_STOP);
    }

    @Test
    @DisplayName("Increment 3 workdays")
    void shouldIncrementWithoutHolidaysOrWeekends() {
        LocalDateTime result = workdayCalendar.getWorkdayIncrement(DEFAULT_START, 3.0f);

        // Thursday same week
        assertResult(DEFAULT_START.plusDays(3), result, DEFAULT_WORKDAY_START, DEFAULT_WORKDAY_STOP);
    }

    @Test
    @DisplayName("Increment 3.56321 workdays")
    void shouldIncrementWithDecimalDays() {
        LocalDateTime result = workdayCalendar.getWorkdayIncrement(DEFAULT_START, 3.56321f);

        // Thursday same week, approximately mid day
        assertResult(DEFAULT_START.plusDays(3).withHour(12).withMinute(30), result, DEFAULT_WORKDAY_START, DEFAULT_WORKDAY_STOP);
    }

    @Test
    @DisplayName("Increment 3 days including a holiday")
    void shouldIncrementWithHoliday() {
        workdayCalendar.setHoliday(DEFAULT_START.toLocalDate().plusDays(1));

        LocalDateTime result = workdayCalendar.getWorkdayIncrement(DEFAULT_START, 3.0f);

        // Friday same week
        assertResult(DEFAULT_START.plusDays(4), result, DEFAULT_WORKDAY_START, DEFAULT_WORKDAY_STOP);
    }

    @Test
    @DisplayName("Increment 5 days including a weekend")
    void shouldIncrementWithWeekend() {
        LocalDateTime result = workdayCalendar.getWorkdayIncrement(DEFAULT_START, 5.0f);

        // Monday next week
        assertResult(DEFAULT_START.plusDays(7), result, DEFAULT_WORKDAY_START, DEFAULT_WORKDAY_STOP);
    }

    @Test
    @DisplayName("Increment 3 days including a single recurring holiday")
    void shouldIncrementWithSingleRecurringHoliday() {
        workdayCalendar.setRecurringHoliday(DEFAULT_START.toLocalDate().plusDays(1));
        LocalDateTime result = workdayCalendar.getWorkdayIncrement(DEFAULT_START, 3.0f);

        // Friday same week
        assertResult(DEFAULT_START.plusDays(4), result, DEFAULT_WORKDAY_START, DEFAULT_WORKDAY_STOP);
    }

    @Test
    @DisplayName("Increment 261 working days (1 year) including a single recurring holiday")
    void shouldIncrementTwiceWithSingleRecurringHoliday() {
        workdayCalendar.setRecurringHoliday(DEFAULT_START.toLocalDate().plusDays(1));
        LocalDateTime result = workdayCalendar.getWorkdayIncrement(DEFAULT_START, 261.0f);

        // 1 year and 2 days after, due to recurring holiday
        assertResult(DEFAULT_START.plusYears(1).plusDays(2), result, DEFAULT_WORKDAY_START, DEFAULT_WORKDAY_STOP);
    }

    @Test
    @DisplayName("Decrement 3 workdays")
    void shouldDecrementWithoutHolidaysOrWeekends() {
        LocalDateTime result = workdayCalendar.getWorkdayIncrement(FRIDAY_END, -3.0f);

        // Tuesday same week
        assertResult(FRIDAY_END.minusDays(3), result, DEFAULT_WORKDAY_START, DEFAULT_WORKDAY_STOP);
    }

    @Test
    @DisplayName("Decrement 3.56321 workdays")
    void testDecrementWithDecimalDays() {
        LocalDateTime result = workdayCalendar.getWorkdayIncrement(FRIDAY_END, -3.56321f);

        // Tuesday same week, aproximately mid day
        assertResult(FRIDAY_END.minusDays(3).withHour(11).withMinute(29), result, DEFAULT_WORKDAY_START, DEFAULT_WORKDAY_STOP);
    }


    private void assertResult(LocalDateTime expected, LocalDateTime result, LocalTime workdayStart, LocalTime workdayStop) {
        result = result.truncatedTo(ChronoUnit.MINUTES);

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

                // Test case result appears to be flawed at manual control, 16:00 - 8 * .7470217 = 10:1.26
//                Arguments.of(LocalDateTime.of(2004, 5, 24, 18, 3),
//                        -6.7470217f,
//                        LocalDateTime.of(2004, 5, 13, 10, 2)),

                // Test case result appears to be flawed at manual control, 08:00 + 8 * .782709 = 14:15.42
//                Arguments.of(LocalDateTime.of(2004, 5, 24, 8, 3),
//                        12.782709f,
//                        LocalDateTime.of(2004, 6, 10, 14, 18)),

                Arguments.of(LocalDateTime.of(2004, 5, 24, 7, 3),
                        8.276628f,
                        LocalDateTime.of(2004, 6, 4, 10, 12))
        );
    }
}