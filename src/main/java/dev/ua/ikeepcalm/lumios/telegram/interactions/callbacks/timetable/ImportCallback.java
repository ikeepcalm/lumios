package dev.ua.ikeepcalm.lumios.telegram.interactions.callbacks.timetable;

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
import dev.ua.ikeepcalm.lumios.telegram.exceptions.CampusApiException;
import dev.ua.ikeepcalm.lumios.telegram.utils.ElectiveDetector;
import dev.ua.ikeepcalm.lumios.telegram.utils.ImportPicker;
import dev.ua.ikeepcalm.lumios.telegram.utils.PersonalTimetableSupport;
import dev.ua.ikeepcalm.lumios.telegram.utils.ImportUtil;
import dev.ua.ikeepcalm.lumios.telegram.utils.ImportUtil.CampusGroup;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.RemoveMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@BotCallback(startsWith = "import#")
public class ImportCallback extends ServicesShortcut implements Interaction {

    private static final Logger log = LoggerFactory.getLogger(ImportCallback.class);

    private final PersonalTimetableSupport support;

    public ImportCallback(PersonalTimetableSupport support) {
        this.support = support;
    }

    /**
     * A class slot on the campus schedule is always an hour and a half long; the API only gives the start.
     */
    private static final int CLASS_DURATION_MINUTES = 90;

    @Override
    public void fireInteraction(CallbackQuery callbackQuery, LumiosUser user, LumiosChat chat) {
        String data = callbackQuery.getData();
        String[] parts = data.split("#", 4);
        String action = parts.length > 1 ? parts[1] : "";

        switch (action) {
            case "x" -> dismiss(callbackQuery, chat);
            case "p" -> showPage(callbackQuery, chat, parts);
            case "g" -> importGroup(callbackQuery, chat, parts);
            default -> {
                log.warn("Unrecognised import callback: {}", data);
                telegramClient.sendAnswerCallbackQuery(
                        translationService.getMessage("command.import.expired", chat), callbackQuery.getId());
            }
        }
    }

    private void dismiss(CallbackQuery callbackQuery, LumiosChat chat) {
        telegramClient.sendAnswerCallbackQuery(
                translationService.getMessage("command.import.cancelled", chat), callbackQuery.getId());
        removeMessage(callbackQuery);
    }

    private void showPage(CallbackQuery callbackQuery, LumiosChat chat, String[] parts) {
        if (parts.length < 4) {
            telegramClient.sendAnswerCallbackQuery(
                    translationService.getMessage("command.import.expired", chat), callbackQuery.getId());
            return;
        }

        int page;
        try {
            page = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            telegramClient.sendAnswerCallbackQuery(
                    translationService.getMessage("command.import.expired", chat), callbackQuery.getId());
            return;
        }
        String query = parts[3];

        List<CampusGroup> groups;
        try {
            groups = ImportUtil.searchGroups(query);
        } catch (CampusApiException e) {
            log.warn("KPI Campus group search failed for query '{}'", query, e);
            telegramClient.sendAnswerCallbackQuery(
                    translationService.getMessage("command.import.api-error", chat), callbackQuery.getId());
            return;
        }

        telegramClient.sendAnswerCallbackQuery(null, callbackQuery.getId());
        if (groups.isEmpty()) {
            removeMessage(callbackQuery);
            return;
        }

        page = ImportPicker.clampPage(page, groups.size());
        EditMessage editMessage = new EditMessage();
        editMessage.setChatId(callbackQuery.getMessage().getChatId());
        editMessage.setMessageId(callbackQuery.getMessage().getMessageId());
        editMessage.setText(ImportPicker.text(translationService, chat, query, groups, page));
        editMessage.setReplyKeyboard(ImportPicker.keyboard(translationService, chat, query, groups, page));
        editMessage(editMessage);
    }

    private void importGroup(CallbackQuery callbackQuery, LumiosChat chat, String[] parts) {
        int groupId;
        try {
            groupId = Integer.parseInt(parts.length > 2 ? parts[2] : "");
        } catch (NumberFormatException e) {
            log.warn("Import callback carried a malformed group id: {}", callbackQuery.getData());
            telegramClient.sendAnswerCallbackQuery(
                    translationService.getMessage("command.import.expired", chat), callbackQuery.getId());
            return;
        }

        telegramClient.sendAnswerCallbackQuery(
                translationService.getMessage("command.import.toast", chat), callbackQuery.getId());
        removeMessage(callbackQuery);

        String groupName = resolveGroupName(groupId);

        TextMessage loading = new TextMessage();
        loading.setChatId(callbackQuery.getMessage().getChatId());
        loading.setText(translationService.getMessage("command.import.loading", chat, groupName));
        Message progress = telegramClient.sendTextMessage(loading);
        if (progress == null) {
            log.warn("Could not send the import progress message to chat {}", chat.getChatId());
            return;
        }

        EditMessage result = new EditMessage();
        result.setChatId(progress.getChatId());
        result.setMessageId(progress.getMessageId());

        CampusTimetable campusTimetable;
        try {
            campusTimetable = ImportUtil.getScheduleByGroup(groupId);
        } catch (CampusApiException e) {
            log.warn("Could not fetch the KPI Campus schedule of group {} ({})", groupId, groupName, e);
            result.setText(translationService.getMessage("command.import.api-error", chat));
            editMessage(result);
            return;
        }

        if (ImportUtil.isEmpty(campusTimetable)) {
            log.info("KPI Campus has no published schedule for group {} ({})", groupId, groupName);
            result.setText(translationService.getMessage("command.import.empty", chat, groupName));
            editMessage(result);
            return;
        }

        List<TimetableEntry> imported = convertToTimetableEntries(campusTimetable, chat);
        int preservedUrls = replaceTimetables(chat, imported);

        result.setText(successMessage(chat, groupName, imported, preservedUrls));
        editMessage(result);
    }

    /**
     * Swaps the chat timetables for the freshly imported ones, carrying over the conference links
     * that were attached to matching classes. Returns how many links survived.
     */
    private int replaceTimetables(LumiosChat chat, List<TimetableEntry> imported) {
        List<TimetableEntry> existing = loadExistingTimetables(chat);

        int preservedUrls = 0;
        for (TimetableEntry fresh : imported) {
            for (TimetableEntry old : existing) {
                if (old.getWeekType() == fresh.getWeekType()) {
                    preservedUrls += preserveUrls(old, fresh);
                    break;
                }
            }
        }

        if (!existing.isEmpty()) {
            timetableService.deleteAll(existing);
        }
        timetableService.saveAll(imported);

        // The curriculum may have changed shape, so cached electives are stale and choices whose
        // subject no longer exists would silently filter classes that are now mandatory.
        support.invalidateElectives(chat.getChatId());
        int pruned = personalTimetableService.pruneChoices(
                chat.getChatId(), ElectiveDetector.choiceKeys(imported));
        if (pruned > 0) {
            log.info("Pruned {} elective choice(s) in chat {} after re-import", pruned, chat.getChatId());
        }
        return preservedUrls;
    }

    private List<TimetableEntry> loadExistingTimetables(LumiosChat chat) {
        List<TimetableEntry> loaded = new ArrayList<>();
        try {
            for (TimetableEntry entry : timetableService.findAllByChatId(chat.getChatId())) {
                try {
                    // Re-read with days and classes eagerly attached so links can be matched below.
                    loaded.add(timetableService.findByChatIdAndWeekTypeWithDays(chat.getChatId(), entry.getWeekType()));
                } catch (NoSuchEntityException e) {
                    loaded.add(entry);
                }
            }
        } catch (NoSuchEntityException e) {
            log.debug("No existing timetables found for chat: {}", chat.getChatId());
        }
        return loaded;
    }

    private String resolveGroupName(int groupId) {
        try {
            Optional<CampusGroup> group = ImportUtil.findGroupById(groupId);
            if (group.isPresent()) {
                return group.get().name();
            }
        } catch (CampusApiException e) {
            log.debug("Could not resolve the name of group {}", groupId, e);
        }
        return String.valueOf(groupId);
    }

    private String successMessage(LumiosChat chat, String groupName, List<TimetableEntry> imported, int preservedUrls) {
        StringBuilder text = new StringBuilder(
                translationService.getMessage("command.import.success", chat, groupName,
                        String.valueOf(countClasses(imported, WeekType.WEEK_A)),
                        String.valueOf(countClasses(imported, WeekType.WEEK_B))));
        if (preservedUrls > 0) {
            text.append("\n").append(translationService.getMessage("command.import.links", chat, String.valueOf(preservedUrls)));
        }
        text.append("\n\n").append(translationService.getMessage("command.import.next-steps", chat));
        return text.toString();
    }

    private int countClasses(List<TimetableEntry> timetables, WeekType weekType) {
        int total = 0;
        for (TimetableEntry timetable : timetables) {
            if (timetable.getWeekType() != weekType) {
                continue;
            }
            for (DayEntry day : timetable.getDays()) {
                total += day.getClassEntries().size();
            }
        }
        return total;
    }

    private void removeMessage(CallbackQuery callbackQuery) {
        RemoveMessage removeMessage = new RemoveMessage();
        removeMessage.setMessageId(callbackQuery.getMessage().getMessageId());
        removeMessage.setChatId(callbackQuery.getMessage().getChatId());
        try {
            telegramClient.sendRemoveMessage(removeMessage);
        } catch (TelegramApiException e) {
            log.debug("Could not remove the import picker message", e);
        }
    }

    private int preserveUrls(TimetableEntry oldEntry, TimetableEntry newEntry) {
        if (oldEntry == null || newEntry == null) {
            return 0;
        }

        int preserved = 0;
        for (DayEntry newDay : newEntry.getDays()) {
            for (ClassEntry newClass : newDay.getClassEntries()) {
                if (newClass.getUrl() == null || newClass.getUrl().isEmpty()) {
                    String oldUrl = findUrlInOldTimetable(oldEntry, newDay.getDayName(), newClass.getStartTime());
                    if (oldUrl != null && !oldUrl.isEmpty()) {
                        newClass.setUrl(oldUrl);
                        preserved++;
                    }
                }
            }
        }
        return preserved;
    }

    private String findUrlInOldTimetable(TimetableEntry oldEntry, DayOfWeek dayOfWeek, LocalTime startTime) {
        for (DayEntry oldDay : oldEntry.getDays()) {
            if (oldDay.getDayName() == dayOfWeek) {
                for (ClassEntry oldClass : oldDay.getClassEntries()) {
                    if (oldClass.getStartTime() != null && oldClass.getStartTime().equals(startTime)) {
                        return oldClass.getUrl();
                    }
                }
            }
        }
        return null;
    }

    private List<TimetableEntry> convertToTimetableEntries(CampusTimetable timetableWrapper, LumiosChat chat) {
        return List.of(
                convertWeek(timetableWrapper.getScheduleFirstWeek(), WeekType.WEEK_A, chat),
                convertWeek(timetableWrapper.getScheduleSecondWeek(), WeekType.WEEK_B, chat)
        );
    }

    private TimetableEntry convertWeek(List<CampusDay> campusDays, WeekType weekType, LumiosChat chat) {
        TimetableEntry weekEntry = new TimetableEntry();
        weekEntry.setWeekType(weekType);
        weekEntry.setChat(chat);
        if (campusDays == null) {
            return weekEntry;
        }

        for (CampusDay dayWrapper : campusDays) {
            DayOfWeek dayName = mapDayOfWeek(dayWrapper.getDay());
            if (dayName == null) {
                log.warn("Skipping a campus day with an unknown name: {}", dayWrapper.getDay());
                continue;
            }

            DayEntry dayEntry = new DayEntry();
            dayEntry.setDayName(dayName);
            dayEntry.setTimetableEntry(weekEntry);

            if (dayWrapper.getPairs() != null) {
                for (CampusClass classWrapper : dayWrapper.getPairs()) {
                    LocalTime startTime = parseTime(classWrapper.getTime());
                    if (startTime == null) {
                        log.warn("Skipping class '{}' with an unparseable time: {}",
                                classWrapper.getName(), classWrapper.getTime());
                        continue;
                    }

                    ClassEntry classEntry = new ClassEntry();
                    classEntry.setName(classWrapper.getName());
                    classEntry.setClassType(mapClassType(classWrapper.getTag()));
                    classEntry.setTeacherName(classWrapper.teacherName());
                    classEntry.setLocation(classWrapper.getLocation());
                    classEntry.setStartTime(startTime);
                    classEntry.setEndTime(startTime.plusMinutes(CLASS_DURATION_MINUTES));
                    classEntry.setDayEntry(dayEntry);
                    dayEntry.getClassEntries().add(classEntry);
                }
            }
            weekEntry.getDays().add(dayEntry);
        }
        return weekEntry;
    }

    private DayOfWeek mapDayOfWeek(String day) {
        if (day == null) {
            return null;
        }
        return switch (day.trim()) {
            case "Пн" -> DayOfWeek.MONDAY;
            case "Вв", "Вт" -> DayOfWeek.TUESDAY;
            case "Ср" -> DayOfWeek.WEDNESDAY;
            case "Чт" -> DayOfWeek.THURSDAY;
            case "Пт" -> DayOfWeek.FRIDAY;
            case "Сб" -> DayOfWeek.SATURDAY;
            case "Нд" -> DayOfWeek.SUNDAY;
            default -> null;
        };
    }

    private ClassType mapClassType(String tag) {
        if (tag == null) {
            return null;
        }
        return switch (tag) {
            case "prac" -> ClassType.PRACTICE;
            case "lec" -> ClassType.LECTURE;
            case "lab" -> ClassType.LAB;
            default -> null;
        };
    }

    /**
     * Campus returns {@code HH:mm:ss}, but tolerate {@code H:mm}, {@code HH:mm} and dot separators too.
     */
    private LocalTime parseTime(String time) {
        if (time == null || time.isBlank()) {
            return null;
        }
        String[] segments = time.trim().replace('.', ':').split(":");
        if (segments.length < 2) {
            return null;
        }
        try {
            return LocalTime.of(Integer.parseInt(segments[0]), Integer.parseInt(segments[1]));
        } catch (NumberFormatException | DateTimeException e) {
            return null;
        }
    }
}
