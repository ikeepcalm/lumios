
package dev.ua.ikeepcalm.queue.database.dal.repositories;

import dev.ua.ikeepcalm.queue.database.entities.timetable.ClassEntry;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassEntryRepository extends CrudRepository<ClassEntry, Long> {

}

