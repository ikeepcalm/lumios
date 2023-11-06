package dev.ua.ikeepcalm.merged.database.dal.impls;


import dev.ua.ikeepcalm.merged.database.dal.interfaces.TimetableService;
import dev.ua.ikeepcalm.merged.database.dal.repositories.TimetableRepository;
import dev.ua.ikeepcalm.merged.database.entities.timetable.Timetable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TimetableServiceImpl implements TimetableService {

    @Autowired
    TimetableRepository timetableRepository;

    @Override
    public void save(Timetable timetable) {
        timetable.getDays().forEach(day -> day.getClassEntries().forEach(lesson -> lesson.setDayEntry(day)));
        timetable.getDays().forEach(day -> day.setTimetable(timetable));
        timetableRepository.save(timetable);
    }
}

