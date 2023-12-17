package dev.ua.ikeepcalm.queue.database.dal.interfaces;


import dev.ua.ikeepcalm.queue.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.queue.database.entities.timetable.types.WeekType;
import dev.ua.ikeepcalm.queue.database.exceptions.NoSuchEntityException;

import java.util.List;

public interface TimetableService {

    void save (TimetableEntry timetableEntry);

    void delete(TimetableEntry timetableEntry);

    TimetableEntry findByChatIdAndWeekType(Long chatId, WeekType weekType) throws NoSuchEntityException;
    List<TimetableEntry> findAllByChatId(Long chatId) throws NoSuchEntityException;
}

