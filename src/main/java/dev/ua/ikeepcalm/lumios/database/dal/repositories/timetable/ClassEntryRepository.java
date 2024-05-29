package dev.ua.ikeepcalm.lumios.database.dal.repositories.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ClassEntryRepository extends CrudRepository<ClassEntry, Long> {

    @Query("SELECT c FROM classEntries c WHERE c.startTime BETWEEN :now AND :nextMinute AND c.dayEntry.dayName = :today")
    List<ClassEntry> findUpcomingClasses(@Param("now") LocalTime now, @Param("nextMinute") LocalTime nextMinute, @Param("today") DayOfWeek today);

}

