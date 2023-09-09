/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Service
 */
package dev.ua.ikeepcalm.merged.dal.impls;

import dev.ua.ikeepcalm.merged.dal.interfaces.RaiseService;
import dev.ua.ikeepcalm.merged.dal.repositories.RaiseRepository;
import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.entities.reverence.ShoppingUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RaiseServiceImpl
implements RaiseService {
    @Autowired
    private RaiseRepository raiseRepository;

    @Override
    public ShoppingUser find(long userId, ReverenceChat reverenceChat) {
        return this.raiseRepository.findShoppingUserByUserIdAndChannel(userId, reverenceChat);
    }

    @Override
    public void save(ShoppingUser shoppingUser) {
        this.raiseRepository.save(shoppingUser);
    }

    @Override
    public void delete(ShoppingUser shoppingUser) {
        this.raiseRepository.delete(shoppingUser);
    }
}

