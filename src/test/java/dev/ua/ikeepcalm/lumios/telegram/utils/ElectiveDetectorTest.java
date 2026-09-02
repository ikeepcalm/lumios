package dev.ua.ikeepcalm.lumios.telegram.utils;

import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.ClassType;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.WeekType;
import dev.ua.ikeepcalm.lumios.telegram.utils.ElectiveDetector.Elective;
import dev.ua.ikeepcalm.lumios.telegram.utils.ElectiveDetector.SlotShape;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static dev.ua.ikeepcalm.lumios.telegram.utils.TimetableFixtures.classEntry;
import static dev.ua.ikeepcalm.lumios.telegram.utils.TimetableFixtures.lecture;
import static dev.ua.ikeepcalm.lumios.telegram.utils.TimetableFixtures.week;
import static org.assertj.core.api.Assertions.assertThat;

class ElectiveDetectorTest {

    private static final LocalTime FIRST = LocalTime.of(8, 30);
    private static final LocalTime SECOND = LocalTime.of(10, 25);

    @Nested
    @DisplayName("subjectKey")
    class SubjectKeyTest {

        @Test
        @DisplayName("folds the punctuation campus varies between imports")
        void foldsPunctuation() {
            assertThat(ElectiveDetector.subjectKey("Комп’ютерна  Графіка"))
                    .isEqualTo(ElectiveDetector.subjectKey("комп'ютерна графіка"));
            assertThat(ElectiveDetector.subjectKey("Алгебра – Основи"))
                    .isEqualTo(ElectiveDetector.subjectKey("алгебра - основи"));
        }

        @Test
        @DisplayName("treats a null name as empty rather than throwing")
        void handlesNull() {
            assertThat(ElectiveDetector.subjectKey(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("shapeOf")
    class ShapeOfTest {

        @Test
        @DisplayName("one subject alone in a slot is shared")
        void singleSubjectIsShared() {
            assertThat(ElectiveDetector.shapeOf(List.of(lecture("Аналіз", FIRST)))).isEqualTo(SlotShape.SHARED);
        }

        @Test
        @DisplayName("two distinct subjects at the same hour are alternatives")
        void twoSubjectsArePool() {
            SlotShape shape = ElectiveDetector.shapeOf(List.of(lecture("Аналіз", FIRST), lecture("Графіка", FIRST)));
            assertThat(shape).isEqualTo(SlotShape.ELECTIVE_POOL);
            assertThat(shape.offersChoice()).isTrue();
            assertThat(shape.subgroup()).isFalse();
        }

        @Test
        @DisplayName("the same subject taught twice by different people is a subgroup split")
        void sameSubjectTwoTeachersIsSubgroup() {
            SlotShape shape = ElectiveDetector.shapeOf(List.of(
                    classEntry("Іноземна мова", FIRST, ClassType.PRACTICE, "Петренко"),
                    classEntry("Іноземна мова", FIRST, ClassType.PRACTICE, "Коваль")));
            assertThat(shape).isEqualTo(SlotShape.SUBGROUP_SPLIT);
            assertThat(shape.subgroup()).isTrue();
        }

        @Test
        @DisplayName("the very same class listed twice by the feed is not a choice")
        void duplicateFeedEntryIsShared() {
            assertThat(ElectiveDetector.shapeOf(List.of(
                    classEntry("Аналіз", FIRST, ClassType.LECTURE, "Петренко"),
                    classEntry("Аналіз", FIRST, ClassType.LECTURE, "Петренко"))))
                    .isEqualTo(SlotShape.SHARED);
        }
    }

    @Nested
    @DisplayName("choiceKeys")
    class ChoiceKeysTest {

        @Test
        @DisplayName("collects every subject of a pool and nothing from a shared slot")
        void collectsPoolSubjects() {
            TimetableEntry timetable = week(DayOfWeek.MONDAY, List.of(
                    lecture("Аналіз", FIRST),
                    lecture("Графіка", SECOND),
                    lecture("Мережі", SECOND)));

            assertThat(ElectiveDetector.choiceKeys(List.of(timetable)))
                    .containsExactlyInAnyOrder("графіка", "мережі");
        }

        @Test
        @DisplayName("keys a subgroup split by teacher, so the two halves are distinguishable")
        void keysSubgroupByTeacher() {
            ClassEntry first = classEntry("Іноземна мова", FIRST, ClassType.PRACTICE, "Петренко");
            ClassEntry second = classEntry("Іноземна мова", FIRST, ClassType.PRACTICE, "Коваль");
            Set<String> keys = ElectiveDetector.choiceKeys(List.of(week(DayOfWeek.MONDAY, List.of(first, second))));

            assertThat(keys).hasSize(2).containsExactlyInAnyOrder(
                    ElectiveDetector.choiceKey(first, true), ElectiveDetector.choiceKey(second, true));
            assertThat(ElectiveDetector.choiceKey(first, true)).startsWith("іноземна мова#");
        }

        @Test
        @DisplayName("a class with no start time occupies no slot and is ignored")
        void ignoresClassesWithoutTime() {
            ClassEntry undated = lecture("Графіка", FIRST);
            undated.setStartTime(null);
            assertThat(ElectiveDetector.choiceKeys(List.of(
                    week(DayOfWeek.MONDAY, List.of(lecture("Аналіз", FIRST), undated))))).isEmpty();
        }
    }

    @Nested
    @DisplayName("electives")
    class ElectivesTest {

        @Test
        @DisplayName("reports every hour a chosen subject happens, including a lab alone in its slot")
        void collectsOccurrencesAcrossSlots() {
            // The lecture sits in a pool; the lab of the same subject sits alone. Both belong to the
            // same choice, so both have to show up against it.
            TimetableEntry weekA = week(WeekType.WEEK_A, DayOfWeek.MONDAY, List.of(
                    lecture("Графіка", FIRST),
                    lecture("Мережі", FIRST)));
            TimetableEntry weekB = week(WeekType.WEEK_B, DayOfWeek.WEDNESDAY, List.of(
                    classEntry("Графіка", SECOND, ClassType.LAB, "Петренко")));

            List<Elective> electives = ElectiveDetector.electives(List.of(weekA, weekB));

            assertThat(electives).extracting(Elective::name).containsExactly("Графіка", "Мережі");
            Elective graphics = electives.getFirst();
            assertThat(graphics.occurrences()).hasSize(2);
            assertThat(graphics.occurrences().getFirst().day()).isEqualTo(DayOfWeek.MONDAY);
            assertThat(graphics.occurrences().getLast().day()).isEqualTo(DayOfWeek.WEDNESDAY);
        }

        @Test
        @DisplayName("spells out the teacher for a subgroup, so the buttons are not identical")
        void namesSubgroupsByTeacher() {
            List<Elective> electives = ElectiveDetector.electives(List.of(week(DayOfWeek.MONDAY, List.of(
                    classEntry("Іноземна мова", FIRST, ClassType.PRACTICE, "Петренко"),
                    classEntry("Іноземна мова", FIRST, ClassType.PRACTICE, "Коваль")))));

            assertThat(electives).extracting(Elective::name)
                    .containsExactly("Іноземна мова · Коваль", "Іноземна мова · Петренко");
        }

        @Test
        @DisplayName("a shortId resolves back to the key it came from")
        void shortIdRoundTrips() {
            List<TimetableEntry> timetables = List.of(week(DayOfWeek.MONDAY, List.of(
                    lecture("Графіка", FIRST), lecture("Мережі", FIRST))));
            Elective elective = ElectiveDetector.electives(timetables).getFirst();

            assertThat(ElectiveDetector.resolveShortId(timetables, elective.shortId()))
                    .isEqualTo(elective.choiceKey());
            assertThat(ElectiveDetector.resolveShortId(timetables, "deadbeef")).isNull();
        }

        @Test
        @DisplayName("a shortId is stable, or every stored choice would break on redeploy")
        void shortIdIsStable() {
            assertThat(ElectiveDetector.shortId("графіка"))
                    .hasSize(8)
                    .isEqualTo(ElectiveDetector.shortId("графіка"));
        }
    }

    @Nested
    @DisplayName("personalise")
    class PersonaliseTest {

        private final ClassEntry shared = lecture("Аналіз", FIRST);
        private final ClassEntry graphics = lecture("Графіка", SECOND);
        private final ClassEntry networks = lecture("Мережі", SECOND);
        private final Set<String> choiceKeys = Set.of("графіка", "мережі");

        @Test
        @DisplayName("keeps a shared class whatever the member has chosen")
        void keepsSharedClasses() {
            assertThat(ElectiveDetector.personalise(List.of(shared), choiceKeys, Set.of()))
                    .containsExactly(shared);
        }

        @Test
        @DisplayName("keeps only the elective the member picked")
        void keepsChosenElective() {
            assertThat(ElectiveDetector.personalise(List.of(graphics, networks), choiceKeys, Set.of("графіка")))
                    .containsExactly(graphics);
        }

        @Test
        @DisplayName("a member who has picked nothing yet still sees the whole pool")
        void undecidedMemberSeesWholePool() {
            // The alternative is an empty slot, or silently hiding the class they are about to miss.
            assertThat(ElectiveDetector.personalise(List.of(graphics, networks), choiceKeys, Set.of()))
                    .containsExactly(graphics, networks);
        }

        @Test
        @DisplayName("hides an elective they did not pick even when it sits alone in its slot")
        void hidesLoneOccurrenceOfUnchosenElective() {
            // The lab of an elective often has a slot to itself. It is still not theirs.
            assertThat(ElectiveDetector.personalise(List.of(networks), choiceKeys, Set.of("графіка")))
                    .isEmpty();
        }

        @Test
        @DisplayName("keeps both halves of a subgroup split for an undecided member")
        void undecidedSubgroupKeepsBoth() {
            ClassEntry first = classEntry("Іноземна мова", FIRST, ClassType.PRACTICE, "Петренко");
            ClassEntry second = classEntry("Іноземна мова", FIRST, ClassType.PRACTICE, "Коваль");
            Set<String> keys = Set.of(ElectiveDetector.choiceKey(first, true),
                    ElectiveDetector.choiceKey(second, true));

            assertThat(ElectiveDetector.personalise(List.of(first, second), keys, Set.of()))
                    .containsExactly(first, second);
            assertThat(ElectiveDetector.personalise(List.of(first, second), keys,
                    Set.of(ElectiveDetector.choiceKey(second, true)))).containsExactly(second);
        }

        @Test
        @DisplayName("drops the duplicate the campus feed sometimes emits")
        void dropsDuplicateFeedEntry() {
            ClassEntry duplicate = classEntry("Аналіз", FIRST, ClassType.LECTURE, "Teacher of Аналіз");
            assertThat(ElectiveDetector.personalise(List.of(shared, duplicate), choiceKeys, Set.of()))
                    .containsExactly(shared);
        }
    }
}
