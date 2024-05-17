package dev.ua.ikeepcalm.lumios.database.dal.repositories.queues;

import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleQueue;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SimpleQueueRepository extends CrudRepository<SimpleQueue, UUID> {

    @NotNull
    Optional<SimpleQueue> findById(@NotNull UUID id);

    List<SimpleQueue> findAllByChatId(long chatId);

    void deleteById(@NotNull UUID id);

}

