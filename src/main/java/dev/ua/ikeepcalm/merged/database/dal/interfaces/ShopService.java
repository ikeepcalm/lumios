package dev.ua.ikeepcalm.merged.database.dal.interfaces;

import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ShoppingUser;

public interface ShopService {
    ShoppingUser find(long userId, ReverenceChat var3);

    void save(ShoppingUser var1);

    void delete(ShoppingUser var1);
}

