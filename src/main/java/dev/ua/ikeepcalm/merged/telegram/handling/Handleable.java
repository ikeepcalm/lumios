/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.telegram.telegrambots.meta.api.objects.Update
 */
package dev.ua.ikeepcalm.merged.telegram.handling;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface Handleable {
    public void manage(Update var1);

    public boolean supports(Update var1);
}

