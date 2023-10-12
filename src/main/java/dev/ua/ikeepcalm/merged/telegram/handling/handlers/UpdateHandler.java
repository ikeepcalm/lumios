/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.Update
 */
package dev.ua.ikeepcalm.merged.telegram.handling.handlers;

import dev.ua.ikeepcalm.merged.patterns.UpdatePatterns.IncreasingUpdate;
import dev.ua.ikeepcalm.merged.telegram.executing.updates.DecreasingUpdate;
import dev.ua.ikeepcalm.merged.telegram.handling.Handleable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class UpdateHandler
implements Handleable {
    @Autowired
    private DecreasingUpdate decreasingUpdate;
    @Autowired
    private dev.ua.ikeepcalm.merged.telegram.executing.updates.AdditionUpdate additionUpdate;

    @Override
    public void manage(Update update) {
        if (!update.hasCallbackQuery()) {
            if (dev.ua.ikeepcalm.merged.patterns.UpdatePatterns.DecreasingUpdate.isDecreasingUpdate(update)) {
                this.decreasingUpdate.execute(update);
            } else if (IncreasingUpdate.isAdditionUpdate(update)) {
                this.additionUpdate.execute(update);
            }
        }
    }

    @Override
    public boolean supports(Update update) {
        if (!update.hasCallbackQuery() && update.getMessage() != null && update.getMessage().getText() != null) {
            return update.getMessage().isReply();
        }
        return false;
    }
}

