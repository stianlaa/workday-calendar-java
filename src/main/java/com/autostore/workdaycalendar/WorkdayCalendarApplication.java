package com.autostore.workdaycalendar;

import com.autostore.workdaycalendar.service.WorkdayCalendar;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

@Slf4j
@SpringBootApplication
public class WorkdayCalendarApplication {

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        SpringApplication.run(WorkdayCalendarApplication.class, args);
        WorkdayCalendar workdayCalendar = new WorkdayCalendar();
        LocalDateTime startDateTime;
        Float incrementInWorkdays;

        log.info("Welcome to the workday calculator");

        log.info("Workday is set from 08.00 to 16:00");
        workdayCalendar.setWorkdayStartAndStop(LocalTime.of(8, 0, 0), LocalTime.of(16, 0, 0));
        log.info("Specific holiday is added at 27. May 2004");
        workdayCalendar.setHoliday(LocalDate.of(2004, 5, 27));
        log.info("Recurring holiday is added at 17. May every year");
        workdayCalendar.setRecurringHoliday(LocalDate.of(2004, 5, 17));


        log.info("Please enter starting dateTime in format: yyyy-MM-dd HH:mm, such as 2004-05-24 07:00");
        while (true) {
            try {
                startDateTime = LocalDateTime.parse(input.nextLine(), formatter);
                break;
            } catch (DateTimeParseException e) {
                log.warn("Invalid format of starting dateTime entry, try again");
            }
        }
        log.info("Please enter starting dateTime in format: x.xx, such as -5.5");
        while (true) {
            try {
                String entry = input.nextLine();
                incrementInWorkdays = Float.valueOf(entry);
                break;
            } catch (NumberFormatException e) {
                log.warn("Invalid format of incrementInWorkdays entry, try again");
            }
        }
        LocalDateTime result = workdayCalendar.getWorkdayIncrement(startDateTime, incrementInWorkdays);
        log.info("{} with the addition of {} working days is {}", startDateTime, incrementInWorkdays, result);

    }

}