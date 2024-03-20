package dev.ua.ikeepcalm.queueupnow.database.dal.impls;

import dev.ua.ikeepcalm.queueupnow.database.dal.interfaces.ShotService;
import dev.ua.ikeepcalm.queueupnow.database.dal.repositories.reverence.ChatShotRepository;
import dev.ua.ikeepcalm.queueupnow.database.dal.repositories.reverence.UserShotRepository;
import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.shots.ReverenceChatShot;
import org.springframework.stereotype.Component;

@Component
public class ShotServiceImpl implements ShotService {

    private final ChatShotRepository chatShotRepository;
    private final UserShotRepository userShotRepository;

    public ShotServiceImpl(ChatShotRepository chatShotRepository, UserShotRepository userShotRepository) {
        this.chatShotRepository = chatShotRepository;
        this.userShotRepository = userShotRepository;
    }
    @Override
    public void save(ReverenceChatShot chatShot) {
        this.userShotRepository.saveAll(chatShot.getUsers());
        this.chatShotRepository.save(chatShot);
    }
}
