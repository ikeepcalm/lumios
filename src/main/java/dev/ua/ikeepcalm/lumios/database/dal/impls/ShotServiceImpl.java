package dev.ua.ikeepcalm.lumios.database.dal.impls;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ShotService;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.reverence.ChatShotRepository;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.reverence.UserShotRepository;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.shots.ChatShot;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

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
        List<ChatShot> chatShots = this.chatShotRepository.findAllByChat_ChatIdAndDate(chatId, date);
        if (chatShots.isEmpty()) {
            throw new NoSuchEntityException("ChatShot not found");
        }

        if (chatShots.size() > 1) {
            chatShotRepository.delete(chatShots.get(1));
        }

        return chatShots.get(0);
    }

    @Override
    public void delete(ChatShot chatShot) {
        this.chatShotRepository.delete(chatShot);
    }
}
