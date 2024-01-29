package dev.ua.ikeepcalm.queueupnow.telegram.modules.reverence.commands;

import dev.ua.ikeepcalm.queueupnow.telegram.modules.CommandParent;
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
                " ◈ Оновлення: " + reverenceUser.getSustainable() + "\n" +
                " ◈ Гаманець: " + reverenceUser.getBalance() + "```", ParseMode.MARKDOWN);
    }
}