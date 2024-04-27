package dev.ua.ikeepcalm.lumios.database.dal.impls;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ShotService;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.reverence.ChatShotRepository;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.reverence.UserShotRepository;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.ChatShot;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.NoSuchElementException;

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
        this.userShotRepository.saveAll(chatShot.getUsers());
        this.chatShotRepository.save(chatShot);
    }

    @Override
    public ChatShot findByChatIdAndDate(Long chatId, LocalDate date) throws NoSuchElementException {
        return this.chatShotRepository.findByReverenceChatIdAndDate(chatId, date).orElseThrow();
    }
}
