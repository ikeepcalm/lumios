package dev.ua.ikeepcalm.lumios.database.dal.impls;


import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.TimetableService;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.timetable.TimetableRepository;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.WeekType;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import org.springframework.stereotype.Service;

import java.sql.SQLSyntaxErrorException;
import java.util.List;
import java.util.Optional;

@Service
public class TimetableServiceImpl implements TimetableService {

    final TimetableRepository timetableRepository;
    final ChatService chatService;

    public TimetableServiceImpl(TimetableRepository timetableRepository,
                                ChatService chatService) {
        this.timetableRepository = timetableRepository;
        this.chatService = chatService;
    }

    @Override
    public void save(TimetableEntry timetableEntry) {
        timetableEntry.getDays().forEach(day -> day.getClassEntries().forEach(lesson -> lesson.setDayEntry(day)));
        timetableEntry.getDays().forEach(day -> day.setTimetableEntry(timetableEntry));
        timetableRepository.save(timetableEntry);
    }

    @Override
    public void delete(TimetableEntry timetableEntry) {
        timetableRepository.delete(timetableEntry);
    }

    @Override
    public void deleteAll(List<TimetableEntry> timetableEntries) {
        timetableRepository.deleteAll(timetableEntries);
    }

    @Override
    public TimetableEntry findByChatIdAndWeekType(Long chatId, WeekType weekType) throws NoSuchEntityException {
        Optional<TimetableEntry> timetable = timetableRepository.findByChatAndWeekType(chatService.findByChatId(chatId), weekType);
        return timetable.orElseThrow(() -> new NoSuchEntityException("Timetable for chat with id " + chatId + " not found!"));
    }

    @Override
    public List<TimetableEntry> findAllByChatId(Long chatId) throws NoSuchEntityException {
        List<TimetableEntry> timetableEntries = timetableRepository.findAllByChat(chatService.findByChatId(chatId));
        if (timetableEntries.isEmpty()) {
            throw new NoSuchEntityException("Timetables for chat with id " + chatId + " not found!");
        } else {
            return timetableEntries;
        }
    }

}

