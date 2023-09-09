/*
 * Decompiled with CFR 0.150.
 */
package dev.ua.ikeepcalm.merged.dal.interfaces;

import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceUser;
import java.util.List;

public interface UserService {
    public ReverenceUser findById(long var1, ReverenceChat var3);

    public void updateAll();

    public ReverenceUser findByUsername(String var1, ReverenceChat var2);

    public List<ReverenceUser> findAll(ReverenceChat var1);

    public void save(ReverenceUser var1);

    public void delete(ReverenceUser var1);

    public boolean checkIfUserExists(long var1, ReverenceChat var3);

    public boolean checkIfMentionedUserExists(String var1, ReverenceChat var2);
}

