package dev.ua.ikeepcalm.lumios.telegram.utils;

import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.DayEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.ClassType;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.WeekType;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds timetables the way the campus import does, so the detector under test sees the same shape it
 * sees in production - including the two-sided wiring between a day and its classes.
 */
final class TimetableFixtures {

    private TimetableFixtures() {
    }

    static ClassEntry classEntry(String name, LocalTime startTime, ClassType classType, String teacher) {
        ClassEntry entry = new ClassEntry();
        entry.setName(name);
        entry.setStartTime(startTime);
        entry.setEndTime(startTime.plusMinutes(90));
        entry.setClassType(classType);
        entry.setTeacherName(teacher);
        return entry;
    }

    static ClassEntry lecture(String name, LocalTime startTime) {
        return classEntry(name, startTime, ClassType.LECTURE, "Teacher of " + name);
    }

    /**
     * One week of a timetable. {@code DayEntry.classEntries} and {@code ClassEntry.dayEntry} are both
     * set, because the production model maps the association from both sides.
     */
    static TimetableEntry week(WeekType weekType, DayOfWeek day, List<ClassEntry> classes) {
        TimetableEntry timetable = new TimetableEntry();
        timetable.setWeekType(weekType);

        DayEntry dayEntry = new DayEntry();
        dayEntry.setDayName(day);
        dayEntry.setClassEntries(new ArrayList<>(classes));
        dayEntry.setTimetableEntry(timetable);
        classes.forEach(entry -> entry.setDayEntry(dayEntry));

        timetable.setDays(new ArrayList<>(List.of(dayEntry)));
        return timetable;
    }

    static TimetableEntry week(DayOfWeek day, List<ClassEntry> classes) {
        return week(WeekType.WEEK_A, day, classes);
    }
}
