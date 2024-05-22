package dev.ua.ikeepcalm.lumios.database.dal.impls;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ShotService;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.reverence.ChatShotRepository;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.reverence.UserShotRepository;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.ChatShot;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ShotServiceImpl implements ShotService {

    private final ChatShotRepository chatShotRepository;
    private final UserShotRepository userShotRepository;

    public ShotServiceImpl(ChatShotRepository chatShotRepository, UserShotRepository userShotRepository) {
        this.chatShotRepository = chatShotRepository;
        this.userShotRepository = userShotRepository;
    }

    @Override
    public void save(ChatShot chatShot) {
        this.userShotRepository.saveAll(chatShot.getUserShots());
        this.chatShotRepository.save(chatShot);
    }

    @Override
    public ChatShot findByChatIdAndDate(Long chatId, LocalDate date) throws NoSuchEntityException {
        return this.chatShotRepository.findByReverenceChat_ChatIdAndDate(chatId, date).orElseThrow(NoSuchEntityException::new);
    }
}
