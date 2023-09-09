/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.telegram.telegrambots.meta.api.objects.Update
 */
package dev.ua.ikeepcalm.merged.patterns.UpdatePatterns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface DecreasingUpdate {
    public static boolean isDecreasingUpdate(Update update) {
        Pattern pattern = Pattern.compile("^\\-[1-9]\\d*");
        Matcher matcher = pattern.matcher(update.getMessage().getText());
        return matcher.matches();
    }
}

