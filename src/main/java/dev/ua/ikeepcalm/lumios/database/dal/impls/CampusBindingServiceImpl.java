package dev.ua.ikeepcalm.lumios.database.dal.impls;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.CampusBindingService;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.campus.CampusBindingRepository;
import dev.ua.ikeepcalm.lumios.database.entities.campus.CampusBinding;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampusBindingServiceImpl implements CampusBindingService {

    private final CampusBindingRepository campusBindingRepository;

    public CampusBindingServiceImpl(CampusBindingRepository campusBindingRepository) {
        this.campusBindingRepository = campusBindingRepository;
    }

    @Override
    public void save(CampusBinding binding) {
        campusBindingRepository.save(binding);
    }

    @Override
    public CampusBinding findByTelegramUserId(Long telegramUserId) throws NoSuchEntityException {
        return campusBindingRepository.findByTelegramUserId(telegramUserId)
                .orElseThrow(NoSuchEntityException::new);
    }

    @Override
    @Transactional
    public void deleteByTelegramUserId(Long telegramUserId) {
        campusBindingRepository.deleteByTelegramUserId(telegramUserId);
    }

    @Override
    public boolean existsByTelegramUserId(Long telegramUserId) {
        return campusBindingRepository.existsByTelegramUserId(telegramUserId);
    }

}
