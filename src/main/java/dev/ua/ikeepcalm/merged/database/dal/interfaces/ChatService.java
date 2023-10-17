/*
 * Decompiled with CFR 0.150.
 */
package dev.ua.ikeepcalm.merged.database.dal.interfaces;

import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;

public interface ChatService {
    ReverenceChat find(long var1);

    void save(ReverenceChat var1);

    void delete(ReverenceChat var1);
}

