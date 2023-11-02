package dev.ua.ikeepcalm.merged.telegram.modules.reverence.commands;

import dev.ua.ikeepcalm.merged.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class StartCommand extends CommandParent {

    private final ChatService chatService;

    public StartCommand(ChatService chatService) {
        this.chatService = chatService;
    }

    public void execute(Message origin) {
        long chatId = origin.getChatId();
        ReverenceChat chat = chatService.find(chatId);

        if (chat == null) {
            registerChat(chatId);
            reply(origin, "Вітаємо! Цей чат успішно зареєстровано в системі. Приємного користування!");
        } else {
            reply(origin, "Цей чат вже зареєстровано в системі. Немає необхідності робити це ще раз!");
        }
    }

    private void registerChat(long chatId) {
        ReverenceChat chat = new ReverenceChat();
        chat.setChatId(chatId);
        chatService.save(chat);
    }
}
