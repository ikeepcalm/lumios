package dev.ua.ikeepcalm.lumios.telegram.utils;

import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.DayEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Works out which subjects in a group timetable are electives, so each member can be asked about
 * only those.
 * <p>
 * A group timetable imported from campus is a superset: it lists every elective the group is offered,
 * and each student attends two or three of them. Electives betray themselves structurally - a pool of
 * them occupies one time slot, because they are alternatives to each other. So any slot holding more
 * than one distinct subject is an elective pool, and every subject in it is an elective. Nothing has
 * to be configured by hand.
 * <p>
 * Choices are keyed on {@link #subjectKey(String)} rather than on a class row id, because
 * {@code /import} deletes and recreates every {@code ClassEntry}; a key derived from the subject name
 * survives that, and picking a subject covers all of its occurrences (its lecture and its lab alike).
 * <p>
 * Known limitation: when one slot holds the same subject twice - a subgroup split, distinguishable
 * only by teacher - both entries share a key and are treated as one subject. Such a slot is not an
 * elective pool and stays visible to everyone.
 */
public final class ElectiveDetector {

    private ElectiveDetector() {
    }

    /**
     * Stable identity of a subject across re-imports. Punctuation that campus varies between
     * imports (apostrophe and dash shapes, repeated spaces) is folded away.
     */
    public static String subjectKey(String name) {
        if (name == null) {
            return "";
        }
        String folded = name.trim().toLowerCase()
                .replace('’', '\'')
                .replace('‘', '\'')
                .replace('`', '\'')
                .replace('–', '-')
                .replace('—', '-')
                .replaceAll("\\s+", " ");
        return folded;
    }

    /**
     * Subject keys that belong to an elective pool anywhere in the given timetables.
     */
    public static Set<String> electiveSubjects(Collection<TimetableEntry> timetables) {
        Set<String> electives = new HashSet<>();
        for (Map.Entry<SlotId, Set<String>> slot : subjectsBySlot(timetables).entrySet()) {
            if (slot.getValue().size() > 1) {
                electives.addAll(slot.getValue());
            }
        }
        return electives;
    }

    /**
     * Elective subjects with their display names, ordered for a stable picker: as a subject can
     * appear in several slots, the first spelling encountered wins.
     */
    public static List<Elective> electives(Collection<TimetableEntry> timetables) {
        Set<String> electiveKeys = electiveSubjects(timetables);
        Map<String, String> displayNames = new TreeMap<>();
        for (ClassEntry classEntry : allClasses(timetables)) {
            String key = subjectKey(classEntry.getName());
            if (electiveKeys.contains(key)) {
                displayNames.putIfAbsent(key, classEntry.getName());
            }
        }

        List<Elective> electives = new ArrayList<>(displayNames.size());
        displayNames.forEach((key, name) -> electives.add(new Elective(key, name, shortId(key))));
        electives.sort((left, right) -> left.name().compareToIgnoreCase(right.name()));
        return electives;
    }

    /**
     * Keeps the classes a member actually attends: everything that is not an elective, plus the
     * electives they chose.
     */
    public static List<ClassEntry> personalise(List<ClassEntry> classes, Set<String> electiveSubjects, Set<String> chosenSubjects) {
        List<ClassEntry> mine = new ArrayList<>(classes.size());
        Set<String> seen = new LinkedHashSet<>();
        for (ClassEntry classEntry : classes) {
            String key = subjectKey(classEntry.getName());
            if (electiveSubjects.contains(key) && !chosenSubjects.contains(key)) {
                continue;
            }
            // The campus feed sometimes repeats an identical class within one slot. Two entries of the
            // same subject taught by different people are a subgroup split and both are kept.
            if (seen.add(key + "|" + classEntry.getClassType() + "|" + classEntry.getTeacherName())) {
                mine.add(classEntry);
            }
        }
        return mine;
    }

    /**
     * Short, stable handle for a subject, so a choice fits in Telegram's 64-byte callback payload
     * where the subject name never would.
     */
    public static String shortId(String subjectKey) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(subjectKey.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                hex.append(String.format("%02x", digest[i]));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required to be available", e);
        }
    }

    private static Map<SlotId, Set<String>> subjectsBySlot(Collection<TimetableEntry> timetables) {
        Map<SlotId, Set<String>> bySlot = new LinkedHashMap<>();
        for (TimetableEntry timetable : timetables) {
            if (timetable == null || timetable.getDays() == null) {
                continue;
            }
            for (DayEntry day : timetable.getDays()) {
                if (day.getClassEntries() == null) {
                    continue;
                }
                for (ClassEntry classEntry : day.getClassEntries()) {
                    if (classEntry.getStartTime() == null) {
                        continue;
                    }
                    SlotId slot = new SlotId(
                            timetable.getWeekType() == null ? "?" : timetable.getWeekType().name(),
                            day.getDayName(), classEntry.getStartTime());
                    bySlot.computeIfAbsent(slot, key -> new LinkedHashSet<>())
                            .add(subjectKey(classEntry.getName()));
                }
            }
        }
        return bySlot;
    }

    private static List<ClassEntry> allClasses(Collection<TimetableEntry> timetables) {
        List<ClassEntry> classes = new ArrayList<>();
        for (TimetableEntry timetable : timetables) {
            if (timetable == null || timetable.getDays() == null) {
                continue;
            }
            for (DayEntry day : timetable.getDays()) {
                if (day.getClassEntries() != null) {
                    classes.addAll(day.getClassEntries());
                }
            }
        }
        return classes;
    }

    /**
     * @param subjectKey stable key, as stored against a member's choice
     * @param name       display name as campus spells it
     * @param shortId    handle used in callback payloads
     */
    public record Elective(String subjectKey, String name, String shortId) {
    }

    private record SlotId(String weekType, DayOfWeek day, LocalTime startTime) {
    }

    /**
     * Resolves a {@link #shortId(String)} back to its subject key within one chat's electives.
     */
    public static String resolveShortId(Collection<TimetableEntry> timetables, String shortId) {
        Map<String, String> byShortId = new HashMap<>();
        for (Elective elective : electives(timetables)) {
            byShortId.put(elective.shortId(), elective.subjectKey());
        }
        return byShortId.get(shortId);
    }
}
