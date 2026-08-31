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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public Set<String> electiveSubjectsOf(Long chatId) {
        return electiveCache.get(chatId, id -> ElectiveDetector.electiveSubjects(timetablesOf(id)));
    }

    public void invalidateElectives(Long chatId) {
        electiveCache.invalidate(chatId);
    }

    /**
     * The classes in this slot that the member attends: everything mandatory, plus their electives.
     */
    public List<ClassEntry> personalClasses(Long chatId, Long telegramUserId, List<ClassEntry> slotClasses) {
        return ElectiveDetector.personalise(slotClasses, electiveSubjectsOf(chatId),
                personalTimetableService.chosenSubjects(chatId, telegramUserId));
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
        LumiosChat languageSource = groupChat;

        if (electives.isEmpty()) {
            TextMessage message = new TextMessage();
            message.setChatId(privateChatId);
            message.setText(translationService.getMessage("mine.no-electives", languageSource,
                    groupName(groupChat)));
            telegramClient.sendTextMessage(message);
            return false;
        }

        TimetableMember member = personalTimetableService.member(groupChat.getChatId(), telegramUserId);
        Set<String> chosen = personalTimetableService.chosenSubjects(groupChat.getChatId(), telegramUserId);
        int safePage = ElectivePicker.clampPage(page, electives.size());

        String text = ElectivePicker.text(translationService, languageSource, groupName(groupChat),
                electives, chosen, member.isDmRemindersEnabled(), safePage);
        var keyboard = ElectivePicker.keyboard(translationService, languageSource, groupChat.getChatId(),
                electives, chosen, member.isDmRemindersEnabled(), safePage);

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

    public String groupName(LumiosChat chat) {
        return chat.getName() == null || chat.getName().isBlank()
                ? String.valueOf(chat.getChatId())
                : chat.getName();
    }
}
