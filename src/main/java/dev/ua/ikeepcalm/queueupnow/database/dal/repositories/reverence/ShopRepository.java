package dev.ua.ikeepcalm.queueupnow.database.dal.repositories.reverence;

import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ShoppingUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopRepository
        extends CrudRepository<ShoppingUser, Long> {
    ShoppingUser findShoppingUserByUserIdAndChannel(long var1, ReverenceChat var3);
}

