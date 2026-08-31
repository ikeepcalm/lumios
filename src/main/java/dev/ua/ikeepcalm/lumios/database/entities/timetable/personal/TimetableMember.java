package dev.ua.ikeepcalm.lumios.database.entities.timetable.personal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long chatId;

    @Column(nullable = false)
    private Long telegramUserId;

    @Column(nullable = false)
    private boolean dmRemindersEnabled = true;

    /**
     * Minutes of advance warning for this member. Null falls back to the chat's setting.
     */
    @Column
    private Integer leadMinutes;

    /**
     * Set once Telegram has refused a private message (the member never pressed Start). Stops the
     * scheduler trying them again for every class.
     */
    @Column(nullable = false)
    private boolean dmUnavailable = false;

    @Column
    private LocalDateTime reviewedAt;

}
