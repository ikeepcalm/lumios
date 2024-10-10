package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.system;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.ai.OpenAI;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@BotCommand(startsWith = "summary", aliases = {"summarise", "summarize"})
public class SummaryCommand extends ServicesShortcut implements Interaction {

    private final OpenAI openAI;

    public SummaryCommand(OpenAI openAI) {
        this.openAI = openAI;
    }

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        String text = update.getMessage().getText();

        String[] parts = text.split(" ");

        if (parts.length < 2) {
            sendMessage("Введіть кількість повідомлень для підведення підсумку!", update.getMessage());
            return;
        }

        try {
            int count = Integer.parseInt(parts[1]);
            if (count < 1 || count > 100) {
                sendMessage("Кількість повідомлень повинна бути від 1 до 100!", update.getMessage());
                return;
            }

            if (chat.getSummaryLimit() <= 0) {
                sendMessage("Ви вже використали всі спроби підведення підсумку за повідомлення на сьогодні!", update.getMessage());
                return;
            }


            int foundMessages = recordService.findLastMessagesByChatId(chat.getChatId(), count).size();

            if (foundMessages < count) {
                System.out.println("foundMessages: " + foundMessages + " count: " + count);

                sendMessage("Недостатньо повідомлень для підведення підсумку! Можливо у мене немає дозволів читати повідомлення в цій групі?", update.getMessage());
                return;
            }

            try {
                telegramClient.execute(SendChatAction.builder()
                        .action(String.valueOf(ActionType.TYPING))
                        .chatId(update.getMessage().getChatId())
                        .build());
            } catch (TelegramApiException e) {
                log.error("Failed to send chat action", e);
            }

            openAI.getChatSummary(chat.getChatId(), count).thenAccept(response -> {
                if (response != null) {
                    sendMessage(response, ParseMode.MARKDOWN, update.getMessage());
                    chat.setSummaryLimit(chat.getSummaryLimit() - 1);
                    chatService.save(chat);
                }
            }).exceptionally(ex -> {
                log.error("Failed to get summary", ex);
                sendMessage("Виникла помилка при спробі взаємодії з Open AI.", update.getMessage());
                return null;
            });


        } catch (NumberFormatException e) {
            sendMessage("Введіть коректне число повідомлень для підведення підсумку!", update.getMessage());
        }
    }
}

