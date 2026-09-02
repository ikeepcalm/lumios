package dev.ua.ikeepcalm.lumios.telegram.utils;

import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.WeekType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

public class WeekValidator {

    /**
     * Monday of the week the two-week cycle is counted from (the week containing 2023-09-01).
     */
    private static final LocalDate ANCHOR_MONDAY =
            LocalDate.of(2023, 9, 1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

    /**
     * Which half of the two-week cycle today falls in, in Kyiv time.
     */
    public static WeekType determineWeekDay() {
        return determineWeekType(TimetableClock.today());
    }

    /**
     * Which half of the two-week cycle a given date falls in.
     * <p>
     * Counts whole weeks elapsed since {@link #ANCHOR_MONDAY}. The previous implementation
     * subtracted ISO week-of-year numbers instead, which works only while every intervening year has
     * 52 ISO weeks: 2026 has 53, so from 2027-01-04 onwards that arithmetic returned the opposite
     * week for good, quietly showing everyone the wrong half of their schedule. The parity here is
     * chosen to match what the old code produced for every date before that break.
     */
    public static WeekType determineWeekType(LocalDate date) {
        LocalDate monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        long weeksElapsed = ChronoUnit.WEEKS.between(ANCHOR_MONDAY, monday);
        return Math.floorMod(weeksElapsed, 2) == 0 ? WeekType.WEEK_B : WeekType.WEEK_A;
    }

}
