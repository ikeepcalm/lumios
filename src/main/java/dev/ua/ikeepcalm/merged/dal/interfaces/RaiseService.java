/*
 * Decompiled with CFR 0.150.
 */
package dev.ua.ikeepcalm.merged.dal.interfaces;

import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.entities.reverence.ShoppingUser;

public interface RaiseService {
    public ShoppingUser find(long var1, ReverenceChat var3);

    public void save(ShoppingUser var1);

    public void delete(ShoppingUser var1);
}

