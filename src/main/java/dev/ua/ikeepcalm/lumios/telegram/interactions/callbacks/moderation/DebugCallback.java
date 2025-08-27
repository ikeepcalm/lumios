package dev.ua.ikeepcalm.lumios.telegram.interactions.callbacks.moderation;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@BotCallback(startsWith = "debug#")
public class DebugCallback extends ServicesShortcut implements Interaction {


    @Override
    public void fireInteraction(CallbackQuery message, LumiosUser user, LumiosChat chat) {
        String[] split = message.getData().split("#");
        if (split.length < 3) {
            telegramClient.sendAnswerCallbackQuery("Помилка! Невірний формат!", message.getId());
            return;
        }

        if (!message.getFrom().getUserName().equals("ikeepcalm")) {
            telegramClient.sendAnswerCallbackQuery("Помилка! Ти жінка!", message.getId());
            return;
        }

        String action = split[1];
        switch (action) {
            case "group" -> groupDebug(message, split[2]);
            case "back" -> backDebug(message);
            case "notify" -> notifyDebug(message, split[2]);
            case "stats" -> statsDebug(message, split[2]);
            case "users" -> usersDebug(message, split[2]);
            case "user" -> userManagementDebug(message, split[2], split[3]);
            case "setrev" -> setReverenceDebug(message, split[2], split[3], split[4]);
            case "resetrev" -> resetReverenceDebug(message, split[2], split[3]);
            case "settings" -> settingsDebug(message, split[2]);
            case "summary" -> summaryLimitDebug(message, split[2]);
            case "comm" -> commLimitDebug(message, split[2]);
            case "toggleai" -> toggleAiDebug(message, split[2]);
            case "limit" -> adjustLimitDebug(message, split[2], split[3], split[4]);
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
                
                ```AI-SETTINGS
                AI Enabled: %s
                Summary Limit: %s
                Communication Limit: %s
                ```
                """.formatted(
                lumiosChat.getChatId(),
                lumiosChat.getName() != null ? lumiosChat.getName() : "Not set",
                lumiosChat.getDescription() != null ? lumiosChat.getDescription() : "Not set",
                lumiosChat.getLastWheelDate() != null ? lumiosChat.getLastWheelDate().toLocalDate() : "Not set",
                overallMessages,
                overallReverence,
                lumiosChat.isAiEnabled() ? "Yes" : "No",
                lumiosChat.getSummaryLimit(),
                lumiosChat.getCommunicationLimit()));
        textMessage.setReplyKeyboard(DebugUtil.createGroupKeyboard(chatId));
        editMessage(textMessage);
    }

    private void usersDebug(CallbackQuery message, String chatId) {
        LumiosChat lumiosChat;
        try {
            lumiosChat = chatService.findByChatId(Long.parseLong(chatId));
        } catch (NoSuchEntityException e) {
            telegramClient.sendAnswerCallbackQuery("Помилка! Не знайдено чат з таким ID!", message.getId());
            return;
        }

        Map<Long, String> users = new LinkedHashMap<>();
        for (LumiosUser lumiosUser : lumiosChat.getUsers()) {
            String displayName = lumiosUser.getFullName() != null ? lumiosUser.getFullName() : 
                               (lumiosUser.getUsername() != null ? lumiosUser.getUsername() : "Unknown");
            users.put(lumiosUser.getUserId(), displayName + " (" + lumiosUser.getReverence() + ")");
        }

        EditMessage textMessage = new EditMessage();
        textMessage.setChatId(message.getMessage().getChatId());
        textMessage.setMessageId(message.getMessage().getMessageId());
        textMessage.setText("<< USER MANAGEMENT >>\n\nSelect a user to manage:");
        textMessage.setReplyKeyboard(DebugUtil.createUsersKeyboard(users, chatId));
        editMessage(textMessage);
    }

    private void userManagementDebug(CallbackQuery message, String chatId, String userId) {
        LumiosUser lumiosUser;
        LumiosChat lumiosChat;
        try {
            lumiosChat = chatService.findByChatId(Long.parseLong(chatId));
            lumiosUser = userService.findById(Long.parseLong(userId), lumiosChat);
        } catch (NoSuchEntityException e) {
            telegramClient.sendAnswerCallbackQuery("Помилка! Користувача не знайдено!", message.getId());
            return;
        }

        EditMessage textMessage = new EditMessage();
        textMessage.setChatId(message.getMessage().getChatId());
        textMessage.setMessageId(message.getMessage().getMessageId());
        textMessage.setParseMode(ParseMode.MARKDOWN);
        textMessage.setText("""
                << USER MANAGEMENT >>
                
                ```USER-INFO
                Name: %s
                Username: %s
                Current Reverence: %s
                Credits: %s
                Balance: %s
                ```
                """.formatted(
                lumiosUser.getFullName() != null ? lumiosUser.getFullName() : "Not set",
                lumiosUser.getUsername() != null ? lumiosUser.getUsername() : "Not set",
                lumiosUser.getReverence(),
                lumiosUser.getCredits(),
                lumiosUser.getBalance()));
        textMessage.setReplyKeyboard(DebugUtil.createUserManagementKeyboard(chatId, Long.parseLong(userId)));
        editMessage(textMessage);
    }

    private void setReverenceDebug(CallbackQuery message, String chatId, String userId, String amount) {
        try {
            LumiosChat lumiosChat = chatService.findByChatId(Long.parseLong(chatId));
            LumiosUser lumiosUser = userService.findById(Long.parseLong(userId), lumiosChat);
            int currentReverence = lumiosUser.getReverence();
            int change = Integer.parseInt(amount);
            lumiosUser.setReverence(currentReverence + change);
            userService.save(lumiosUser);
            
            telegramClient.sendAnswerCallbackQuery(
                String.format("Reverence updated! %d → %d", currentReverence, lumiosUser.getReverence()), 
                message.getId());
            
            userManagementDebug(message, chatId, userId);
        } catch (Exception e) {
            telegramClient.sendAnswerCallbackQuery("Помилка при оновленні reverence!", message.getId());
        }
    }

    private void resetReverenceDebug(CallbackQuery message, String chatId, String userId) {
        try {
            LumiosChat lumiosChat = chatService.findByChatId(Long.parseLong(chatId));
            LumiosUser lumiosUser = userService.findById(Long.parseLong(userId), lumiosChat);
            int oldReverence = lumiosUser.getReverence();
            lumiosUser.setReverence(0);
            userService.save(lumiosUser);
            
            telegramClient.sendAnswerCallbackQuery(
                String.format("Reverence reset! %d → 0", oldReverence), 
                message.getId());
            
            userManagementDebug(message, chatId, userId);
        } catch (Exception e) {
            telegramClient.sendAnswerCallbackQuery("Помилка при скиданні reverence!", message.getId());
        }
    }

    private void settingsDebug(CallbackQuery message, String chatId) {
        LumiosChat lumiosChat;
        try {
            lumiosChat = chatService.findByChatId(Long.parseLong(chatId));
        } catch (NoSuchEntityException e) {
            telegramClient.sendAnswerCallbackQuery("Помилка! Не знайдено чат з таким ID!", message.getId());
            return;
        }

        EditMessage textMessage = new EditMessage();
        textMessage.setChatId(message.getMessage().getChatId());
        textMessage.setMessageId(message.getMessage().getMessageId());
        textMessage.setParseMode(ParseMode.MARKDOWN);
        textMessage.setText("""
                << SETTINGS MANAGEMENT >>
                
                ```CURRENT-SETTINGS
                AI Enabled: %s
                Summary Limit: %s
                Communication Limit: %s
                ```
                
                Select a setting to modify:
                """.formatted(
                lumiosChat.isAiEnabled() ? "Yes" : "No",
                lumiosChat.getSummaryLimit(),
                lumiosChat.getCommunicationLimit()));
        textMessage.setReplyKeyboard(DebugUtil.createSettingsKeyboard(chatId));
        editMessage(textMessage);
    }

    private void summaryLimitDebug(CallbackQuery message, String chatId) {
        LumiosChat lumiosChat;
        try {
            lumiosChat = chatService.findByChatId(Long.parseLong(chatId));
        } catch (NoSuchEntityException e) {
            telegramClient.sendAnswerCallbackQuery("Помилка! Не знайдено чат з таким ID!", message.getId());
            return;
        }

        EditMessage textMessage = new EditMessage();
        textMessage.setChatId(message.getMessage().getChatId());
        textMessage.setMessageId(message.getMessage().getMessageId());
        textMessage.setText("<< SUMMARY LIMIT >>\n\nCurrent: " + lumiosChat.getSummaryLimit() + "\n\nSelect adjustment:");
        textMessage.setReplyKeyboard(DebugUtil.createLimitKeyboard(chatId, "summary"));
        editMessage(textMessage);
    }

    private void commLimitDebug(CallbackQuery message, String chatId) {
        LumiosChat lumiosChat;
        try {
            lumiosChat = chatService.findByChatId(Long.parseLong(chatId));
        } catch (NoSuchEntityException e) {
            telegramClient.sendAnswerCallbackQuery("Помилка! Не знайдено чат з таким ID!", message.getId());
            return;
        }

        EditMessage textMessage = new EditMessage();
        textMessage.setChatId(message.getMessage().getChatId());
        textMessage.setMessageId(message.getMessage().getMessageId());
        textMessage.setText("<< COMMUNICATION LIMIT >>\n\nCurrent: " + lumiosChat.getCommunicationLimit() + "\n\nSelect adjustment:");
        textMessage.setReplyKeyboard(DebugUtil.createLimitKeyboard(chatId, "comm"));
        editMessage(textMessage);
    }

    private void toggleAiDebug(CallbackQuery message, String chatId) {
        try {
            LumiosChat lumiosChat = chatService.findByChatId(Long.parseLong(chatId));
            boolean oldStatus = lumiosChat.isAiEnabled();
            lumiosChat.setAiEnabled(!oldStatus);
            chatService.save(lumiosChat);
            
            telegramClient.sendAnswerCallbackQuery(
                String.format("AI %s!", !oldStatus ? "enabled" : "disabled"), 
                message.getId());
            
            settingsDebug(message, chatId);
        } catch (Exception e) {
            telegramClient.sendAnswerCallbackQuery("Помилка при зміні AI статусу!", message.getId());
        }
    }

    private void adjustLimitDebug(CallbackQuery message, String chatId, String type, String change) {
        try {
            LumiosChat lumiosChat = chatService.findByChatId(Long.parseLong(chatId));
            int adjustment = Integer.parseInt(change);
            
            if ("summary".equals(type)) {
                int oldLimit = lumiosChat.getSummaryLimit();
                int newLimit = Math.max(0, oldLimit + adjustment);
                lumiosChat.setSummaryLimit(newLimit);
                chatService.save(lumiosChat);
                telegramClient.sendAnswerCallbackQuery(
                    String.format("Summary limit: %d → %d", oldLimit, newLimit), 
                    message.getId());
                summaryLimitDebug(message, chatId);
            } else if ("comm".equals(type)) {
                int oldLimit = lumiosChat.getCommunicationLimit();
                int newLimit = Math.max(0, oldLimit + adjustment);
                lumiosChat.setCommunicationLimit(newLimit);
                chatService.save(lumiosChat);
                telegramClient.sendAnswerCallbackQuery(
                    String.format("Communication limit: %d → %d", oldLimit, newLimit), 
                    message.getId());
                commLimitDebug(message, chatId);
            }
        } catch (Exception e) {
            telegramClient.sendAnswerCallbackQuery("Помилка при зміні ліміту!", message.getId());
        }
    }
}
