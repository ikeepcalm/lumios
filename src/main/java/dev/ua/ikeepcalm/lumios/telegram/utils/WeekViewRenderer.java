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
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Renders {@code /week}: one day per page, skipping the days with nothing on them.
 * <p>
 * Under a personal view a day can empty out entirely - a day whose only classes are other people's
 * electives - so which days have pages at all depends on who is asking.
 */
@Component
public class WeekViewRenderer {

    private static final String COMMAND_TYPE = "week";

    private final TimetableService timetableService;
    private final TimetableViewSupport viewSupport;
    private final TranslationService translationService;
    private final TelegramClient telegramClient;

    public WeekViewRenderer(TimetableService timetableService, TimetableViewSupport viewSupport,
                            TranslationService translationService, TelegramClient telegramClient) {
        this.timetableService = timetableService;
        this.viewSupport = viewSupport;
        this.translationService = translationService;
        this.telegramClient = telegramClient;
    }

    public TextMessage answer(Message message, LumiosChat chat) {
        Long telegramUserId = message.getFrom().getId();
        Target target = viewSupport.target(message, chat, telegramUserId);

        if (target.ambiguous()) {
            return message(message, translationService.getMessage("view.choose-group", chat),
                    viewSupport.chooseGroupKeyboard(target.chooseFrom(), COMMAND_TYPE));
        }
        if (!target.resolved()) {
            return message(message, translationService.getMessage("mine.no-groups", chat), null);
        }

        LumiosChat groupChat = target.groupChat();
        Scope scope = viewSupport.scopeFrom(message, groupChat.getChatId(), telegramUserId);
        View view = build(groupChat, chat, telegramUserId, 1, scope);

        if (view == null) {
            return message(message, translationService.getMessage("command.week.not-found", chat), null);
        }
        return message(message, view.text(), view.keyboard());
    }

    public void replace(MaybeInaccessibleMessage shown, LumiosChat languageSource, LumiosChat groupChat, Long telegramUserId,
                        int page, Scope scope) {
        View view = build(groupChat, languageSource, telegramUserId, page, scope);
        if (view == null) {
            return;
        }

        EditMessage edit = new EditMessage();
        if (shown.getChatId() == null) {
            return;
        }
        edit.setChatId(shown.getChatId());
        if (shown.getMessageId() == null) {
            return;
        }
        edit.setMessageId(shown.getMessageId());
        edit.setText(view.text());
        edit.setParseMode(ParseMode.MARKDOWN);
        edit.setReplyKeyboard(view.keyboard());
        telegramClient.sendEditMessage(edit);
    }

    public void replace(Message shown, LumiosChat languageSource, LumiosChat groupChat, Long telegramUserId,
                        int page, Scope scope) {
        View view = build(groupChat, languageSource, telegramUserId, page, scope);
        if (view == null) {
            return;
        }

        EditMessage edit = new EditMessage();
        edit.setChatId(shown.getChatId());
        edit.setMessageId(shown.getMessageId());
        edit.setText(view.text());
        edit.setParseMode(ParseMode.MARKDOWN);
        edit.setReplyKeyboard(view.keyboard());
        telegramClient.sendEditMessage(edit);
    }

    /**
     * @return null when the chat has no timetable for the current week
     */
    private View build(LumiosChat groupChat, LumiosChat languageSource, Long telegramUserId, int page, Scope scope) {
        TimetableEntry timetable;
        try {
            timetable = timetableService.findByChatIdAndWeekType(groupChat.getChatId(),
                    WeekValidator.determineWeekDay());
        } catch (NoSuchEntityException e) {
            return null;
        }

        boolean personalAvailable = viewSupport.personalAvailable(groupChat.getChatId(), telegramUserId);
        // See DayViewRenderer: a payload can outlive the choices it was built for.
        scope = personalAvailable ? scope : Scope.ALL;
        List<Day> days = new ArrayList<>();
        for (DayEntry dayEntry : timetable.getDays().stream()
                .sorted(Comparator.comparingInt(day -> day.getDayName().getValue()))
                .toList()) {
            List<ClassEntry> classes = viewSupport.filterDay(groupChat.getChatId(), telegramUserId,
                    sorted(dayEntry.getClassEntries()), scope);
            if (!classes.isEmpty()) {
                days.add(new Day(dayEntry.getDayName(), classes));
            }
        }

        // An empty week keeps the scope button, or a member who switches to their own classes in a week
        // they have none is left with no way back.
        if (days.isEmpty()) {
            return new View(translationService.getMessage("command.week.no-classes", languageSource)
                            + viewSupport.footer(scope, personalAvailable, languageSource),
                    personalAvailable
                            ? viewSupport.keyboard(List.of(), groupChat.getChatId(), COMMAND_TYPE, 1, 1,
                                    scope, personalAvailable, languageSource)
                            : null);
        }

        int safePage = Math.clamp(page, 1, days.size());
        Day day = days.get(safePage - 1);
        String text = TimetablePagedUtil.buildWeekDayMessage(day.dayName(), day.classes(), safePage, days.size(),
                translationService, languageSource)
                      + viewSupport.footer(scope, personalAvailable, languageSource);
        InlineKeyboardMarkup keyboard = viewSupport.keyboard(List.of(), groupChat.getChatId(), COMMAND_TYPE,
                safePage, days.size(), scope, personalAvailable, languageSource);
        return new View(text, keyboard);
    }

    private List<ClassEntry> sorted(List<ClassEntry> classes) {
        if (classes == null) {
            return List.of();
        }
        return classes.stream()
                .sorted(Comparator.comparing(ClassEntry::getStartTime,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
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

    private record Day(DayOfWeek dayName, List<ClassEntry> classes) {
    }

    private record View(String text, InlineKeyboardMarkup keyboard) {
    }
}
