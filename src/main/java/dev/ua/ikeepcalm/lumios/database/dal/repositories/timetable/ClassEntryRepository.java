package dev.ua.ikeepcalm.lumios.database.dal.repositories.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClassEntryRepository extends CrudRepository<ClassEntry, Long> {

    /**
     * Classes starting in [from, until) on the given weekday.
     * <p>
     * The bound is half-open on purpose. The previous query used BETWEEN, which is inclusive on both
     * ends, so a class starting on an exact minute matched in two consecutive scheduler runs and only
     * an in-memory cache stopped the duplicate message.
     */
    @Query("SELECT c FROM classEntries c WHERE c.startTime >= :from AND c.startTime < :until AND c.dayEntry.dayName = :today")
    List<ClassEntry> findClassesStartingBetween(@Param("from") LocalTime from, @Param("until") LocalTime until, @Param("today") DayOfWeek today);

    @NotNull
    Optional<ClassEntry> findById(long id);

}

