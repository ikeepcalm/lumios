package dev.ua.ikeepcalm.lumios.database.dal.impls;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.reverence.UserRepository;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public LumiosUser findById(long userId, LumiosChat lumiosChat) throws NoSuchEntityException {
        Optional<LumiosUser> reverenceUser = this.userRepository.findReverenceUserByUserIdAndChat(userId, lumiosChat);
        if (reverenceUser.isPresent()) {
            return reverenceUser.get();
        } else {
            throw new NoSuchEntityException("No such user with id: " + userId);
        }
    }

    @Override
    public void updateAll() {
        Iterable<LumiosUser> list = userRepository.findAll();
        for (LumiosUser user : list) {
            user.setCredits(user.getSustainable());
            if (user.getReverence() > 0) {
                user.setBalance((int) (user.getBalance() + Math.sqrt(user.getReverence())));
            }
            this.userRepository.save(user);
        }
    }

    @Override
    public void increaseAll() {
        Iterable<LumiosUser> list = userRepository.findAll();
        for (LumiosUser user : list) {
            user.setSustainable((int) (user.getSustainable() + Math.sqrt(user.getBalance())));
            user.setBalance(0);
            this.userRepository.save(user);
        }
    }

    @Override
    public List<LumiosUser> findById(long id) {
        return this.userRepository.findByUserId(id);
    }

    @Override
    public LumiosUser findByUsername(String username, LumiosChat lumiosChat) {
        return this.userRepository.findReverenceUserByUsernameAndChat(username, lumiosChat);
    }

    @Override
    public List<LumiosUser> findAll(LumiosChat lumiosChat) {
        return this.userRepository.findAllByChat(lumiosChat);
    }

    @Override
    public void save(LumiosUser lumiosUser) {
        this.userRepository.save(lumiosUser);
    }

    @Override
    public void delete(LumiosUser lumiosUser) {
        this.userRepository.delete(lumiosUser);
    }

    @Override
    public boolean checkIfUserExists(long userId, LumiosChat lumiosChat) {
        Optional<LumiosUser> reverenceUser = this.userRepository.findReverenceUserByUserIdAndChat(userId, lumiosChat);
        return reverenceUser.isPresent();
    }

    @Override
    public boolean checkIfMentionedUserExists(String username, LumiosChat lumiosChat) {
        LumiosUser lumiosUser = userRepository.findReverenceUserByUsernameAndChat(username, lumiosChat);
        return lumiosUser != null;
    }
}

