package dev.ua.ikeepcalm.lumios.database.dal.interfaces;

import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

public interface QueueService {

    MixedQueue findMixedById(UUID id) throws NoSuchEntityException;

    SimpleQueue findSimpleById(UUID id) throws NoSuchEntityException;

    List<SimpleQueue> findAllSimpleByChatId(long chatId);

    List<MixedQueue> findAllMixedByChatId(long chatId);

    @Transactional
    void save(MixedQueue mixedQueue);

    @Transactional
    void save(SimpleQueue simpleQueue);

    void deleteMixedQueue(MixedQueue mixedQueue);

    void deleteSimpleQueue(SimpleQueue simpleQueue);

}
