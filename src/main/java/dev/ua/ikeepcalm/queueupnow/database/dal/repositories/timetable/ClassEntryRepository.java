
package dev.ua.ikeepcalm.queueupnow.database.dal.repositories.timetable;

import dev.ua.ikeepcalm.queueupnow.database.entities.timetable.ClassEntry;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassEntryRepository extends CrudRepository<ClassEntry, Long> {

}

