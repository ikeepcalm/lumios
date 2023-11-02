package dev.ua.ikeepcalm.merged.telegram.modules.reverence.patterns;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ReverencePatterns {

    static boolean isIncreasingUpdate(Update update) {
        Pattern pattern = Pattern.compile("^\\+[1-9]\\d*");
        Matcher matcher = pattern.matcher(update.getMessage().getText());
        return matcher.matches();
    }

    static boolean isDecreasingUpdate(Update update) {
        Pattern pattern = Pattern.compile("^\\-[1-9]\\d*");
        Matcher matcher = pattern.matcher(update.getMessage().getText());
        return matcher.matches();
    }

    static boolean isIncreasingCommand(Message origin) {
        Pattern commandPattern = Pattern.compile("/increase@queueupnow_bot @[a-zA-Z0-9]+ \\d+");
        Pattern aliasPattern = Pattern.compile("/increase @[a-zA-Z0-9]+ \\d+");
        Matcher commandMatcher = commandPattern.matcher(origin.getText());
        Matcher aliasMatcher = aliasPattern.matcher(origin.getText());
        return commandMatcher.matches() || aliasMatcher.matches();
    }

    static boolean isDecreaseCommand(Message origin) {
        Pattern commandPattern = Pattern.compile("/decrease@queueupnow_bot @[a-zA-Z0-9]+ \\d+");
        Pattern aliasPattern = Pattern.compile("/decrease @[a-zA-Z0-9]+ \\d+");
        Matcher commandMatcher = commandPattern.matcher(origin.getText());
        Matcher aliasMatcher = aliasPattern.matcher(origin.getText());
        return commandMatcher.matches() || aliasMatcher.matches();
    }

}
