package dev.ua.ikeepcalm.merged.telegram.modules.reverence.patterns.updates;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface DecreasingUpdate {
    static boolean isDecreasingUpdate(Update update) {
        Pattern pattern = Pattern.compile("^\\-[1-9]\\d*");
        Matcher matcher = pattern.matcher(update.getMessage().getText());
        return matcher.matches();
    }
}

