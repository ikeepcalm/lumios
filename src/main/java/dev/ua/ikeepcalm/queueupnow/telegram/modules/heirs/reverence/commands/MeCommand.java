package dev.ua.ikeepcalm.queueupnow.telegram.modules.heirs.reverence.commands;

import dev.ua.ikeepcalm.queueupnow.telegram.modules.parents.CommandParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class MeCommand extends CommandParent {

    @Override
    public void processUpdate(Message message) {
        instantiateUpdate(message);
        sendMessage("```Власна-статистика" +
                " ◈ Рейтинг: " + reverenceUser.getReverence() + "\n" +
                " ◈ Кредити: " + reverenceUser.getCredits() + "\n" +
                " ◈ Оновлення: " + reverenceUser.getSustainable() + "```\n", ParseMode.MARKDOWN);
    }
}