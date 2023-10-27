package dev.ua.ikeepcalm.merged.telegram.modules.reverence.patterns.commands;

import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface IncreasingCommand {
    static boolean isIncreasingCommand(Message origin) {
        Pattern commandPattern = Pattern.compile("/increase@queueupnow_bot @[a-zA-Z0-9]+ \\d+");
        Pattern aliasPattern = Pattern.compile("/increase @[a-zA-Z0-9]+ \\d+");
        Matcher commandMatcher = commandPattern.matcher(origin.getText());
        Matcher aliasMatcher = aliasPattern.matcher(origin.getText());
        return commandMatcher.matches() || aliasMatcher.matches();
    }
}

