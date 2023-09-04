package dev.ua.ikeepcalm.queueupnow.telegram.handling.handlers;

import dev.ua.ikeepcalm.queueupnow.telegram.executing.commands.QueueCommand;
import dev.ua.ikeepcalm.queueupnow.telegram.handling.Handleable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class CommandHandler implements Handleable {

    @Autowired
    private QueueCommand queueCommand;


    @Override
    public void manage(Update update) {
        Message origin = update.getMessage();
        if (origin.getText().startsWith("/queue")) {
            queueCommand.execute(origin);
        }
    }

    @Override
    public boolean supports(Update update) {
        if (update.getMessage() != null){
            if (update.getMessage().getText() !=null){
                return update.getMessage().getText().startsWith("/");
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
