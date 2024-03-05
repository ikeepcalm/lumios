package dev.ua.ikeepcalm.queueupnow.telegram.modules.heirs.timetable.utils;

import dev.ua.ikeepcalm.queueupnow.database.entities.timetable.types.WeekType;

import java.time.LocalDate;
import java.time.temporal.IsoFields;

public class WeekValidator {

    public static WeekType determineWeekDay() {
        LocalDate currentDate = LocalDate.now();
        int weekNumber = currentDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        LocalDate weekAStartDate = LocalDate.of(2023, 9, 1);
        int weekANumber = weekAStartDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int weekDifference = weekNumber - weekANumber;
        if (weekDifference % 2 == 0) {
            return WeekType.WEEK_B;
        } else {
            return WeekType.WEEK_A;
        }
    }

}
