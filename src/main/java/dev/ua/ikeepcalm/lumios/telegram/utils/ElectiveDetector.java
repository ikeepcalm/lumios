package dev.ua.ikeepcalm.lumios.telegram.utils;

import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.DayEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.ClassType;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Works out which classes in a group timetable are optional, so each member can be asked about only
 * those.
 * <p>
 * A group timetable imported from campus is a superset: it lists every elective the group is offered,
 * and each student attends two or three of them. Optional classes betray themselves structurally,
 * because alternatives to each other have to occupy the same time slot. Two shapes exist:
 * <ul>
 *     <li>an <b>elective pool</b> - one slot holding more than one distinct subject;</li>
 *     <li>a <b>subgroup split</b> - one slot holding the same subject twice, distinguishable only by
 *     who teaches it.</li>
 * </ul>
 * Nothing has to be configured by hand.
 * <p>
 * Choices are keyed on {@link #choiceKey} rather than on a class row id, because {@code /import}
 * deletes and recreates every {@code ClassEntry}; a key derived from the subject name survives that,
 * and picking a subject covers all of its occurrences - its lecture and its lab alike, even when the
 * lab sits alone in a slot of its own.
 */
public final class ElectiveDetector {

    /**
     * Leaves room for the {@code #} and the eight-character teacher hash inside
     * {@code elective_choices.subject_key varchar(255)}.
     */
    private static final int SUBJECT_KEY_LIMIT = 200;

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
        return name.trim().toLowerCase()
                .replace('’', '\'')
                .replace('‘', '\'')
                .replace('`', '\'')
                .replace('–', '-')
                .replace('—', '-')
                .replaceAll("\\s+", " ");
    }

    /**
     * What a member's choice is recorded against.
     * <p>
     * For an elective pool that is just the subject: every occurrence of it is theirs. For a subgroup
     * split the subject alone cannot tell the two halves apart, so the teacher is folded in - hashed
     * rather than spelled out, to keep the key inside the column it is stored in.
     *
     * @param subgroupSlot whether the class sits in a slot that is a subgroup split, as reported by
     *                     {@link #shapeOf}
     */
    public static String choiceKey(ClassEntry classEntry, boolean subgroupSlot) {
        String subject = subjectKey(classEntry.getName());
        if (!subgroupSlot) {
            return subject;
        }
        String trimmed = subject.length() <= SUBJECT_KEY_LIMIT ? subject : subject.substring(0, SUBJECT_KEY_LIMIT);
        return trimmed + "#" + shortId(subjectKey(classEntry.getTeacherName()));
    }

    /**
     * Whether one time slot offers a choice, and of which shape. Derived from the slot alone, so both
     * detection and filtering can ask the same question without consulting the whole timetable.
     *
     * @param slotClasses every class the timetable puts at one (week, day, start time)
     */
    public static SlotShape shapeOf(Collection<ClassEntry> slotClasses) {
        Set<String> subjects = new HashSet<>();
        for (ClassEntry classEntry : slotClasses) {
            subjects.add(subjectKey(classEntry.getName()));
        }
        if (subjects.size() > 1) {
            return SlotShape.ELECTIVE_POOL;
        }

        Set<String> teachers = new HashSet<>();
        for (ClassEntry classEntry : slotClasses) {
            String teacher = subjectKey(classEntry.getTeacherName());
            if (!teacher.isBlank()) {
                teachers.add(teacher);
            }
        }
        // One subject taught twice by different people is a subgroup split, not a repeated feed entry.
        return teachers.size() > 1 ? SlotShape.SUBGROUP_SPLIT : SlotShape.SHARED;
    }

    /**
     * Every choice key that is optional somewhere in the given timetables. A key in this set is hidden
     * from members who did not pick it, wherever it occurs.
     */
    public static Set<String> choiceKeys(Collection<TimetableEntry> timetables) {
        Set<String> keys = new HashSet<>();
        for (List<ClassEntry> slot : slots(timetables).values()) {
            SlotShape shape = shapeOf(slot);
            if (shape.offersChoice()) {
                for (ClassEntry classEntry : slot) {
                    keys.add(choiceKey(classEntry, shape.subgroup()));
                }
            }
        }
        return keys;
    }

    /**
     * The optional classes of a timetable with their display names and when they happen, ordered for a
     * stable picker.
     */
    public static List<Elective> electives(Collection<TimetableEntry> timetables) {
        Map<SlotId, List<ClassEntry>> slots = slots(timetables);
        Set<String> optional = choiceKeys(timetables);

        Map<String, String> displayNames = new TreeMap<>();
        Map<String, Set<Occurrence>> occurrences = new HashMap<>();

        // A second pass over every slot, not just the pools: an elective's lab can sit alone in a slot
        // of its own, and it belongs on the same line of the picker as its lecture.
        for (Map.Entry<SlotId, List<ClassEntry>> slot : slots.entrySet()) {
            boolean subgroup = shapeOf(slot.getValue()).subgroup();
            for (ClassEntry classEntry : slot.getValue()) {
                String key = choiceKey(classEntry, subgroup);
                if (!optional.contains(key)) {
                    continue;
                }
                displayNames.putIfAbsent(key, displayName(classEntry, subgroup));
                occurrences.computeIfAbsent(key, id -> new HashSet<>()).add(new Occurrence(
                        slot.getKey().day(), slot.getKey().startTime(), classEntry.getClassType()));
            }
        }

        List<Elective> electives = new ArrayList<>(displayNames.size());
        displayNames.forEach((key, name) -> electives.add(new Elective(key, name, shortId(key),
                sorted(occurrences.getOrDefault(key, Set.of())))));
        electives.sort((left, right) -> left.name().compareToIgnoreCase(right.name()));
        return electives;
    }

    /**
     * Keeps the classes of one slot that a member actually attends: everything shared, plus the
     * optional ones they picked.
     * <p>
     * A member who has decided nothing about this slot yet keeps seeing all of it. Showing every
     * option to someone mid-way through the picker is mildly noisy; showing them an empty slot, or
     * silently dropping the class they are about to miss, is worse.
     *
     * @param slotClasses classes sharing one start time - what {@link #shapeOf} needs to read the slot
     * @param choiceKeys  every optional key of the chat, from {@link #choiceKeys}
     * @param chosen      the keys this member has picked
     */
    public static List<ClassEntry> personalise(List<ClassEntry> slotClasses, Set<String> choiceKeys, Set<String> chosen) {
        SlotShape shape = shapeOf(slotClasses);
        boolean undecided = shape.offersChoice() && slotClasses.stream()
                .noneMatch(classEntry -> chosen.contains(choiceKey(classEntry, shape.subgroup())));

        List<ClassEntry> mine = new ArrayList<>(slotClasses.size());
        Set<String> seen = new LinkedHashSet<>();
        for (ClassEntry classEntry : slotClasses) {
            String key = choiceKey(classEntry, shape.subgroup());
            if (!undecided && choiceKeys.contains(key) && !chosen.contains(key)) {
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
     * Short, stable handle for a key, so a choice fits in Telegram's 64-byte callback payload where
     * the subject name never would.
     */
    public static String shortId(String choiceKey) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(choiceKey.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                hex.append(String.format("%02x", digest[i]));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required to be available", e);
        }
    }

    /**
     * Resolves a {@link #shortId(String)} back to its choice key within one chat's electives.
     */
    public static String resolveShortId(Collection<TimetableEntry> timetables, String shortId) {
        Map<String, String> byShortId = new HashMap<>();
        for (Elective elective : electives(timetables)) {
            byShortId.put(elective.shortId(), elective.choiceKey());
        }
        return byShortId.get(shortId);
    }

    /**
     * A subgroup split spells out the teacher, because the subject name is identical on both halves
     * and would otherwise give the member two indistinguishable buttons.
     */
    private static String displayName(ClassEntry classEntry, boolean subgroup) {
        String name = classEntry.getName() == null ? "" : classEntry.getName().trim();
        if (!subgroup || classEntry.getTeacherName() == null || classEntry.getTeacherName().isBlank()) {
            return name;
        }
        return name + " · " + classEntry.getTeacherName().trim();
    }

    private static List<Occurrence> sorted(Set<Occurrence> occurrences) {
        return occurrences.stream()
                .sorted(Comparator.comparingInt((Occurrence occurrence) -> occurrence.day().getValue())
                        .thenComparing(Occurrence::startTime))
                .toList();
    }

    /**
     * Every class of the timetables bucketed by the slot it occupies. Entries with no start time carry
     * no slot and are dropped - they cannot be an alternative to anything.
     */
    private static Map<SlotId, List<ClassEntry>> slots(Collection<TimetableEntry> timetables) {
        Map<SlotId, List<ClassEntry>> bySlot = new LinkedHashMap<>();
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
                    bySlot.computeIfAbsent(slot, key -> new ArrayList<>()).add(classEntry);
                }
            }
        }
        return bySlot;
    }

    /**
     * Whether a slot offers the member a choice, and how its keys are built.
     */
    public enum SlotShape {

        /**
         * One subject everybody attends.
         */
        SHARED,

        /**
         * Several distinct subjects at the same hour - alternatives to each other.
         */
        ELECTIVE_POOL,

        /**
         * One subject taught twice at the same hour by different people.
         */
        SUBGROUP_SPLIT;

        public boolean offersChoice() {
            return this != SHARED;
        }

        public boolean subgroup() {
            return this == SUBGROUP_SPLIT;
        }
    }

    /**
     * @param choiceKey   stable key, as stored against a member's choice
     * @param name        display name as campus spells it, with the teacher appended for a subgroup
     * @param shortId     handle used in callback payloads
     * @param occurrences when this class happens, so near-identical names can be told apart
     */
    public record Elective(String choiceKey, String name, String shortId, List<Occurrence> occurrences) {
    }

    /**
     * One time a class happens. The week is deliberately left out: a member picking an elective does
     * not care which half of the cycle it falls in, and folding the halves together keeps the picker
     * from listing the same lecture twice.
     */
    public record Occurrence(DayOfWeek day, LocalTime startTime, ClassType classType) {
    }

    private record SlotId(String weekType, DayOfWeek day, LocalTime startTime) {
    }
}
