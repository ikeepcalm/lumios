package dev.ua.ikeepcalm.lumios.telegram.interactions.commands;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@BotCommand(command = "wheel")
public class WheelCommand extends ServicesShortcut implements Interaction {

    @Contract(pure = true, value = "-> new")
    private @NotNull SecureRandom RNG() {
        return new SecureRandom(SecureRandom.getSeed(20));
    }

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        LocalDateTime lastWheel = chat.getLastWheelDate();
        if (lastWheel == null) {
            lastWheel = LocalDateTime.now().minusDays(7);
        }
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(lastWheel, now);
        if (duration.toHours() >= 24) {
            Set<LumiosUser> lumiosUsers = chat.getUsers();
            LumiosUser winner = List.copyOf(lumiosUsers).get(RNG().nextInt(lumiosUsers.size()));

            int winAmount = RNG().nextInt(1000);
            winner.setReverence(winner.getReverence() + winAmount);
            userService.save(winner);
            chat.setLastWheelDate(now);
            chatService.save(chat);
            String text = """
                    *КОЛЕСО ФОРТУНИ 🎡*
                                        
                    🎉 Переможець: %s
                    💰 Виграш: __ %d __
                                        
                    _Не розказуйте нікому про це!_
                    """.formatted(winner.getUsername()
                    .replace("_", "\\_")
                    .replace("*", "\\*"), winAmount);
            try {
                sendMessage(text, ParseMode.MARKDOWN, message);
            } catch (Exception e) {
                sendMessage(text, message);
            }
        } else {
            sendMessage("До наступної спроби залишилося %d годин(и) %d хвилин(и)".formatted(24 - duration.toHours(), 60 - duration.toMinutes() % 60), ParseMode.MARKDOWN, message);
        }
    }
}

