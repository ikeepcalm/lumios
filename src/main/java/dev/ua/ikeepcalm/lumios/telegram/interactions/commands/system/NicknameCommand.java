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
                sendMessage("Зараз бот не має псевдоніма. Використовуйте `/nickname [псевдонім]` щоб встановити псевдонім для зручного звертання.", message);
            } else {
                sendMessage("Поточний псевдонім бота: **" + chat.getBotNickname() + "**\n\nВи можете звертатися до мене або як " + getBotUsername() + " або як " + chat.getBotNickname(), ParseMode.MARKDOWN, message);
            }
            return;
        }
        
        String nickname = args[1].trim();
        
        if (nickname.length() < 2 || nickname.length() > 20) {
            sendMessage("Псевдонім повинен бути від 2 до 20 символів довжиною.", message);
            return;
        }
        
        if (nickname.matches(".*[@#/].*")) {
            sendMessage("Псевдонім не може містити символи @, # або /", message);
            return;
        }
        
        chat.setBotNickname(nickname);
        chatService.save(chat);
        
        sendMessage("Псевдонім успішно встановлено! Тепер ви можете звертатися до мене як: **" + nickname + "**\n\nПриклад: \"" + nickname + ", що таке Java?\"", message);
        log.info("Nickname set to '{}' for chat {}", nickname, chat.getChatId());
    }
    
    private String getBotUsername() {
        return "@lumios_bot";
    }
}