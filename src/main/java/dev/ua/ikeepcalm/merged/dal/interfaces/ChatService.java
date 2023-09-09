/*
 * Decompiled with CFR 0.150.
 */
package dev.ua.ikeepcalm.merged.dal.interfaces;

import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceChat;

public interface ChatService {
    public ReverenceChat find(long var1);

    public void save(ReverenceChat var1);

    public void delete(ReverenceChat var1);
}

