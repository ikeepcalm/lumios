package dev.ua.ikeepcalm.merged.telegram.utils;

import dev.ua.ikeepcalm.merged.telegram.AbsSender;
import dev.ua.ikeepcalm.merged.telegram.wrappers.TextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InteractiveRunnerUtil {

    private final AbsSender absSender;

    @Autowired
    public InteractiveRunnerUtil(AbsSender absSender) {
        this.absSender = absSender;
    }

    public void sayCommand(String argument){
        TextMessage textMessage = new TextMessage();
        textMessage.setText(argument);
        textMessage.setChatId(-1001767321866L);
        absSender.sendTextMessage(textMessage);
    }

}
