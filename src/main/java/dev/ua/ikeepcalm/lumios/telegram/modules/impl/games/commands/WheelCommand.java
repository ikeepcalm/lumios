package dev.ua.ikeepcalm.lumios.telegram.modules.impl.games.commands;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.ReverenceUser;
import dev.ua.ikeepcalm.lumios.telegram.modules.parents.CommandParent;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Component
public class WheelCommand extends CommandParent {

    private static final Logger log = LoggerFactory.getLogger(WheelCommand.class);

    @Override
    @Transactional
    public void processUpdate(Message message) {
        LocalDateTime lastWheel = reverenceChat.getLastWheelDate();
        if (lastWheel == null) {
            lastWheel = LocalDateTime.now().minusDays(7);
        }
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(lastWheel, now);
        if (duration.toHours() >= 24) {
            Set<ReverenceUser> reverenceUsers = reverenceChat.getUsers();
            Random random = new Random();
            ReverenceUser winner = List.copyOf(reverenceUsers).get(random.nextInt(reverenceUsers.size()));

            int winAmount = random.nextInt(1000);
            winner.setReverence(winner.getReverence() + winAmount);
            userService.save(winner);
            reverenceChat.setLastWheelDate(now);
            chatService.save(reverenceChat);
            String text = """
                    *КОЛЕСО ФОРТУНИ 🎡*
                    
                    🎉 Переможець: %s
                    💰 Виграш: __ %d __
                    
                    _Не розказуйте нікому про це!_
                    """.formatted(winner.getUsername(), winAmount);
            sendMessage(text, ParseMode.MARKDOWN);
        } else {
            sendMessage("До наступної спроби залишилося %d годин(и) %d хвилин(и)".formatted(24 - duration.toHours(), 60 - duration.toMinutes() % 60), ParseMode.MARKDOWN);
        }
    }
}

