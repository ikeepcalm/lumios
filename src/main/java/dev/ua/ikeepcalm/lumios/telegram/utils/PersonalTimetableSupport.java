package dev.ua.ikeepcalm.lumios.telegram.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.ChatService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.PersonalTimetableService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.TimetableService;
import dev.ua.ikeepcalm.lumios.database.dal.interfaces.UserService;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.personal.TimetableMember;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.WeekType;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.TelegramClient;
import dev.ua.ikeepcalm.lumios.telegram.utils.ElectiveDetector.Elective;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * Shared plumbing behind {@code /mine}: resolving which group a member is configuring, working out
 * that group's electives, and rendering the picker.
 */
@Component
public class PersonalTimetableSupport {

    private static final Logger log = LoggerFactory.getLogger(PersonalTimetableSupport.class);

    /**
     * Which subjects are electives changes only on re-import, but the reminder scheduler asks every
     * minute, so the answer is cached. {@link #invalidateElectives} clears it after an import.
     */
    private final Cache<Long, Set<String>> electiveCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

    private final TimetableService timetableService;
    private final UserService userService;
    private final ChatService chatService;
    private final PersonalTimetableService personalTimetableService;
    private final TranslationService translationService;
    private final TelegramClient telegramClient;

    public PersonalTimetableSupport(TimetableService timetableService, UserService userService, ChatService chatService,
                                    PersonalTimetableService personalTimetableService,
                                    TranslationService translationService, TelegramClient telegramClient) {
        this.timetableService = timetableService;
        this.userService = userService;
        this.chatService = chatService;
        this.personalTimetableService = personalTimetableService;
        this.translationService = translationService;
        this.telegramClient = telegramClient;
    }

    /**
     * Group chats this member belongs to that actually have an imported timetable. Private chats are
     * excluded - a member's own chat with the bot is not a study group.
     */
    @Transactional(readOnly = true)
    public List<LumiosChat> timetabledGroupsOf(Long telegramUserId) {
        Map<Long, LumiosChat> groups = new LinkedHashMap<>();
        for (LumiosUser membership : userService.findById(telegramUserId)) {
            LumiosChat chat = membership.getChat();
            if (chat == null || chat.getChatId() == null || chat.getChatId() > 0) {
                continue;
            }
            if (!timetablesOf(chat.getChatId()).isEmpty()) {
                groups.putIfAbsent(chat.getChatId(), chat);
            }
        }
        return new ArrayList<>(groups.values());
    }

    /**
     * Both halves of a chat's two-week timetable, with days and classes attached.
     */
    @Transactional(readOnly = true)
    public List<TimetableEntry> timetablesOf(Long chatId) {
        List<TimetableEntry> timetables = new ArrayList<>(2);
        for (WeekType weekType : new WeekType[]{WeekType.WEEK_A, WeekType.WEEK_B}) {
            try {
                TimetableEntry timetable = timetableService.findByChatIdAndWeekTypeWithDays(chatId, weekType);
                if (timetable != null) {
                    timetables.add(timetable);
                }
            } catch (NoSuchEntityException e) {
                log.debug("Chat {} has no {} timetable", chatId, weekType);
            }
        }
        return timetables;
    }

    public List<Elective> electivesOf(Long chatId) {
        return ElectiveDetector.electives(timetablesOf(chatId));
    }

    public Set<String> choiceKeysOf(Long chatId) {
        return electiveCache.get(chatId, id -> ElectiveDetector.choiceKeys(timetablesOf(id)));
    }

    public void invalidateElectives(Long chatId) {
        electiveCache.invalidate(chatId);
    }

    /**
     * The classes in this slot that the member attends: everything shared, plus the optional ones they
     * picked.
     */
    public List<ClassEntry> personalClasses(Long chatId, Long telegramUserId, List<ClassEntry> slotClasses) {
        return ElectiveDetector.personalise(slotClasses, choiceKeysOf(chatId),
                personalTimetableService.chosenSubjects(chatId, telegramUserId));
    }

    /**
     * Same, for a caller that already holds the whole chat's choices. The reminder scheduler runs once
     * a minute over every slot of every chat, so it must not ask the database per member.
     */
    public List<ClassEntry> personalClasses(Long chatId, List<ClassEntry> slotClasses, Set<String> chosen) {
        return ElectiveDetector.personalise(slotClasses, choiceKeysOf(chatId), chosen);
    }

    /**
     * Filters a whole day, one slot at a time.
     * <p>
     * Whether a slot offers a choice - and so whether an undecided member should keep seeing all of it -
     * is a property of the slot, not of the day, so the day cannot be filtered in one pass. Slots are
     * bucketed by start time alone, the way {@link ElectiveDetector} reads them; grouping by the
     * "start - end" label instead would split a pool whose options run for different lengths.
     *
     * @return the member's classes, in start-time order
     */
    public List<ClassEntry> personalDay(Long chatId, List<ClassEntry> dayClasses, Set<String> chosen) {
        Map<LocalTime, List<ClassEntry>> bySlot = new TreeMap<>();
        for (ClassEntry classEntry : dayClasses) {
            if (classEntry.getStartTime() != null) {
                bySlot.computeIfAbsent(classEntry.getStartTime(), time -> new ArrayList<>()).add(classEntry);
            }
        }

        List<ClassEntry> mine = new ArrayList<>();
        for (List<ClassEntry> slot : bySlot.values()) {
            mine.addAll(personalClasses(chatId, slot, chosen));
        }
        return mine;
    }

    public List<ClassEntry> personalDay(Long chatId, Long telegramUserId, List<ClassEntry> dayClasses) {
        return personalDay(chatId, dayClasses,
                personalTimetableService.chosenSubjects(chatId, telegramUserId));
    }

    /**
     * Whether it is worth offering this member a personal view at all: the chat has to have something
     * optional in it, and they have to have said which of it is theirs.
     */
    public boolean personalAvailable(Long chatId, Long telegramUserId) {
        return !choiceKeysOf(chatId).isEmpty()
               && !personalTimetableService.chosenSubjects(chatId, telegramUserId).isEmpty();
    }

    /**
     * Rendered mentions for the members who want to be tagged on the group announcement, keyed by the
     * choice key of the class that should carry them.
     * <p>
     * Only optional classes get an entry. A shared class is attended by everyone reading the message,
     * so tagging it would mention every opted-in member on every announcement and teach the group to
     * mute the bot.
     *
     * @return an empty map when nobody in the chat asked to be tagged
     */
    public Map<String, List<String>> tagsBySubject(LumiosChat groupChat, List<ClassEntry> slotClasses) {
        ElectiveDetector.SlotShape shape = ElectiveDetector.shapeOf(slotClasses);
        if (!shape.offersChoice()) {
            return Map.of();
        }

        List<TimetableMember> taggers = personalTimetableService.remindableMembers(groupChat.getChatId()).stream()
                .filter(member -> member.getReminderChannel().tagsInGroup())
                .toList();
        if (taggers.isEmpty()) {
            return Map.of();
        }

        Map<Long, Set<String>> choices = personalTimetableService.chosenSubjectsByMember(groupChat.getChatId());
        Map<Long, String> names = displayNames(groupChat);
        Map<String, List<String>> tags = new LinkedHashMap<>();
        for (ClassEntry classEntry : slotClasses) {
            String key = ElectiveDetector.choiceKey(classEntry, shape.subgroup());
            if (tags.containsKey(key)) {
                continue;
            }
            for (TimetableMember member : taggers) {
                Long userId = member.getTelegramUserId();
                if (choices.getOrDefault(userId, Set.of()).contains(key)) {
                    tags.computeIfAbsent(key, id -> new ArrayList<>())
                            .add(MessageFormatter.formatMentionLink(names.get(userId), userId));
                }
            }
        }
        return tags;
    }

    /**
     * What to call each member of the chat. {@code LumiosChat.users} is eagerly loaded, so this costs
     * no extra query.
     */
    private Map<Long, String> displayNames(LumiosChat groupChat) {
        Map<Long, String> names = new HashMap<>();
        if (groupChat.getUsers() == null) {
            return names;
        }
        for (LumiosUser member : groupChat.getUsers()) {
            if (member.getUserId() == null) {
                continue;
            }
            String name = member.getFullName() == null || member.getFullName().isBlank()
                    ? member.getUsername()
                    : member.getFullName();
            names.putIfAbsent(member.getUserId(), name);
        }
        return names;
    }

    /**
     * The chat whose language a private message to this member should be written in.
     * <p>
     * A member's own chat with the bot carries their language choice; the group's carries the group's.
     * A personal reminder is read by one person, so theirs wins where we have it.
     */
    public LumiosChat languageChatFor(Long telegramUserId, LumiosChat fallback) {
        LumiosChat privateChat = chatOrNull(telegramUserId);
        return privateChat == null ? fallback : privateChat;
    }

    public LumiosChat chatOrNull(Long chatId) {
        try {
            return chatService.findByChatId(chatId);
        } catch (NoSuchEntityException e) {
            return null;
        }
    }

    /**
     * Renders the picker into the member's private chat, either as a new message or by editing the
     * one they just tapped.
     *
     * @param editMessageId message to replace, or null to send a fresh one
     * @return false when the group has no electives to choose from
     */
    public boolean sendMenu(LumiosChat groupChat, Long telegramUserId, Long privateChatId, Integer editMessageId) {
        return sendMenu(groupChat, telegramUserId, privateChatId, editMessageId, 0);
    }

    public boolean sendMenu(LumiosChat groupChat, Long telegramUserId, Long privateChatId, Integer editMessageId, int page) {
        List<Elective> electives = electivesOf(groupChat.getChatId());
        LumiosChat languageSource = languageChatFor(telegramUserId, groupChat);

        if (electives.isEmpty()) {
            TextMessage message = new TextMessage();
            message.setChatId(privateChatId);
            message.setText(translationService.getMessage("mine.no-electives", languageSource,
                    groupName(groupChat)));
            telegramClient.sendTextMessage(message);
            return false;
        }

        Set<String> chosen = personalTimetableService.chosenSubjects(groupChat.getChatId(), telegramUserId);
        int safePage = ElectivePicker.clampPage(page, electives.size());

        String text = ElectivePicker.text(translationService, languageSource, groupName(groupChat),
                electives, chosen, safePage);
        var keyboard = ElectivePicker.keyboard(translationService, languageSource, groupChat.getChatId(),
                electives, chosen, safePage);

        if (editMessageId == null) {
            TextMessage message = new TextMessage();
            message.setChatId(privateChatId);
            message.setText(text);
            message.setReplyKeyboard(keyboard);
            telegramClient.sendTextMessage(message);
        } else {
            EditMessage edit = new EditMessage();
            edit.setChatId(privateChatId);
            edit.setMessageId(editMessageId);
            edit.setText(text);
            edit.setReplyKeyboard(keyboard);
            telegramClient.sendEditMessage(edit);
        }
        return true;
    }

    /**
     * Renders the reminder settings menu into the member's private chat, either as a new message or by
     * editing the one they just tapped.
     *
     * @param editMessageId message to replace, or null to send a fresh one
     * @return false when a new message could not be delivered - a member who never pressed Start cannot
     *         be written to, and telling them it worked would leave them waiting for a menu
     */
    public boolean sendReminderMenu(LumiosChat groupChat, Long telegramUserId, Long privateChatId, Integer editMessageId) {
        LumiosChat languageSource = languageChatFor(telegramUserId, groupChat);
        TimetableMember member = personalTimetableService.member(groupChat.getChatId(), telegramUserId);

        String text = ReminderSettingsPicker.text(translationService, languageSource, groupName(groupChat),
                member, groupChat);
        var keyboard = ReminderSettingsPicker.keyboard(translationService, languageSource,
                groupChat.getChatId(), member, groupChat);

        if (editMessageId == null) {
            TextMessage message = new TextMessage();
            message.setChatId(privateChatId);
            message.setText(text);
            message.setReplyKeyboard(keyboard);
            return telegramClient.sendTextMessage(message) != null;
        }

        EditMessage edit = new EditMessage();
        edit.setChatId(privateChatId);
        edit.setMessageId(editMessageId);
        edit.setText(text);
        edit.setReplyKeyboard(keyboard);
        telegramClient.sendEditMessage(edit);
        return true;
    }

    public String groupName(LumiosChat chat) {
        return chat.getName() == null || chat.getName().isBlank()
                ? String.valueOf(chat.getChatId())
                : chat.getName();
    }
}
