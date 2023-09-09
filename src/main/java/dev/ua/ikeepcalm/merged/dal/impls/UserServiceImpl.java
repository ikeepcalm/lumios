/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Service
 */
package dev.ua.ikeepcalm.merged.dal.impls;

import dev.ua.ikeepcalm.merged.dal.interfaces.UserService;
import dev.ua.ikeepcalm.merged.dal.repositories.UserRepository;
import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl
implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public ReverenceUser findById(long userId, ReverenceChat reverenceChat) {
        return this.userRepository.findReverenceUserByUserIdAndChannel(userId, reverenceChat);
    }

    @Override
    public void updateAll() {
        Iterable<ReverenceUser> list = userRepository.findAll();
        for (ReverenceUser user : list) {
            user.setCredits(user.getSustainable());
            if (user.getReverence() < 0) {
                if (-0.1 * (double)user.getReverence() > (double)user.getBalance()) {
                    user.setBalance(0);
                } else {
                    user.setBalance((int)((double)user.getBalance() + (double)user.getReverence() * 0.1));
                }
            } else {
                user.setBalance((int)((double)user.getBalance() + (double)user.getReverence() * 0.1));
            }
            this.userRepository.save(user);
        }
    }

    @Override
    public ReverenceUser findByUsername(String username, ReverenceChat reverenceChat) {
        return this.userRepository.findReverenceUserByUsernameAndChannel(username, reverenceChat);
    }

    @Override
    public List<ReverenceUser> findAll(ReverenceChat reverenceChat) {
        return this.userRepository.findAllByChannel(reverenceChat);
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
        ReverenceUser reverenceUser = this.userRepository.findReverenceUserByUserIdAndChannel(userId, reverenceChat);
        return reverenceUser != null;
    }

    @Override
    public boolean checkIfMentionedUserExists(String username, ReverenceChat reverenceChat) {
        ReverenceUser reverenceUser = this.userRepository.findReverenceUserByUsernameAndChannel(username, reverenceChat);
        return reverenceUser != null;
    }

    @Autowired
    private void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}

