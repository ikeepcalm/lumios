/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.telegram.telegrambots.meta.api.objects.Message
 */
package dev.ua.ikeepcalm.merged.patterns.CommandPatterns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface AdditionCommand {
    public static boolean isAddCommand(Message origin) {
        Pattern commandPattern = Pattern.compile("/add@queueupnow_bot @[a-zA-Z0-9]+ \\d+");
        Pattern aliasPattern = Pattern.compile("/add @[a-zA-Z0-9]+ \\d+");
        Matcher commandMatcher = commandPattern.matcher(origin.getText());
        Matcher aliasMatcher = aliasPattern.matcher(origin.getText());
        return commandMatcher.matches() || aliasMatcher.matches();
    }
}
