package dev.ua.ikeepcalm.lumios.telegram.modules.impl.reverence.commands;

import dev.ua.ikeepcalm.lumios.telegram.modules.parents.CommandParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
public class MeCommand extends CommandParent {

    @Override
    public void processUpdate(Message message) {
        sendMessage("```Власна-статистика" +
                " ◈ Рейтинг: " + reverenceUser.getReverence() + "\n" +
                " ◈ Кредити: " + reverenceUser.getCredits() + "\n" +
                " ◈ Оновлення: " + reverenceUser.getSustainable() + "```\n", ParseMode.MARKDOWN);
    }
}