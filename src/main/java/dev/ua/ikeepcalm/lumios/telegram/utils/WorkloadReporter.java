package dev.ua.ikeepcalm.lumios.telegram.utils;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.PersonalTimetableService;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.ClassEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.DayEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.TimetableEntry;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.ClassType;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.WeekType;
import dev.ua.ikeepcalm.lumios.telegram.TelegramClient;
import dev.ua.ikeepcalm.lumios.telegram.ai.Gemini;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * What {@code /due} answers: an estimate of the work the caller probably has to do over the next two
 * weeks, read out of their own timetable by Gemini.
 * <p>
 * The bot has no idea what any individual assignment is - nobody ever fills that in, which is why the
 * hand-written task tracker this replaced went unused. What it does know is the shape of the fortnight,
 * and the shape says most of it: a lab has to be finished <i>before</i> the class it is defended at,
 * a seminar wants preparation and attendance, a lecture wants neither. Feeding that to the model and
 * letting it weigh the subjects gets a useful answer out of data the bot already has.
 * <p>
 * The agenda is built entirely inside the caller's transaction, before the model is asked anything -
 * the reply arrives on another thread, where no lazy collection could be read.
 */
@Component
public class WorkloadReporter {

    private static final Logger log = LoggerFactory.getLogger(WorkloadReporter.class);

    /**
     * Today plus the rest of this week and all of the next. Counted from Monday so that a {@code /due}
     * typed on a Friday still covers a whole working week ahead, rather than two days.
     */
    private static final int DAYS_FROM_MONDAY = 13;

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final PersonalTimetableSupport personalSupport;
    private final PersonalTimetableService personalTimetableService;
    private final TranslationService translationService;
    private final TelegramClient telegramClient;
    private final Gemini gemini;

    public WorkloadReporter(PersonalTimetableSupport personalSupport,
                            PersonalTimetableService personalTimetableService,
                            TranslationService translationService, TelegramClient telegramClient,
                            Gemini gemini) {
        this.personalSupport = personalSupport;
        this.personalTimetableService = personalTimetableService;
        this.translationService = translationService;
        this.telegramClient = telegramClient;
        this.gemini = gemini;
    }

    /**
     * Sends the estimate into {@code targetChatId}, or an explanation of why there is none.
     *
     * @param languageSource the chat whose language the answer is written in - the caller's own, in a
     *                       private chat
     */
    @Transactional(readOnly = true)
    public void report(Long targetChatId, LumiosChat groupChat, LumiosChat languageSource, Long telegramUserId) {
        String agenda = agenda(groupChat.getChatId(), telegramUserId, languageSource);
        if (agenda.isEmpty()) {
            send(targetChatId, translationService.getMessage("command.due.no-classes", languageSource), null);
            return;
        }

        typing(targetChatId);
        gemini.getSingleResponse(prompt(agenda, languageSource), systemInstruction(languageSource), languageSource)
                .thenAccept(response -> {
                    if (response == null || response.isBlank()) {
                        send(targetChatId, translationService.getMessage("command.due.error", languageSource), null);
                        return;
                    }
                    for (String chunk : MessageFormatter.chunkMessage(response, ParseMode.MARKDOWNV2,
                            translationService, languageSource)) {
                        send(targetChatId, chunk, ParseMode.MARKDOWNV2);
                    }
                })
                .exceptionally(throwable -> {
                    log.error("Failed to estimate the workload of member {} in chat {}", telegramUserId,
                            groupChat.getChatId(), throwable);
                    send(targetChatId, translationService.getMessage("command.due.error", languageSource), null);
                    return null;
                });
    }

    /**
     * The caller's own classes for the fortnight, one line each, grouped by day. Days they have nothing
     * on are left out entirely - an empty line teaches the model nothing and costs tokens.
     *
     * @return an empty string when the chat has no timetable, or the caller has nothing in it
     */
    private String agenda(Long chatId, Long telegramUserId, LumiosChat languageSource) {
        Map<WeekType, TimetableEntry> byWeek = new EnumMap<>(WeekType.class);
        for (TimetableEntry timetable : personalSupport.timetablesOf(chatId)) {
            byWeek.put(timetable.getWeekType(), timetable);
        }
        if (byWeek.isEmpty()) {
            return "";
        }

        Set<String> chosen = personalTimetableService.chosenSubjects(chatId, telegramUserId);
        Locale locale = Locale.forLanguageTag(languageSource.getLanguage());
        LocalDate today = TimetableClock.today();
        LocalDate last = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusDays(DAYS_FROM_MONDAY);

        StringBuilder agenda = new StringBuilder();
        for (LocalDate date = today; !date.isAfter(last); date = date.plusDays(1)) {
            TimetableEntry timetable = byWeek.get(WeekValidator.determineWeekType(date));
            if (timetable == null) {
                continue;
            }

            List<ClassEntry> mine = personalSupport.personalDay(chatId, classesOn(timetable, date.getDayOfWeek()), chosen);
            if (mine.isEmpty()) {
                continue;
            }

            agenda.append(date.getDayOfWeek().getDisplayName(TextStyle.FULL, locale))
                    .append(", ").append(DATE.format(date)).append(":\n");
            for (ClassEntry classEntry : mine) {
                agenda.append("  ").append(time(classEntry)).append(" [").append(type(classEntry)).append("] ")
                        .append(classEntry.getName() == null ? "?" : classEntry.getName().trim()).append("\n");
            }
        }
        return agenda.toString();
    }

    private List<ClassEntry> classesOn(TimetableEntry timetable, DayOfWeek dayOfWeek) {
        List<ClassEntry> classes = new ArrayList<>();
        if (timetable.getDays() == null) {
            return classes;
        }
        for (DayEntry day : timetable.getDays()) {
            if (dayOfWeek.equals(day.getDayName()) && day.getClassEntries() != null) {
                classes.addAll(day.getClassEntries());
            }
        }
        return classes;
    }

    private String time(ClassEntry classEntry) {
        return classEntry.getStartTime() == null ? "--:--" : TIME.format(classEntry.getStartTime());
    }

    /**
     * Class types are spelled out in English whatever the answer's language: they are the one part of
     * the agenda the model has to reason about precisely, and {@link ClassType} already names them.
     */
    private String type(ClassEntry classEntry) {
        ClassType classType = classEntry.getClassType();
        return classType == null ? ClassType.UNKNOWN.name() : classType.name();
    }

    private String systemInstruction(LumiosChat languageSource) {
        return isEnglish(languageSource)
                ? "You help a university student plan their week. Answer in English, in Telegram Markdown. "
                  + "Be concrete and short; no preamble, no closing pleasantries."
                : "Ти допомагаєш студенту спланувати тиждень. Відповідай українською, у розмітці Telegram Markdown. "
                  + "Пиши конкретно і стисло, без вступів і без прощань.";
    }

    private String prompt(String agenda, LumiosChat languageSource) {
        if (isEnglish(languageSource)) {
            return """
                    Here is a student's personal timetable for this week and the next. It already contains \
                    only the subjects they actually attend.

                    %s
                    Work out what they most likely have to prepare over this period, and give them a \
                    prioritised list.

                    Rules for reading the timetable:
                    - A LECTURE needs no preparation and can be skipped. Mention one only if it matters.
                    - A LAB requires the work to be finished BEFORE the class, because it is defended there. \
                    These are the hardest deadlines.
                    - A PRACTICE or seminar requires both preparation and attendance.
                    - Subjects whose names sound hard and technical (for instance "Computer modelling of \
                    discrete-event systems") outrank easy humanities ones (for instance "Human rights and \
                    freedoms").
                    - You do not know the real assignments. Phrase everything as what is likely, not as fact.

                    Answer with:
                    - one short opening line;
                    - bullet points grouped by priority (🔴 high, 🟡 medium, 🟢 low), each naming the subject, \
                    the date it is due by, and what probably has to be done;
                    - at most 12 bullets, no explanations afterwards.
                    """.formatted(agenda);
        }
        return """
                Ось особистий розклад студента на цей і наступний тиждень. У ньому вже лише ті дисципліни, \
                які він справді відвідує.

                %s
                Визнач, що йому найімовірніше треба підготувати за цей період, і склади список пріоритетів.

                Як читати розклад:
                - LECTURE (лекція) не потребує підготовки, її можна пропустити. Згадуй лише якщо це важливо.
                - LAB (лабораторна) вимагає, щоб роботу було виконано ДО пари, бо саме на ній її захищають. \
                Це найжорсткіші дедлайни.
                - PRACTICE (практичне чи семінарське заняття) вимагає і підготовки, і присутності.
                - Дисципліни, назви яких звучать складно і технічно (наприклад «Комп'ютерне моделювання \
                подійно-дискретних систем»), мають вищий пріоритет за легкі гуманітарні (наприклад «Права \
                і свободи людини»).
                - Ти не знаєш реальних завдань. Формулюй усе як імовірне, а не як факт.

                Відповідь має містити:
                - один короткий вступний рядок;
                - пункти, згруповані за пріоритетом (🔴 високий, 🟡 середній, 🟢 низький), у кожному: \
                дисципліна, дата, до якої треба встигнути, і що саме ймовірно треба зробити;
                - не більше 12 пунктів, без пояснень після них.
                """.formatted(agenda);
    }

    private boolean isEnglish(LumiosChat chat) {
        return chat != null && "en".equals(chat.getLanguage());
    }

    private void typing(Long chatId) {
        try {
            telegramClient.execute(SendChatAction.builder()
                    .action(String.valueOf(ActionType.TYPING))
                    .chatId(chatId)
                    .build());
        } catch (TelegramApiException e) {
            log.debug("Could not send the typing action to chat {}", chatId, e);
        }
    }

    private void send(Long chatId, String text, String parseMode) {
        TextMessage message = new TextMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setParseMode(parseMode);
        telegramClient.sendTextMessage(message);
    }
}
