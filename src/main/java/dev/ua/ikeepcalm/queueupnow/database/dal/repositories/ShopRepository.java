package dev.ua.ikeepcalm.queue.database.dal.repositories;

import dev.ua.ikeepcalm.queue.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.queue.database.entities.reverence.ShoppingUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopRepository
        extends CrudRepository<ShoppingUser, Long> {
    ShoppingUser findShoppingUserByUserIdAndChannel(long var1, ReverenceChat var3);
}

