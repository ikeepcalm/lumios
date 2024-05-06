package dev.ua.ikeepcalm.lumios.database.dal.impls;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.BindService;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.reverence.BindRepository;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceBind;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import org.springframework.stereotype.Service;

@Service
public class BindServiceImpl implements BindService {

    private final BindRepository bindRepository;

    public BindServiceImpl(BindRepository bindRepository) {
        this.bindRepository = bindRepository;
    }

    @Override
    public void save(ReverenceBind reverenceBind) {
        bindRepository.save(reverenceBind);
    }

    @Override
    public ReverenceBind findByChatId(Long chatId) throws NoSuchEntityException {
        return bindRepository.findByChatId(chatId).orElseThrow(NoSuchEntityException::new);
    }

    @Override
    public ReverenceBind findByUserId(Long userId) throws NoSuchEntityException {
        return bindRepository.findByUserId(userId).orElseThrow(NoSuchEntityException::new);
    }

    @Override
    public void deleteByUserId(Long userId) {
        bindRepository.deleteByUserId(userId);
    }
}
