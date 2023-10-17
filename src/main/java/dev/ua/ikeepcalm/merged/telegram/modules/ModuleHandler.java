/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.telegram.telegrambots.meta.api.objects.Update
 */
package dev.ua.ikeepcalm.merged.telegram.modules;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface ModuleHandler {
    void dispatchUpdate(Update update);
    default boolean supports(Update update){
        if (update.hasMessage()) {
            if (update.getMessage().hasText()){
                return update.getMessage().getText().startsWith("/");
            } else {
                return false;
            }
        } else {
            return update.hasCallbackQuery();
        }
    }
}

