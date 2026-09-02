package dev.ua.ikeepcalm.lumios.database.entities.timetable.personal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * One elective a member attends, in one chat.
 * <p>
 * Keyed on the subject rather than on a class row: {@code /import} deletes and recreates every
 * {@code ClassEntry}, so a row id would not survive a re-import. Keying on the subject also means one
 * choice covers all of that subject's occurrences - its lecture and its lab alike.
 */
@Getter
@Setter
@Entity
@Table(name = "elective_choices",
        uniqueConstraints = @UniqueConstraint(name = "uk_elective_choices_chat_user_subject",
                columnNames = {"chatId", "telegramUserId", "subjectKey"}),
        indexes = @Index(name = "idx_elective_choices_chat_user", columnList = "chatId, telegramUserId"))
public class ElectiveChoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long chatId;

    @Column(nullable = false)
    private Long telegramUserId;

    /**
     * Normalised subject name, as produced by
     * {@code dev.ua.ikeepcalm.lumios.telegram.utils.ElectiveDetector#subjectKey}.
     */
    @Column(nullable = false)
    private String subjectKey;

}
