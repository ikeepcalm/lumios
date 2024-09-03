package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.reverence;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@BotCommand(command = "me")
public class MeCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update message, LumiosUser user, LumiosChat chat) {
        if (user.getReverence() < 0) {
            sendMessage("```Власна-статистика" +
                        " ◈ Рейтинг: " + user.getReverence() + "\n\n"
                        + "Допоки ви знаходитесь у від'ємному секторі поваги, для вас недоступний певний функціонал!```\n", ParseMode.MARKDOWN
                    , message.getMessage());
        } else {
            sendMessage("```Власна-статистика" +
                        " ◈ Рейтинг: " + user.getReverence() + "\n" +
                        " ◈ Кредити: " + user.getCredits() + "\n" +
                        " ◈ Оновлення: " + user.getSustainable() + "```\n", ParseMode.MARKDOWN, message.getMessage());
        }
    }
}