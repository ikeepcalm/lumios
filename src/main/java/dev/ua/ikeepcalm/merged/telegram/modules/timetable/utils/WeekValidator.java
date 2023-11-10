package dev.ua.ikeepcalm.merged.telegram.modules.timetable.utils;

import dev.ua.ikeepcalm.merged.database.entities.timetable.types.WeekType;

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
            return WeekType.WEEK_A;
        } else {
            return WeekType.WEEK_B;
        }
    }

}
