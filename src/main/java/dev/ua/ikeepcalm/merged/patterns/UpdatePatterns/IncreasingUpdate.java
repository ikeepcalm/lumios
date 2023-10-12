/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.telegram.telegrambots.meta.api.objects.Update
 */
package dev.ua.ikeepcalm.merged.patterns.UpdatePatterns;

import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface IncreasingUpdate {
    public static boolean isAdditionUpdate(Update update) {
        Pattern pattern = Pattern.compile("^\\+[1-9]\\d*");
        Matcher matcher = pattern.matcher(update.getMessage().getText());
        return matcher.matches();
    }
}

