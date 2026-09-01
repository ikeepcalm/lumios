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
import dev.ua.ikeepcalm.lumios.telegram.utils.parsers.TimetableParser;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Renders one day of a timetable - the shared body of {@code /today} and {@code /tomorrow}, and of the
 * paging and scope buttons underneath them.
 * <p>
 * Both commands do exactly the same thing to a different date, and both now have to resolve a group
 * (they work in a private chat too), filter to the caller's own classes, and page what is left. Doing
 * that in one place is the only way the command and its callback stay in step.
 */
@Component
public class DayViewRenderer {

    private final TimetableService timetableService;
    private final TimetableViewSupport viewSupport;
    private final TranslationService translationService;
    private final TelegramClient telegramClient;

    public DayViewRenderer(TimetableService timetableService, TimetableViewSupport viewSupport,
                           TranslationService translationService, TelegramClient telegramClient) {
        this.timetableService = timetableService;
        this.viewSupport = viewSupport;
        this.translationService = translationService;
        this.telegramClient = telegramClient;
    }

    /**
     * Builds the answer to the command, resolving the group first and asking which one when the member
     * is in several. The caller sends it, so it keeps control of whether the reply is auto-deleted.
     */
    public TextMessage answer(Message message, LumiosChat chat, String commandType, LocalDate date) {
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
        View view = build(groupChat, chat, telegramUserId, commandType, date, 0, scope);

        if (view == null) {
            return message(message,
                    translationService.getMessage("command." + commandType + ".not-found", chat), null);
        }
        return message(message, view.text(), view.keyboard());
    }

    /**
     * Replaces an existing view in place, for the paging and scope buttons.
     *
     * @param page 1-based, clamped to what the view actually has
     */
    public void replace(Message shown, LumiosChat languageSource, LumiosChat groupChat, Long telegramUserId,
                        String commandType, LocalDate date, int page, Scope scope) {
        View view = build(groupChat, languageSource, telegramUserId, commandType, date, page, scope);
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
     * @param page 0 asks for whichever slot is current, which is what a member wants when they type the
     *             command mid-morning; anything else is taken literally
     * @return null when the chat has no timetable at all
     */
    private View build(LumiosChat groupChat, LumiosChat languageSource, Long telegramUserId,
                       String commandType, LocalDate date, int page, Scope scope) {
        List<ClassEntry> dayClasses = classesOn(groupChat.getChatId(), date);
        if (dayClasses == null) {
            return null;
        }

        String title = translationService.getMessage("command." + commandType + ".title", languageSource);
        boolean personalAvailable = viewSupport.personalAvailable(groupChat.getChatId(), telegramUserId);
        // A payload can outlive the choices it was built for - a member who clears theirs while an old
        // keyboard is still on screen has nothing left to filter by, so the scope collapses to ALL.
        scope = personalAvailable ? scope : Scope.ALL;
        List<ClassEntry> mine = viewSupport.filterDay(groupChat.getChatId(), telegramUserId, dayClasses, scope);

        // An empty day still carries the scope button. Otherwise a member who switches to their own
        // classes on a day they have none is left looking at a message with no way back.
        if (mine.isEmpty()) {
            return new View(translationService.getMessage("command." + commandType + ".no-classes", languageSource)
                            + viewSupport.footer(scope, personalAvailable, languageSource),
                    onlySwitch(groupChat.getChatId(), commandType, scope, personalAvailable, languageSource));
        }

        if (groupChat.isPlainTimetableEnabled()) {
            return new View(TimetablePagedUtil.buildPlainDayMessage(mine, title)
                            + viewSupport.footer(scope, personalAvailable, languageSource),
                    onlySwitch(groupChat.getChatId(), commandType, scope, personalAvailable, languageSource));
        }

        Map<String, List<ClassEntry>> groupedByTime = TimetableParser.groupClassesByTime(mine);
        List<String> timeSlots = new ArrayList<>(groupedByTime.keySet());
        int maxPage = timeSlots.size();
        int safePage = page <= 0
                ? TimetablePagedUtil.calculateCurrentPage(groupedByTime)
                : Math.clamp(page, 1, maxPage);

        String text = TimetablePagedUtil.buildPagedTimetableMessage(groupedByTime, safePage, title,
                translationService, languageSource) + viewSupport.footer(scope, personalAvailable, languageSource);
        InlineKeyboardMarkup keyboard = viewSupport.keyboard(
                TimetablePagedUtil.buildClassButtons(groupedByTime.get(timeSlots.get(safePage - 1))),
                groupChat.getChatId(), commandType, safePage, maxPage, scope, personalAvailable, languageSource);
        return new View(text, keyboard);
    }

    /**
     * The day's classes in start-time order, from whichever half of the two-week cycle the date falls in.
     *
     * @return null when the chat has no timetable for that week
     */
    private List<ClassEntry> classesOn(Long chatId, LocalDate date) {
        TimetableEntry timetable;
        try {
            timetable = timetableService.findByChatIdAndWeekType(chatId, WeekValidator.determineWeekType(date));
        } catch (NoSuchEntityException e) {
            return null;
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<ClassEntry> classes = new ArrayList<>();
        for (DayEntry dayEntry : timetable.getDays()) {
            if (dayOfWeek.equals(dayEntry.getDayName()) && dayEntry.getClassEntries() != null) {
                classes.addAll(dayEntry.getClassEntries());
            }
        }
        // Paging walks the slots in the order they arrive, and the rows come back in whatever order the
        // database chose - so without this the pages can run backwards through the day.
        classes.sort(Comparator.comparing(ClassEntry::getStartTime,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return classes;
    }

    /**
     * A keyboard with nothing on it but the scope button - what an empty or unpaged day needs.
     */
    private InlineKeyboardMarkup onlySwitch(Long groupChatId, String commandType, Scope scope,
                                            boolean personalAvailable, LumiosChat languageSource) {
        if (!personalAvailable) {
            return null;
        }
        return viewSupport.keyboard(List.of(), groupChatId, commandType, 1, 1, scope,
                personalAvailable, languageSource);
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

    private record View(String text, InlineKeyboardMarkup keyboard) {
    }
}
