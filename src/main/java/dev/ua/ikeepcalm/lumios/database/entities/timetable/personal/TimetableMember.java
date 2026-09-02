package dev.ua.ikeepcalm.lumios.database.entities.timetable.personal;

import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.ReminderChannel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * A member who has been through the elective picker for one chat.
 * <p>
 * The row existing is what marks them as opted in to personal reminders: they can only reach the
 * picker through a private chat with the bot, so having a row means they both consented and are
 * reachable. Not tied to {@code LumiosUser}, which is per-chat bookkeeping for the reverence system.
 */
@Getter
@Setter
@Entity
@Table(name = "timetable_members",
        uniqueConstraints = @UniqueConstraint(name = "uk_timetable_members_chat_user",
                columnNames = {"chatId", "telegramUserId"}))
public class TimetableMember {

    /**
     * Used when {@link #digestTime} is null, so the column can stay nullable and a member who never
     * touched the setting still gets a sensible hour.
     */
    public static final LocalTime DEFAULT_DIGEST_TIME = LocalTime.of(7, 0);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long chatId;

    @Column(nullable = false)
    private Long telegramUserId;

    /**
     * How they want to be told about a class that is about to start.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReminderChannel reminderChannel = ReminderChannel.DM;

    /**
     * Minutes of advance warning for this member. Null falls back to the chat's setting. Only honoured
     * for the private message: the group announcement is one message shared by everyone, so it can
     * only fire at the chat's own lead time.
     */
    @Column
    private Integer leadMinutes;

    /**
     * Set once Telegram has refused a private message (the member never pressed Start). Stops the
     * scheduler trying them again for every class.
     */
    @Column(nullable = false)
    private boolean dmUnavailable = false;

    /**
     * A single private message each morning listing the member's own classes for the day.
     */
    @Column(nullable = false)
    private boolean digestEnabled = false;

    /**
     * When to send that digest. Null means {@link #DEFAULT_DIGEST_TIME}.
     */
    @Column
    private LocalTime digestTime;

    @Column
    private LocalDateTime reviewedAt;

    public LocalTime digestTimeOrDefault() {
        return digestTime == null ? DEFAULT_DIGEST_TIME : digestTime;
    }

}
