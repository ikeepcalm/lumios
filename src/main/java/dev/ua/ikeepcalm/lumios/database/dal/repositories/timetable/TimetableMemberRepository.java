package dev.ua.ikeepcalm.lumios.database.dal.repositories.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.timetable.personal.TimetableMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimetableMemberRepository extends JpaRepository<TimetableMember, Long> {

    Optional<TimetableMember> findByChatIdAndTelegramUserId(Long chatId, Long telegramUserId);

    List<TimetableMember> findByChatIdAndDmRemindersEnabledTrueAndDmUnavailableFalse(Long chatId);

    List<TimetableMember> findByTelegramUserId(Long telegramUserId);

}
