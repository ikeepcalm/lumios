package dev.ua.ikeepcalm.lumios.database.dal.repositories.reverence;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository
        extends CrudRepository<LumiosChat, Long> {
    Optional<LumiosChat> findByChatId(long var1);
    
    @Modifying
    @Query("UPDATE LumiosChat c SET c.summaryLimit = :summaryLimit, c.communicationLimit = :communicationLimit")
    int batchUpdateLimits(@Param("summaryLimit") int summaryLimit, @Param("communicationLimit") int communicationLimit);
}

