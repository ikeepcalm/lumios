package dev.ua.ikeepcalm.lumios.database.dal.impls;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.QueueService;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.queues.MixedQueueRepository;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.queues.MixedUserRepository;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.queues.SimpleQueueRepository;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.queues.SimpleUserRepository;
import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.MixedUser;
import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class QueueServiceImpl implements QueueService {

    private final MixedQueueRepository mixedQueueRepository;
    private final SimpleQueueRepository simpleQueueRepository;
    private final SimpleUserRepository simpleUserRepository;
    private final MixedUserRepository mixedUserRepository;

    public QueueServiceImpl(MixedQueueRepository mixedQueueRepository, SimpleQueueRepository simpleQueueRepository, SimpleUserRepository simpleUserRepository, MixedUserRepository mixedUserRepository) {
        this.mixedQueueRepository = mixedQueueRepository;
        this.simpleQueueRepository = simpleQueueRepository;
        this.simpleUserRepository = simpleUserRepository;
        this.mixedUserRepository = mixedUserRepository;
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
    public List<MixedQueue> findAllMixedByChatId(long chatId) {
        return mixedQueueRepository.findAllByChatId(chatId);
    }

    @Override
    public void save(MixedQueue mixedQueue) {
        for (MixedUser mixedUser : mixedQueue.getContents()) {
            mixedUser.setMixedQueue(mixedQueue);
        }

        List<MixedUser> existingUsers = mixedUserRepository.findAllByMixedQueueId(mixedQueue.getId());
        for (MixedUser existingUser : existingUsers) {
            if (!mixedQueue.getContents().contains(existingUser)) {
                mixedUserRepository.delete(existingUser);
            }
        }

        mixedQueueRepository.save(mixedQueue);
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
        }
        simpleQueueRepository.save(simpleQueue);
    }

    @Override
    public void deleteMixedQueue(MixedQueue mixedQueue) {
        try {
            mixedUserRepository.deleteAll(findMixedById(mixedQueue.getId()).getContents());
        } catch (NoSuchEntityException e) {
            throw new RuntimeException(e);
        }
        mixedQueueRepository.deleteById(mixedQueue.getId());
    }

    @Override
    public void deleteSimpleQueue(SimpleQueue simpleQueue) {
        try {
            simpleUserRepository.deleteAll(findSimpleById(simpleQueue.getId()).getContents());
        } catch (NoSuchEntityException e) {
            throw new RuntimeException(e);
        }
        simpleQueueRepository.deleteById(simpleQueue.getId());
    }

}
