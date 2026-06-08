package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.system;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.source.AiModel;
import dev.ua.ikeepcalm.lumios.telegram.ai.Gemini;
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
    private final Gemini gemini;

    public SummaryCommand(OpenAI openAI, Gemini gemini) {
        this.openAI = openAI;
        this.gemini = gemini;
    }

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        String text = update.getMessage().getText();

        String[] parts = text.split(" ");

        if (parts.length < 2) {
            sendMessage(translationService.getMessage("summary.error.empty_count", chat), update.getMessage());
            return;
        }

        try {
            int count = Integer.parseInt(parts[1]);
            if (count < 1 || count > 200) {
                sendMessage(translationService.getMessage("summary.error.invalid_range", chat), update.getMessage());
                return;
            }

            if (chat.getSummaryLimit() <= 0) {
                sendMessage(translationService.getMessage("summary.error.limit_reached", chat), update.getMessage());
                return;
            }


            int foundMessages = recordService.findLastMessagesByChatId(chat.getChatId(), count).size();

            if (foundMessages < count) {
                System.out.println("foundMessages: " + foundMessages + " count: " + count);

                sendMessage(translationService.getMessage("summary.error.insufficient_messages", chat), update.getMessage());
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

            if (chat.getAiModel() == null) {
                chat.setAiModel(AiModel.OPENAI);
            }

            switch (chat.getAiModel()) {
                case GEMINI -> {
                    gemini.getChatSummary(chat, count).thenAccept(response -> {
                        if (response != null) {
                            sendMessage(response, ParseMode.MARKDOWN, update.getMessage());
                            chat.setSummaryLimit(chat.getSummaryLimit() - 1);
                            chatService.save(chat);
                        }
                    }).exceptionally(ex -> {
                        log.error("Failed to get summary from Gemini", ex);
                        sendMessage(translationService.getMessage("summary.error.gemini", chat), update.getMessage());
                        return null;
                    });
                }
                case OPENAI -> {
                    openAI.getChatSummary(chat, count).thenAccept(response -> {
                        if (response != null) {
                            sendMessage(response, ParseMode.MARKDOWN, update.getMessage());
                            chat.setSummaryLimit(chat.getSummaryLimit() - 1);
                            chatService.save(chat);
                        }
                    }).exceptionally(ex -> {
                        log.error("Failed to get summary from OpenAI", ex);
                        sendMessage(translationService.getMessage("summary.error.openai", chat), update.getMessage());
                        return null;
                    });
                }
            }


        } catch (NumberFormatException e) {
            sendMessage(translationService.getMessage("summary.error.invalid_number", chat), update.getMessage());
        }
    }
}

