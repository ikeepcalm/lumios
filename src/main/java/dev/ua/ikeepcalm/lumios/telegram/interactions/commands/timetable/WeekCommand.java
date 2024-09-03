package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.timetable;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.DayEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.parsers.TimetableParser;
import dev.ua.ikeepcalm.lumios.telegram.utils.WeekValidator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

@Component
@BotCommand(command = "week")
public class WeekCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        try {
            TimetableEntry timetableEntry = timetableService.findByChatIdAndWeekType(message.getChatId(),
                    WeekValidator.determineWeekDay());

            StringBuilder messageBuilder = new StringBuilder("\uD83D\uDCC5> *РОЗКЛАД НА ТИЖДЕНЬ* <\uD83D\uDCC5 \n\n");
            messageBuilder.append("``` \uD83D\uDD35 - ЛЕКЦІЯ\n \uD83D\uDFE0 - ПРАКТИКА\n \uD83D\uDFE2 - ЛАБОРАТОРНА```\n\n");
            for (DayEntry dayEntry : timetableEntry.getDays()) {
                if (!dayEntry.getClassEntries().isEmpty()) {
                    messageBuilder.append("*").append(dayEntry.getDayName()).append(" {*\n");
                    List<ClassEntry> classEntries = dayEntry.getClassEntries();
                    for (int i = 0; i < classEntries.size(); i++) {
                        ClassEntry classEntry = classEntries.get(i);
                        messageBuilder.append(classEntry.getStartTime()).append(" - ").append(classEntry.getEndTime()).append("\n");
                        messageBuilder.append(TimetableParser.parseClassEmoji(classEntry.getClassType())).append(" [").append(classEntry.getName()).append("]");
                        messageBuilder.append("(").append(classEntry.getUrl()).append(")");
                        if (i < classEntries.size() - 1) {
                            messageBuilder.append("\n\n");
                        }
                    }
                    messageBuilder.append("\n*}*\n\n");
                }
            }

            sendMessage(messageBuilder.toString(), ParseMode.MARKDOWN, message);
        } catch (NoSuchEntityException e) {
            sendMessage("Не знайдено розклад на цей тиждень! Ви точно все налаштували?", message);
        }
    }

}
