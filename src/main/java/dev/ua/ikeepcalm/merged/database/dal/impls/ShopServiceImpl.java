/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Service
 */
package dev.ua.ikeepcalm.merged.database.dal.impls;

import dev.ua.ikeepcalm.merged.database.dal.interfaces.ShopService;
import dev.ua.ikeepcalm.merged.database.dal.repositories.ShopRepository;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ShoppingUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShopServiceImpl
implements ShopService {

    private final ShopRepository shopRepository;

    public ShopServiceImpl(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    @Override
    public ShoppingUser find(long userId, ReverenceChat reverenceChat) {
        return this.shopRepository.findShoppingUserByUserIdAndChannel(userId, reverenceChat);
    }

    @Override
    public void save(ShoppingUser shoppingUser) {
        this.shopRepository.save(shoppingUser);
    }

    @Override
    public void delete(ShoppingUser shoppingUser) {
        this.shopRepository.delete(shoppingUser);
    }
}

