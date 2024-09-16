package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.tasks;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.markup.TaskMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@BotCommand(command = "task", aliases = {"tasks"})
public class TaskCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(update.getMessage().getChatId());
        textMessage.setText("""
                *Меню завдань*
                
                В цьому меню ви можете створити нове завдання, відредагувати або видалити існуюче, а також перейти до веб-порталу для управління завданнями. Навігація здійснюється за допомогою вбудованої клавіатурою під цим повідомленням.
                """);
        textMessage.setParseMode(ParseMode.MARKDOWN);
        textMessage.setReplyKeyboard(TaskMarkupUtil.getMenuMarkup());
        sendMessage(textMessage, update.getMessage());
    }
}
