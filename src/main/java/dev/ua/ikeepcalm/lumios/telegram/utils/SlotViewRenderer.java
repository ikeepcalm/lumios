package dev.ua.ikeepcalm.lumios.telegram.utils;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.TimetableService;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.DayEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.TelegramClient;
import dev.ua.ikeepcalm.lumios.telegram.utils.TimetableViewSupport.Scope;
import dev.ua.ikeepcalm.lumios.telegram.utils.TimetableViewSupport.Target;
import dev.ua.ikeepcalm.lumios.telegram.utils.markup.ClassMarkupUtil;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Renders a single time slot - the shared body of {@code /now} and {@code /next}, and of the scope
 * button underneath them.
 * <p>
 * The personal filter changes what {@code /next} even means. It used to answer with the earliest
 * remaining slot; for a member who takes none of the electives in it, that slot is empty and the honest
 * answer is the one after it. So the search walks slots in order until one of them has something in it
 * for the person asking.
 */
@Component
public class SlotViewRenderer {

    /**
     * How far ahead {@code /next} looks once today is over. A week is enough to cross an empty
     * Friday/Saturday and land on the other half of the two-week cycle.
     */
    private static final int LOOKAHEAD_DAYS = 7;

    private final TimetableService timetableService;
    private final TimetableViewSupport viewSupport;
    private final PersonalTimetableSupport personalTimetableSupport;
    private final TranslationService translationService;
    private final TelegramClient telegramClient;

    public SlotViewRenderer(TimetableService timetableService, TimetableViewSupport viewSupport,
                            PersonalTimetableSupport personalTimetableSupport,
                            TranslationService translationService, TelegramClient telegramClient) {
        this.timetableService = timetableService;
        this.viewSupport = viewSupport;
        this.personalTimetableSupport = personalTimetableSupport;
        this.translationService = translationService;
        this.telegramClient = telegramClient;
    }

    /**
     * Builds the answer to the command. The caller sends it, so it keeps control of whether the reply is
     * auto-deleted.
     *
     * @param commandType {@code now} or {@code next}
     */
    public TextMessage answer(Message message, LumiosChat chat, String commandType) {
        Long telegramUserId = message.getFrom().getId();
        Target target = viewSupport.target(message, chat, telegramUserId);

        if (target.ambiguous()) {
            return message(message, translationService.getMessage("view.choose-group", chat),
                    viewSupport.chooseGroupKeyboard(target.chooseFrom(), commandType));
        }
        if (!target.resolved()) {
            return message(message, translationService.getMessage("mine.no-groups", chat), null);
        }

        LumiosChat groupChat = target.groupChat();
        Scope scope = viewSupport.scopeFrom(message, groupChat.getChatId(), telegramUserId);
        TextMessage built = build(groupChat, chat, telegramUserId, commandType, scope);
        built.setChatId(message.getChatId());
        built.setMessageId(message.getMessageId());
        return built;
    }

    /**
     * Replaces an existing view in place, for the scope button.
     */
    public void replace(MaybeInaccessibleMessage shown, LumiosChat languageSource, LumiosChat groupChat, Long telegramUserId,
                        String commandType, Scope scope) {
        TextMessage built = build(groupChat, languageSource, telegramUserId, commandType, scope);

        EditMessage edit = new EditMessage();
        if (shown.getChatId() == null) {
            return;
        }
        edit.setChatId(shown.getChatId());
        if (shown.getMessageId() == null) {
            return;
        }
        edit.setMessageId(shown.getMessageId());
        edit.setText(built.getText());
        edit.setParseMode(ParseMode.MARKDOWN);
        edit.setReplyKeyboard(built.getReplyKeyboard());
        telegramClient.sendEditMessage(edit);
    }

    /**
     * Replaces an existing view in place, for the scope button.
     */
    public void replace(Message shown, LumiosChat languageSource, LumiosChat groupChat, Long telegramUserId,
                        String commandType, Scope scope) {
        TextMessage built = build(groupChat, languageSource, telegramUserId, commandType, scope);

        EditMessage edit = new EditMessage();
        edit.setChatId(shown.getChatId());
        edit.setMessageId(shown.getMessageId());
        edit.setText(built.getText());
        edit.setParseMode(ParseMode.MARKDOWN);
        edit.setReplyKeyboard(built.getReplyKeyboard());
        telegramClient.sendEditMessage(edit);
    }

    private TextMessage build(LumiosChat groupChat, LumiosChat languageSource, Long telegramUserId,
                              String commandType, Scope scope) {
        boolean personalAvailable = viewSupport.personalAvailable(groupChat.getChatId(), telegramUserId);
        // See DayViewRenderer: a payload can outlive the choices it was built for.
        scope = personalAvailable ? scope : Scope.ALL;
        TextMessage built = "now".equals(commandType)
                ? now(groupChat, languageSource, telegramUserId, scope)
                : next(groupChat, languageSource, telegramUserId, scope);

        built.setText(built.getText() + viewSupport.footer(scope, personalAvailable, languageSource));
        appendSwitchRow(built, groupChat.getChatId(), commandType, scope, personalAvailable, languageSource);
        return built;
    }

    /**
     * Whatever is running right now, filtered to the caller's own classes.
     */
    private TextMessage now(LumiosChat groupChat, LumiosChat languageSource, Long telegramUserId, Scope scope) {
        LocalDate today = TimetableClock.today();
        LocalTime currentTime = TimetableClock.now();

        Map<LocalTime, List<ClassEntry>> slots = slotsOn(groupChat.getChatId(), today);
        if (slots == null) {
            return plain(translationService.getMessage("class.now.error", languageSource));
        }

        for (List<ClassEntry> slot : slots.values()) {
            List<ClassEntry> running = slot.stream()
                    .filter(classEntry -> classEntry.getStartTime() != null && classEntry.getEndTime() != null)
                    .filter(classEntry -> currentTime.isAfter(classEntry.getStartTime())
                                          && currentTime.isBefore(classEntry.getEndTime()))
                    .toList();
            if (running.isEmpty()) {
                continue;
            }

            List<ClassEntry> mine = filter(groupChat.getChatId(), telegramUserId, running, scope);
            if (mine.isEmpty()) {
                // The slot is running, but none of it is theirs - which is a free period, not an error.
                break;
            }
            return mine.size() == 1
                    ? ClassMarkupUtil.createNowNotification(mine.getFirst(), languageSource, translationService)
                    : ClassMarkupUtil.createMultipleNowNotification(mine, languageSource, translationService);
        }
        return plain(translationService.getMessage("class.now.none", languageSource));
    }

    /**
     * The next slot that actually has something in it for this member, today or on a later day.
     */
    private TextMessage next(LumiosChat groupChat, LumiosChat languageSource, Long telegramUserId, Scope scope) {
        LocalDate today = TimetableClock.today();

        List<ClassEntry> remainingToday = firstNonEmptySlot(groupChat.getChatId(), telegramUserId, today,
                TimetableClock.now(), scope);
        if (!remainingToday.isEmpty()) {
            return remainingToday.size() == 1
                    ? ClassMarkupUtil.createNextNotification(remainingToday.getFirst(), languageSource, translationService)
                    : ClassMarkupUtil.createMultipleNextNotification(remainingToday, languageSource, translationService);
        }

        // Nothing left today, so the day has to be named - a bare start time would read as if the class
        // were still today.
        for (int offset = 1; offset <= LOOKAHEAD_DAYS; offset++) {
            LocalDate date = today.plusDays(offset);
            List<ClassEntry> classes = firstNonEmptySlot(groupChat.getChatId(), telegramUserId, date, null, scope);
            if (!classes.isEmpty()) {
                return ClassMarkupUtil.createLaterDayNotification(classes, date.getDayOfWeek(),
                        languageSource, translationService);
            }
        }
        return plain(translationService.getMessage("class.next.none", languageSource));
    }

    /**
     * The earliest slot of the date that still has classes the member attends.
     *
     * @param after take only slots starting after this time, or null for the whole day
     */
    private List<ClassEntry> firstNonEmptySlot(Long chatId, Long telegramUserId, LocalDate date,
                                               LocalTime after, Scope scope) {
        Map<LocalTime, List<ClassEntry>> slots = slotsOn(chatId, date);
        if (slots == null) {
            return List.of();
        }
        for (Map.Entry<LocalTime, List<ClassEntry>> slot : slots.entrySet()) {
            if (after != null && !slot.getKey().isAfter(after)) {
                continue;
            }
            List<ClassEntry> mine = filter(chatId, telegramUserId, slot.getValue(), scope);
            if (!mine.isEmpty()) {
                return mine;
            }
        }
        return List.of();
    }

    private List<ClassEntry> filter(Long chatId, Long telegramUserId, List<ClassEntry> slot, Scope scope) {
        if (scope == Scope.ALL) {
            return slot;
        }
        return personalTimetableSupport.personalDay(chatId, telegramUserId, slot);
    }

    /**
     * The date's classes bucketed by start time, in chronological order.
     *
     * @return null when the chat has no timetable for that week
     */
    private Map<LocalTime, List<ClassEntry>> slotsOn(Long chatId, LocalDate date) {
        TimetableEntry timetable;
        try {
            timetable = timetableService.findByChatIdAndWeekType(chatId, WeekValidator.determineWeekType(date));
        } catch (NoSuchEntityException e) {
            return null;
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        Map<LocalTime, List<ClassEntry>> slots = new TreeMap<>();
        for (DayEntry dayEntry : timetable.getDays()) {
            if (!dayOfWeek.equals(dayEntry.getDayName()) || dayEntry.getClassEntries() == null) {
                continue;
            }
            for (ClassEntry classEntry : dayEntry.getClassEntries()) {
                if (classEntry.getStartTime() != null) {
                    slots.computeIfAbsent(classEntry.getStartTime(), time -> new ArrayList<>()).add(classEntry);
                }
            }
        }
        return slots;
    }

    /**
     * Hangs the scope button off whatever keyboard the markup util produced, which may be none.
     */
    private void appendSwitchRow(TextMessage message, Long groupChatId, String commandType, Scope scope,
                                 boolean personalAvailable, LumiosChat languageSource) {
        InlineKeyboardRow switchRow = viewSupport.switchRow(groupChatId, commandType, 1, scope,
                personalAvailable, languageSource);
        if (switchRow == null) {
            return;
        }

        List<InlineKeyboardRow> rows = new ArrayList<>();
        ReplyKeyboard existing = message.getReplyKeyboard();
        if (existing instanceof InlineKeyboardMarkup inline) {
            rows.addAll(inline.getKeyboard());
        }
        rows.add(switchRow);
        message.setReplyKeyboard(new InlineKeyboardMarkup(rows));
    }

    private TextMessage plain(String text) {
        TextMessage message = new TextMessage();
        message.setText(text);
        message.setParseMode(ParseMode.MARKDOWN);
        return message;
    }

    private TextMessage message(Message replyTo, String text, InlineKeyboardMarkup keyboard) {
        TextMessage message = new TextMessage();
        message.setChatId(replyTo.getChatId());
        message.setMessageId(replyTo.getMessageId());
        message.setText(text);
        message.setParseMode(ParseMode.MARKDOWN);
        message.setReplyKeyboard(keyboard);
        return message;
    }
}
