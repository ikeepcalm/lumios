package dev.ua.ikeepcalm.lumios.telegram.interactions.callbacks;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCallback;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.DebugUtil;
import dev.ua.ikeepcalm.lumios.telegram.utils.PagedUtil;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@BotCallback(startsWith = "debug#")
public class DebugCallback extends ServicesShortcut implements Interaction {


    @Override
    public void fireInteraction(CallbackQuery message, LumiosUser user, LumiosChat chat) {
        String[] split = message.getData().split("#");
        if (split.length != 3) {
            telegramClient.sendAnswerCallbackQuery("Помилка! Невірний формат!", message.getId());
            return;
        }

        if (!message.getFrom().getUserName().equals("ikeepcalm")) {
            telegramClient.sendAnswerCallbackQuery("Помилка! Ти жінка!", message.getId());
        }

        String action = split[1];
        switch (action) {
            case "group" -> groupDebug(message, split[2]);
            case "back" -> backDebug(message);
            case "notify" -> notifyDebug(message, split[2]);
            case "stats" -> statsDebug(message, split[2]);
            default -> telegramClient.sendAnswerCallbackQuery("Помилка! Невірний формат!", message.getId());
        }
    }

    private void statsDebug(CallbackQuery message, String chatId) {
        LumiosChat chat;
        try {
            chat = chatService.findByChatId(Long.parseLong(chatId));
        } catch (NoSuchEntityException e) {
            telegramClient.sendAnswerCallbackQuery("Помилка! Не знайдено чат з таким ID!", message.getId());
            return;
        }

        List<LumiosUser> users = userService.findAll(chat);
        EditMessage textMessage = new EditMessage();
        textMessage.setChatId(message.getMessage().getChatId());
        textMessage.setMessageId(message.getMessage().getMessageId());
        textMessage.setText(PagedUtil.buildStatsMessage(users, 1));
        textMessage.setParseMode(ParseMode.MARKDOWN);

        if (users.size() > 10) {
            int maxPage;
            if (users.size() % 10 == 0) {
                maxPage = users.size() / 10;
            } else {
                maxPage = users.size() / 10 + 1;
            }
            textMessage.setReplyKeyboard(PagedUtil.buildStatsKeyboard(1, maxPage, true, '#', Long.parseLong(chatId)));
        }

        editMessage(textMessage);

        telegramClient.sendAnswerCallbackQuery("Статистика відправлена!", message.getId());
    }

    private void notifyDebug(CallbackQuery message, String chatId) {
        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(Long.parseLong(chatId));
        textMessage.setText("\uD83D\uDC40");
        sendMessage(textMessage, (Message) message.getMessage());
        telegramClient.sendAnswerCallbackQuery("Сповіщення відправлено!", message.getId());
    }

    private void backDebug(CallbackQuery message) {
        Iterable<LumiosChat> chats = chatService.findAll();
        Map<String, String> groups = new HashMap<>();
        for (LumiosChat lumiosChat : chats) {
            String name = lumiosChat.getName();
            if (name == null) {
                name = "Unnamed";
            }
            String id = lumiosChat.getChatId().toString();
            if (id.startsWith("-")) {
                groups.put(id, name);
            }
        }

        EditMessage textMessage = new EditMessage();
        textMessage.setChatId(message.getMessage().getChatId());
        textMessage.setMessageId(message.getMessage().getMessageId());
        textMessage.setText("<< DEBUG MENU >>");
        textMessage.setReplyKeyboard(DebugUtil.createGroupsKeyboard(groups));
        editMessage(textMessage);
    }

    private void groupDebug(CallbackQuery message, String chatId) {
        LumiosChat lumiosChat;
        try {
            lumiosChat = chatService.findByChatId(Long.parseLong(chatId));
        } catch (NoSuchEntityException e) {
            telegramClient.sendAnswerCallbackQuery("Помилка! Не знайдено чат з таким ID!", message.getId());
            return;
        }

        int overallMessages = recordService.countAllByChatId(Long.valueOf(chatId));
        int overallReverence = 0;
        for (LumiosUser lumiosUser : lumiosChat.getUsers()) {
            overallReverence += lumiosUser.getReverence();
        }

        EditMessage textMessage = new EditMessage();
        textMessage.setChatId(message.getMessage().getChatId());
        textMessage.setMessageId(message.getMessage().getMessageId());
        textMessage.setParseMode(ParseMode.MARKDOWN);
        textMessage.setText("""
                << CHAT DEBUG  >> \n
                ```GENERAL-INFO
                ID: %s
                Name: %s
                Description: %s
                Last wheel: %s```
                                
                ```RECORDED-COUNTS                
                Overall messages: %s
                Overall reverence: %s
                ```
                """.formatted(
                lumiosChat.getChatId(),
                lumiosChat.getName() != null ? lumiosChat.getName() : "Not set",
                lumiosChat.getDescription() != null ? lumiosChat.getDescription() : "Not set",
                lumiosChat.getLastWheelDate() != null ? lumiosChat.getLastWheelDate().toLocalDate() : "Not set",
                overallMessages,
                overallReverence));
        textMessage.setReplyKeyboard(DebugUtil.createGroupKeyboard(chatId));
        editMessage(textMessage);
    }
}
