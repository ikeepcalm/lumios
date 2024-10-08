package dev.ua.ikeepcalm.lumios.database.dal.impls;


import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.TimetableService;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.timetable.ClassEntryRepository;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.timetable.TimetableRepository;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.WeekType;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TimetableServiceImpl implements TimetableService {

    final TimetableRepository timetableRepository;
    final ClassEntryRepository classEntryRepository;
    final ChatService chatService;

    public TimetableServiceImpl(TimetableRepository timetableRepository, ClassEntryRepository classEntryRepository,
                                ChatService chatService) {
        this.timetableRepository = timetableRepository;
        this.classEntryRepository = classEntryRepository;
        this.chatService = chatService;
    }

    @Override
    public void save(TimetableEntry timetableEntry) {
        timetableEntry.getDays().forEach(day -> day.getClassEntries().forEach(lesson -> lesson.setDayEntry(day)));
        timetableEntry.getDays().forEach(day -> day.setTimetableEntry(timetableEntry));
        timetableRepository.save(timetableEntry);
    }

    @Override
    public void save(ClassEntry classEntry) {
        classEntryRepository.save(classEntry);
    }

    @Override
    public void saveAll(List<TimetableEntry> timetableEntries) {
        for (TimetableEntry timetableEntry : timetableEntries) {
            timetableEntry.getDays().forEach(day -> day.getClassEntries().forEach(lesson -> lesson.setDayEntry(day)));
            timetableEntry.getDays().forEach(day -> day.setTimetableEntry(timetableEntry));
            timetableRepository.save(timetableEntry);
        }
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

    @Override
    public ClassEntry findClassById(Long classId) throws NoSuchEntityException {
        Optional<ClassEntry> classEntry = classEntryRepository.findById(classId);
        return classEntry.orElseThrow(() -> new NoSuchEntityException("Class with id " + classId + " not found!"));
    }

}

