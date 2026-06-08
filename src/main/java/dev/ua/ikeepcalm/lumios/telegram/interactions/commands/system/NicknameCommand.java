package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.system;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Slf4j
@Component
@BotCommand(command = "nickname")
public class NicknameCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        String[] args = message.getText().split("\\s+", 2);
        
        if (args.length == 1) {
            if (chat.getBotNickname() == null || chat.getBotNickname().trim().isEmpty()) {
                sendMessage(translationService.getMessage("nickname.no_nickname", chat), message);
            } else {
                sendMessage(translationService.getMessage("nickname.current_nickname", chat, chat.getBotNickname(), getBotUsername()), ParseMode.MARKDOWN, message);
            }
            return;
        }
        
        String nickname = args[1].trim();
        
        if (nickname.length() < 2 || nickname.length() > 20) {
            sendMessage(translationService.getMessage("nickname.invalid_length", chat), message);
            return;
        }
        
        if (nickname.matches(".*[@#/].*")) {
            sendMessage(translationService.getMessage("nickname.invalid_characters", chat), message);
            return;
        }
        
        chat.setBotNickname(nickname);
        chatService.save(chat);
        
        sendMessage(translationService.getMessage("nickname.success", chat, nickname), message);
        log.info("Nickname set to '{}' for chat {}", nickname, chat.getChatId());
    }
    
    private String getBotUsername() {
        return "@lumios_bot";
    }
}