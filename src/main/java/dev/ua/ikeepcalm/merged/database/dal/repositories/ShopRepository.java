/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.data.repository.CrudRepository
 *  org.springframework.stereotype.Repository
 */
package dev.ua.ikeepcalm.merged.database.dal.repositories;

import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ShoppingUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopRepository
extends CrudRepository<ShoppingUser, Long> {
    ShoppingUser findShoppingUserByUserIdAndChannel(long var1, ReverenceChat var3);
}

