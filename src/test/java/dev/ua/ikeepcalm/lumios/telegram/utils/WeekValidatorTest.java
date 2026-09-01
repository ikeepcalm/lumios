package dev.ua.ikeepcalm.lumios.telegram.utils;

import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.WeekType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class WeekValidatorTest {

    @Test
    @DisplayName("every day of one week falls in the same half of the cycle")
    void oneWeekIsOneHalf() {
        // The anchor week - 2023-09-01 is a Friday, so its Monday is 2023-08-28.
        LocalDate monday = LocalDate.of(2023, 8, 28);
        for (int day = 0; day < 7; day++) {
            assertThat(WeekValidator.determineWeekType(monday.plusDays(day)))
                    .as("day %s", monday.plusDays(day))
                    .isEqualTo(WeekType.WEEK_B);
        }
    }

    @Test
    @DisplayName("consecutive weeks alternate")
    void consecutiveWeeksAlternate() {
        LocalDate monday = LocalDate.of(2023, 8, 28);
        for (int week = 0; week < 200; week++) {
            WeekType expected = week % 2 == 0 ? WeekType.WEEK_B : WeekType.WEEK_A;
            assertThat(WeekValidator.determineWeekType(monday.plusWeeks(week)))
                    .as("week %s", week)
                    .isEqualTo(expected);
        }
    }

    @Test
    @DisplayName("Sunday and the Monday after it are different halves of the cycle")
    void sundayAndNextMondayDiffer() {
        // This is why /tomorrow has to resolve the week from tomorrow's date: asked on a Sunday it was
        // reading the current week's timetable while showing the next week's day.
        LocalDate sunday = LocalDate.of(2026, 9, 6);
        assertThat(sunday.getDayOfWeek().getValue()).isEqualTo(7);
        assertThat(WeekValidator.determineWeekType(sunday))
                .isNotEqualTo(WeekValidator.determineWeekType(sunday.plusDays(1)));
    }

    @Test
    @DisplayName("dates before the anchor still resolve, rather than folding onto one half")
    void handlesDatesBeforeTheAnchor() {
        LocalDate beforeAnchor = LocalDate.of(2023, 8, 21);
        assertThat(WeekValidator.determineWeekType(beforeAnchor)).isEqualTo(WeekType.WEEK_A);
    }

    @Test
    @DisplayName("stays correct across the 53-week ISO year that broke the old arithmetic")
    void survivesFiftyThreeWeekYear() {
        // 2026 has 53 ISO weeks. Counting whole weeks from the anchor does not care; subtracting ISO
        // week-of-year numbers, as this once did, flipped for good from 2027-01-04 onwards.
        LocalDate monday = LocalDate.of(2027, 1, 4);
        long weeksElapsed = java.time.temporal.ChronoUnit.WEEKS.between(LocalDate.of(2023, 8, 28), monday);
        WeekType expected = weeksElapsed % 2 == 0 ? WeekType.WEEK_B : WeekType.WEEK_A;
        assertThat(WeekValidator.determineWeekType(monday)).isEqualTo(expected);
    }
}
