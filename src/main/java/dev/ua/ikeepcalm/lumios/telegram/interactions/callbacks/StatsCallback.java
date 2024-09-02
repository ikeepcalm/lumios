package dev.ua.ikeepcalm.lumios.telegram.interactions.callbacks;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.TelegramClient;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCallback;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.PagedUtil;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.RemoveMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
@BotCallback(startsWith = "stats")
public class StatsCallback extends ServicesShortcut implements Interaction {

    private final TelegramClient telegramClient;

    public StatsCallback(TelegramClient telegramClient) {
        super();
        this.telegramClient = telegramClient;
    }

    @Override
    public void fireInteraction(CallbackQuery message, LumiosUser user, LumiosChat chat) {
        String data = message.getData();

        if (data.charAt(5) == '-') {
            defaultStats(message, chat, data);
        } else if (data.charAt(5) == '#') {
            debugStats(message, data);
        }
    }

    private void defaultStats(CallbackQuery message, LumiosChat chat, String data) {
        List<LumiosUser> users = userService.findAll(chat);
        String[] split = data.split("-");
        int page = Integer.parseInt(split[1]);
        int maxPage;
        if (users.size() % 10 == 0) {
            maxPage = users.size() / 10;
        } else {
            maxPage = users.size() / 10 + 1;
        }
        String direction = split[2];

        if (direction.equals("forward")) {
            page++;
        } else if (direction.equals("back")) {
            page--;
        }

        EditMessage editedMessage = new EditMessage();
        editedMessage.setChatId(message.getMessage().getChatId());
        editedMessage.setMessageId(message.getMessage().getMessageId());
        editedMessage.setParseMode(ParseMode.MARKDOWN);
        editedMessage.setText(PagedUtil.buildStatsMessage(users, page));
        editedMessage.setReplyKeyboard(PagedUtil.buildStatsKeyboard(page, maxPage, false, '-', chat.getChatId()));
        editMessage(editedMessage);
    }

    private void debugStats(CallbackQuery message, String data) {
        String[] split = data.split("#");
        long chatId = Long.parseLong(split[3]);
        LumiosChat chat;
        try {
            chat = chatService.findByChatId(chatId);
        } catch (Exception e) {
            telegramClient.sendAnswerCallbackQuery("Помилка! Не знайдено чат з таким ID!", message.getId());
            return;
        }

        List<LumiosUser> users = userService.findAll(chat);
        int page = Integer.parseInt(split[1]);
        int maxPage;
        if (users.size() % 10 == 0) {
            maxPage = users.size() / 10;
        } else {
            maxPage = users.size() / 10 + 1;
        }
        String direction = split[2];

        switch (direction) {
            case "forward" -> page++;
            case "back" -> page--;
            case "exit" -> {
                RemoveMessage removeMessage = new RemoveMessage();
                removeMessage.setChatId(message.getMessage().getChatId());
                removeMessage.setMessageId(message.getMessage().getMessageId());
                try {
                    telegramClient.sendRemoveMessage(removeMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return;
            }
        }

        EditMessage editedMessage = new EditMessage();
        editedMessage.setChatId(message.getMessage().getChatId());
        editedMessage.setMessageId(message.getMessage().getMessageId());
        editedMessage.setParseMode(ParseMode.MARKDOWN);
        editedMessage.setText(PagedUtil.buildStatsMessage(users, page));
        editedMessage.setReplyKeyboard(PagedUtil.buildStatsKeyboard(page, maxPage, true, '#', chatId));
        editMessage(editedMessage);
    }

}
