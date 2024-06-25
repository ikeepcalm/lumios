package dev.ua.ikeepcalm.lumios.database.dal.repositories.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.WeekType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimetableRepository extends CrudRepository<TimetableEntry, Long> {

    Optional<TimetableEntry> findByChatAndWeekType(LumiosChat chat, WeekType weekType);

    List<TimetableEntry> findAllByChat(LumiosChat chat);
}

