package dev.ua.ikeepcalm.lumios.database.dal.repositories.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.timetable.personal.ElectiveChoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElectiveChoiceRepository extends JpaRepository<ElectiveChoice, Long> {

    List<ElectiveChoice> findByChatIdAndTelegramUserId(Long chatId, Long telegramUserId);

    List<ElectiveChoice> findByChatId(Long chatId);

    void deleteByChatIdAndTelegramUserIdAndSubjectKey(Long chatId, Long telegramUserId, String subjectKey);

    boolean existsByChatIdAndTelegramUserIdAndSubjectKey(Long chatId, Long telegramUserId, String subjectKey);

}
