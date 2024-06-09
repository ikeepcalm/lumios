package dev.ua.ikeepcalm.lumios.database.dal.impls;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.reverence.UserRepository;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceUser;
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
    public ReverenceUser findById(long userId, ReverenceChat reverenceChat) throws NoSuchEntityException {
        Optional<ReverenceUser> reverenceUser = this.userRepository.findReverenceUserByUserIdAndChat(userId, reverenceChat);
        if (reverenceUser.isPresent()) {
            return reverenceUser.get();
        } else {
            throw new NoSuchEntityException("No such user with id: " + userId);
        }
    }

    @Override
    public void updateAll() {
        Iterable<ReverenceUser> list = userRepository.findAll();
        for (ReverenceUser user : list) {
            user.setCredits(user.getSustainable());
            if (user.getReverence() > 0) {
                user.setBalance((int) (user.getBalance() + Math.sqrt(user.getReverence())));
            }
            this.userRepository.save(user);
        }
    }

    @Override
    public void increaseAll() {
        Iterable<ReverenceUser> list = userRepository.findAll();
        for (ReverenceUser user : list) {
            user.setSustainable((int) (user.getSustainable() + Math.sqrt(user.getBalance())));
            user.setBalance(0);
            this.userRepository.save(user);
        }
    }

    @Override
    public List<ReverenceUser> findById(long id) {
        return this.userRepository.findByUserId(id);
    }

    @Override
    public ReverenceUser findByUsername(String username, ReverenceChat reverenceChat) {
        return this.userRepository.findReverenceUserByUsernameAndChat(username, reverenceChat);
    }

    @Override
    public List<ReverenceUser> findAll(ReverenceChat reverenceChat) {
        return this.userRepository.findAllByChat(reverenceChat);
    }

    @Override
    public void save(ReverenceUser reverenceUser) {
        this.userRepository.save(reverenceUser);
    }

    @Override
    public void delete(ReverenceUser reverenceUser) {
        this.userRepository.delete(reverenceUser);
    }

    @Override
    public boolean checkIfUserExists(long userId, ReverenceChat reverenceChat) {
        Optional<ReverenceUser> reverenceUser = this.userRepository.findReverenceUserByUserIdAndChat(userId, reverenceChat);
        return reverenceUser.isPresent();
    }

    @Override
    public boolean checkIfMentionedUserExists(String username, ReverenceChat reverenceChat) {
        ReverenceUser reverenceUser = userRepository.findReverenceUserByUsernameAndChat(username, reverenceChat);
        return reverenceUser != null;
    }
}

