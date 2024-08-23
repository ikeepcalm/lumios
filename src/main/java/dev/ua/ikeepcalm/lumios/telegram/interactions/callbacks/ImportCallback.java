package dev.ua.ikeepcalm.lumios.telegram.interactions.callbacks;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.DayEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.campus.CampusClass;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.campus.CampusDay;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.campus.CampusTimetable;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.ClassType;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.WeekType;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCallback;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.ImportUtil;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.RemoveMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@BotCallback(startsWith = "import#")
public class ImportCallback extends ServicesShortcut implements Interaction {

    private static final Logger log = LoggerFactory.getLogger(ImportCallback.class);

    @Override
    public void fireInteraction(CallbackQuery callbackQuery, LumiosUser user, LumiosChat chat) {
        String data = callbackQuery.getData();
        String[] split = data.split("#");
        String groupId = split[1];

        RemoveMessage removeMessage = new RemoveMessage();
        removeMessage.setMessageId(callbackQuery.getMessage().getMessageId());
        removeMessage.setChatId(callbackQuery.getMessage().getChatId());
        try {
            telegramClient.sendRemoveMessage(removeMessage);
        } catch (TelegramApiException e) {
            log.error("Error while removing message", e);
        }

        TextMessage textMessage = new TextMessage();
        textMessage.setChatId(callbackQuery.getMessage().getChatId());
        textMessage.setText("Імпортую розклад для `" + data.replace("import#", "") + "`");
        textMessage.setParseMode(ParseMode.MARKDOWN);
        Message message = telegramClient.sendTextMessage(textMessage);

        CampusTimetable campusTimetable = ImportUtil.getScheduleByGroup(groupId);
        List<TimetableEntry> timetableEntries = convertToTimetableEntries(campusTimetable, chat);
        List<TimetableEntry> allByChatId;
        try {
            allByChatId = timetableService.findAllByChatId(chat.getChatId());
        } catch (NoSuchEntityException e) {
            allByChatId = new ArrayList<>();
        }
        if (allByChatId.isEmpty()) {
            timetableService.saveAll(timetableEntries);
        } else {
            timetableService.deleteAll(allByChatId);
            timetableService.saveAll(timetableEntries);
        }

        EditMessage editMessage = new EditMessage();
        editMessage.setChatId(message.getChatId());
        editMessage.setMessageId(message.getMessageId());
        editMessage.setText("Розклад для `" + data.replace("import#", "") + "` був іспішно імпортований!");
        editMessage.setParseMode(ParseMode.MARKDOWN);
        editMessage(editMessage);
    }

    private List<TimetableEntry> convertToTimetableEntries(CampusTimetable timetableWrapper, LumiosChat chat) {
        List<TimetableEntry> timetableEntries = new ArrayList<>();
        TimetableEntry firstWeekEntry = new TimetableEntry();
        firstWeekEntry.setWeekType(WeekType.WEEK_A);
        firstWeekEntry.setChat(chat);
        for (CampusDay dayWrapper : timetableWrapper.getScheduleFirstWeek()) {
            DayEntry dayEntry = new DayEntry();
            dayEntry.setDayName(mapDayOfWeek(dayWrapper.getDay()));
            dayEntry.setTimetableEntry(firstWeekEntry);

            for (CampusClass classWrapper : dayWrapper.getPairs()) {
                ClassEntry classEntry = new ClassEntry();
                classEntry.setName(classWrapper.getName());
                classEntry.setClassType(mapClassType(classWrapper.getTag()));
                classEntry.setStartTime(parseTime(classWrapper.getTime()));
                classEntry.setEndTime(parseTime(classWrapper.getTime()).plusMinutes(90));
                classEntry.setDayEntry(dayEntry);
                dayEntry.getClassEntries().add(classEntry);
            }
            firstWeekEntry.getDays().add(dayEntry);
        }
        timetableEntries.add(firstWeekEntry);

        TimetableEntry secondWeekEntry = new TimetableEntry();
        secondWeekEntry.setWeekType(WeekType.WEEK_B);
        secondWeekEntry.setChat(chat);
        for (CampusDay dayWrapper : timetableWrapper.getScheduleSecondWeek()) {
            DayEntry dayEntry = new DayEntry();
            dayEntry.setDayName(mapDayOfWeek(dayWrapper.getDay()));
            dayEntry.setTimetableEntry(secondWeekEntry);

            for (CampusClass classWrapper : dayWrapper.getPairs()) {
                ClassEntry classEntry = new ClassEntry();
                classEntry.setName(classWrapper.getName());
                classEntry.setClassType(mapClassType(classWrapper.getTag()));
                classEntry.setStartTime(parseTime(classWrapper.getTime()));
                classEntry.setEndTime(parseTime(classWrapper.getTime()).plusMinutes(90));
                classEntry.setDayEntry(dayEntry);
                dayEntry.getClassEntries().add(classEntry);
            }
            secondWeekEntry.getDays().add(dayEntry);
        }
        timetableEntries.add(secondWeekEntry);

        return timetableEntries;
    }

    private DayOfWeek mapDayOfWeek(String day) {
        return switch (day) {
            case "Пн" -> DayOfWeek.MONDAY;
            case "Вв" -> DayOfWeek.TUESDAY;
            case "Ср" -> DayOfWeek.WEDNESDAY;
            case "Чт" -> DayOfWeek.THURSDAY;
            case "Пт" -> DayOfWeek.FRIDAY;
            case "Сб" -> DayOfWeek.SATURDAY;
            default -> null;
        };
    }

    private ClassType mapClassType(String tag) {
        return switch (tag) {
            case "prac" -> ClassType.PRACTICE;
            case "lec" -> ClassType.LECTURE;
            case "lab" -> ClassType.LAB;
            default -> null;
        };
    }

    private LocalTime parseTime(String time) {
        String timeWithColon = time.replace(".", ":");
        String formattedTime = timeWithColon.length() == 4 ? "0" + timeWithColon : timeWithColon;
        return LocalTime.parse(formattedTime, DateTimeFormatter.ofPattern("HH:mm"));
    }
}
