package dev.ua.ikeepcalm.merged.database.dal.interfaces;


import dev.ua.ikeepcalm.merged.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.merged.database.entities.timetable.types.WeekType;
import dev.ua.ikeepcalm.merged.database.exceptions.NoSuchEntityException;

import java.util.List;

public interface TimetableService {

    void save (TimetableEntry timetableEntry);

    void delete(TimetableEntry timetableEntry);

    TimetableEntry findByChatIdAndWeekType(Long chatId, WeekType weekType) throws NoSuchEntityException;
    List<TimetableEntry> findAllByChatId(Long chatId) throws NoSuchEntityException;
}

