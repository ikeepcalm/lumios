package dev.ua.ikeepcalm.queueupnow.database.dal.interfaces;

import dev.ua.ikeepcalm.queueupnow.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.queueupnow.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.queueupnow.database.exceptions.NoSuchEntityException;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

public interface QueueService {

    MixedQueue findMixedById(UUID id) throws NoSuchEntityException;
    SimpleQueue findSimpleById(UUID id) throws NoSuchEntityException;

    List<SimpleQueue> findAllSimpleByChatId(long chatId);

    @Transactional
    void save(MixedQueue mixedQueue);
    @Transactional
    void save(SimpleQueue simpleQueue);

    void deleteMixedQueue(MixedQueue simpleQueue);
    void deleteSimpleQueue(SimpleQueue simpleQueue);

}
