package dev.ua.ikeepcalm.lumios.database.dal.repositories.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.timetable.personal.TimetableMember;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.ReminderChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimetableMemberRepository extends JpaRepository<TimetableMember, Long> {

    Optional<TimetableMember> findByChatIdAndTelegramUserId(Long chatId, Long telegramUserId);

    /**
     * Everyone in the chat who wants a reminder in any shape. The caller decides per member whether
     * that means a private message, a mention in the group, or both.
     */
    List<TimetableMember> findByChatIdAndReminderChannelNot(Long chatId, ReminderChannel excluded);

    List<TimetableMember> findByTelegramUserId(Long telegramUserId);

    /**
     * Members due a digest at exactly this time. {@code digestTime} is null for anyone who never
     * changed it, so the default hour is matched separately by the caller.
     */
    List<TimetableMember> findByDigestEnabledTrueAndDmUnavailableFalseAndDigestTime(LocalTime digestTime);

    List<TimetableMember> findByDigestEnabledTrueAndDmUnavailableFalseAndDigestTimeIsNull();

}
