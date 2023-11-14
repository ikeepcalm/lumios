package dev.ua.ikeepcalm.merged.telegram.modules.reverence.patterns;

import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ReverencePatterns {

    static boolean isIncreasingUpdate(Message message) {
        Pattern pattern = Pattern.compile("^\\+[1-9]\\d*");
        Matcher matcher = pattern.matcher(message.getText());
        return matcher.matches();
    }

    static boolean isDecreasingUpdate(Message message) {
        Pattern pattern = Pattern.compile("^\\-[1-9]\\d*");
        Matcher matcher = pattern.matcher(message.getText());
        return matcher.matches();
    }

    static boolean isIncreaseCommand(Message message) {
        Pattern commandPattern = Pattern.compile("/increase@queueupnow_bot @[a-zA-Z0-9_]+ \\d+");
        Pattern aliasPattern = Pattern.compile("/increase @[a-zA-Z0-9_]+ \\d+");
        Matcher commandMatcher = commandPattern.matcher(message.getText());
        Matcher aliasMatcher = aliasPattern.matcher(message.getText());
        return commandMatcher.matches() || aliasMatcher.matches();
    }

    static boolean isDecreaseCommand(Message message) {
        Pattern commandPattern = Pattern.compile("/decrease@queueupnow_bot @[a-zA-Z0-9_]+ \\d+");
        Pattern aliasPattern = Pattern.compile("/decrease @[a-zA-Z0-9_]+ \\d+");
        Matcher commandMatcher = commandPattern.matcher(message.getText());
        Matcher aliasMatcher = aliasPattern.matcher(message.getText());
        return commandMatcher.matches() || aliasMatcher.matches();
    }

}
