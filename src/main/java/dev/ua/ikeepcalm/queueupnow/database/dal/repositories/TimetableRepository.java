
package dev.ua.ikeepcalm.queueupnow.database.dal.repositories;

import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.queueupnow.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.queueupnow.database.entities.timetable.types.WeekType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimetableRepository extends CrudRepository<TimetableEntry, Long> {

    Optional<TimetableEntry> findByChatAndWeekType(ReverenceChat chat, WeekType weekType);
    List<TimetableEntry> findAllByChat(ReverenceChat chat);
}

