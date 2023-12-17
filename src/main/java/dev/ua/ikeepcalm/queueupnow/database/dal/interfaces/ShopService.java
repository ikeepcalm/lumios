package dev.ua.ikeepcalm.queue.database.dal.interfaces;

import dev.ua.ikeepcalm.queue.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.queue.database.entities.reverence.ShoppingUser;

public interface ShopService {
    ShoppingUser find(long userId, ReverenceChat var3);

    void save(ShoppingUser var1);

    void delete(ShoppingUser var1);
}

