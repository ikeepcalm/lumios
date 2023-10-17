/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.data.repository.CrudRepository
 *  org.springframework.stereotype.Repository
 */
package dev.ua.ikeepcalm.merged.database.dal.repositories;

import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceUser;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository
extends CrudRepository<ReverenceUser, Long> {
    ReverenceUser findReverenceUserByUserIdAndChannel(long var1, ReverenceChat var3);

    ReverenceUser findReverenceUserByUsernameAndChannel(String var1, ReverenceChat var2);

    List<ReverenceUser> findAllByChannel(ReverenceChat var1);
}

