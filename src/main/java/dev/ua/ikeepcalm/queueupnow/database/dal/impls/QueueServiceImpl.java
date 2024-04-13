package dev.ua.ikeepcalm.queueupnow.database.dal.impls;

import dev.ua.ikeepcalm.queueupnow.database.dal.interfaces.QueueService;
import dev.ua.ikeepcalm.queueupnow.database.dal.repositories.queues.MixedQueueRepository;
import dev.ua.ikeepcalm.queueupnow.database.dal.repositories.queues.MixedUserRepository;
import dev.ua.ikeepcalm.queueupnow.database.dal.repositories.queues.SimpleQueueRepository;
import dev.ua.ikeepcalm.queueupnow.database.dal.repositories.queues.SimpleUserRepository;
import dev.ua.ikeepcalm.queueupnow.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.queueupnow.database.entities.queue.MixedUser;
import dev.ua.ikeepcalm.queueupnow.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.queueupnow.database.entities.queue.SimpleUser;
import dev.ua.ikeepcalm.queueupnow.database.exceptions.NoSuchEntityException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class QueueServiceImpl implements QueueService {

    private final MixedQueueRepository mixedQueueRepository;
    private final SimpleQueueRepository simpleQueueRepository;
    private final SimpleUserRepository simpleUserRepository;

    public QueueServiceImpl(MixedQueueRepository mixedQueueRepository, SimpleQueueRepository simpleQueueRepository, SimpleUserRepository simpleUserRepository) {
        this.mixedQueueRepository = mixedQueueRepository;
        this.simpleQueueRepository = simpleQueueRepository;
        this.simpleUserRepository = simpleUserRepository;
    }

    @Override
    public MixedQueue findMixedById(UUID id) throws NoSuchEntityException {
        return mixedQueueRepository.findById(id).orElseThrow(() -> new NoSuchEntityException("No such queue with id: " + id));
    }

    @Override
    public SimpleQueue findSimpleById(UUID id) throws NoSuchEntityException {
        return simpleQueueRepository.findById(id).orElseThrow(() -> new NoSuchEntityException("No such queue with id: " + id));
    }

    @Override
    public List<SimpleQueue> findAllSimpleByChatId(long chatId) {
        return simpleQueueRepository.findAllByChatId(chatId);
    }

    @Override
    public void save(MixedQueue mixedQueue) {
        for (MixedUser mixedUser : mixedQueue.getContents()) {
            mixedUser.setMixedQueue(mixedQueue);
        } mixedQueueRepository.save(mixedQueue);
    }

    @Override
    @Transactional
    public void save(SimpleQueue simpleQueue) {
        for (SimpleUser simpleUser : simpleQueue.getContents()) {
            simpleUser.setSimpleQueue(simpleQueue);
        }

        List<SimpleUser> existingUsers = simpleUserRepository.findBySimpleQueueId(simpleQueue.getId());
        for (SimpleUser existingUser : existingUsers) {
            if (!simpleQueue.getContents().contains(existingUser)) {
                simpleUserRepository.delete(existingUser);
            }
        } simpleQueueRepository.save(simpleQueue);
    }

    @Override
    public void deleteMixedQueue(MixedQueue simpleQueue) {
        mixedQueueRepository.delete(simpleQueue);
    }

    @Override
    public void deleteSimpleQueue(SimpleQueue simpleQueue) {
        simpleQueueRepository.deleteById(simpleQueue.getId());
    }

}
