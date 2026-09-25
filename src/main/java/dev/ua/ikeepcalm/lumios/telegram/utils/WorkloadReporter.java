package dev.ua.ikeepcalm.lumios.telegram.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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
import org.json.JSONArray;
import org.json.JSONObject;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * What {@code /due} answers: what the caller probably has to get done over the coming fortnight,
 * worked out from their own timetable by Gemini.
 * <p>
 * The bot has no idea what any individual assignment is - nobody ever fills that in, which is why the
 * hand-written task tracker this replaced went unused. What it does know is the shape of the fortnight,
 * and the shape says most of it: a lab has to be finished <i>before</i> the class it is defended at,
 * and a seminar wants preparation as well as attendance. Lectures are left out of the agenda entirely
 * - they need no preparation, so a model that can see them only spends words on them.
 * <p>
 * The model is asked for JSON and never for prose. Telegram rejects most of the Markdown dialects an
 * LLM writes unprompted - and the client then retries without a parse mode, so the reader sees raw
 * {@code **asterisks**} - whereas a fixed layout rendered here escapes cleanly every time and reads the
 * same on every invocation.
 * <p>
 * The agenda is built entirely inside the caller's transaction, before the model is asked anything -
 * the reply arrives on another thread, where no lazy collection could be read.
 */
@Component
public class WorkloadReporter {

    private static final Logger log = LoggerFactory.getLogger(WorkloadReporter.class);

    /**
     * How far ahead to look, counted from this Monday so that a {@code /due} typed on a Friday still
     * covers a whole working week rather than the two days left of this one. Days already past are
     * dropped when the agenda is walked, so late in the week the answer is about next week by itself.
     */
    private static final int DAYS_FROM_MONDAY = 13;

    /**
     * Long enough that {@code /due /due /due} costs one request, short enough that a member who fixes
     * their electives sees the difference within a lesson. Re-picking electives moves the key anyway.
     */
    private static final long CACHE_MINUTES = 15;

    private static final int MAX_ITEMS = 8;

    private static final DateTimeFormatter DAY_MONTH = DateTimeFormatter.ofPattern("dd.MM");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Keyed on the day and on what the member attends, so it expires by itself at midnight and the
     * moment they change an elective - neither needs an explicit invalidation.
     */
    private final Cache<ReportKey, CompletableFuture<String>> reports = Caffeine.newBuilder()
            .expireAfterWrite(CACHE_MINUTES, TimeUnit.MINUTES)
            .maximumSize(500)
            .build();

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
        Set<String> chosen = personalTimetableService.chosenSubjects(groupChat.getChatId(), telegramUserId);
        LocalDate today = TimetableClock.today();
        String agenda = agenda(groupChat.getChatId(), chosen, today, languageSource);

        if (agenda.isEmpty()) {
            send(targetChatId, translationService.getMessage("command.due.no-classes", languageSource), null);
            return;
        }

        ReportKey key = new ReportKey(groupChat.getChatId(), telegramUserId, today, chosen.hashCode());
        // Caffeine runs the mapping function under a per-key lock, so three /due in a row share one
        // future - and therefore one request - rather than racing to ask the same question.
        CompletableFuture<String> report = reports.get(key, id -> {
            typing(targetChatId);
            return gemini.getSingleResponse(prompt(agenda, today, languageSource),
                            systemInstruction(languageSource), languageSource, true)
                    .thenApply(response -> render(response, languageSource, today))
                    .whenComplete((rendered, failure) -> {
                        // A cached failure would keep answering "try again later" for a quarter of an
                        // hour; drop it so the next /due really does try again.
                        if (failure != null) {
                            reports.invalidate(id);
                        }
                    });
        });

        report.thenAccept(text -> send(targetChatId, text, ParseMode.MARKDOWNV2))
                .exceptionally(throwable -> {
                    log.error("Failed to estimate the workload of member {} in chat {}", telegramUserId,
                            groupChat.getChatId(), throwable);
                    send(targetChatId, translationService.getMessage("command.due.error", languageSource), null);
                    return null;
                });
    }

    /**
     * The caller's own classes for the fortnight, one line each, grouped by day.
     * <p>
     * Lectures are dropped: nothing is prepared for them, and leaving them in only invites the model to
     * pad the answer. Days with nothing left on them are skipped, so what reaches the model already
     * reflects how much of the week is gone.
     *
     * @return an empty string when the chat has no timetable, or nothing is left to prepare for
     */
    private String agenda(Long chatId, Set<String> chosen, LocalDate today, LumiosChat languageSource) {
        Map<WeekType, TimetableEntry> byWeek = new EnumMap<>(WeekType.class);
        for (TimetableEntry timetable : personalSupport.timetablesOf(chatId)) {
            byWeek.put(timetable.getWeekType(), timetable);
        }
        if (byWeek.isEmpty()) {
            return "";
        }

        Locale locale = Locale.forLanguageTag(languageSource.getLanguage());
        LocalDate last = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusDays(DAYS_FROM_MONDAY);

        StringBuilder agenda = new StringBuilder();
        for (LocalDate date = today; !date.isAfter(last); date = date.plusDays(1)) {
            TimetableEntry timetable = byWeek.get(WeekValidator.determineWeekType(date));
            if (timetable == null) {
                continue;
            }

            List<ClassEntry> mine = personalSupport.personalDay(chatId, classesOn(timetable, date.getDayOfWeek()), chosen)
                    .stream()
                    .filter(classEntry -> classEntry.getClassType() != ClassType.LECTURE)
                    .toList();
            if (mine.isEmpty()) {
                continue;
            }

            agenda.append(date.getDayOfWeek().getDisplayName(TextStyle.FULL, locale))
                    .append(" ").append(DAY_MONTH.format(date)).append(" (").append(date).append("):\n");
            for (ClassEntry classEntry : mine) {
                agenda.append("  ").append(time(classEntry)).append(" [").append(type(classEntry)).append("] ")
                        .append(classEntry.getName() == null ? "?" : classEntry.getName().trim()).append("\n");
            }
        }
        return agenda.toString();
    }

    /**
     * Turns the model's JSON into the one layout {@code /due} ever produces.
     * <p>
     * Everything the model wrote is escaped before it goes anywhere near Telegram; the markup around it
     * is ours and is written pre-escaped.
     */
    String render(String response, LumiosChat languageSource, LocalDate today) {
        JSONArray items = items(response);
        if (items.isEmpty()) {
            return escape(translationService.getMessage("command.due.nothing-to-do", languageSource));
        }

        LocalDate last = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusDays(DAYS_FROM_MONDAY);
        StringBuilder text = new StringBuilder();
        text.append("🎓 *").append(escape(translationService.getMessage("command.due.title", languageSource)))
                .append("*\n_").append(escape(shortDay(today, languageSource) + " " + DAY_MONTH.format(today)
                        + " → " + shortDay(last, languageSource) + " " + DAY_MONTH.format(last))).append("_\n");

        int shown = 0;
        for (Priority priority : Priority.values()) {
            List<JSONObject> group = new ArrayList<>();
            for (int i = 0; i < items.length() && shown + group.size() < MAX_ITEMS; i++) {
                JSONObject item = items.optJSONObject(i);
                if (item != null && priority == Priority.of(item.optString("priority"))) {
                    group.add(item);
                }
            }
            if (group.isEmpty()) {
                continue;
            }

            text.append("\n").append(priority.marker).append(" *")
                    .append(escape(translationService.getMessage(priority.messageKey, languageSource))).append("*\n");
            for (JSONObject item : group) {
                text.append("• *").append(escape(clean(item.optString("subject"), 40))).append("*")
                        .append(" \\| ").append(escape(due(item.optString("due"), languageSource)))
                        .append(" \\| ").append(escape(clean(item.optString("note"), 70))).append("\n");
                shown++;
            }
        }
        return text.toString();
    }

    /**
     * The model is asked for {@code {"items": [...]}}, but a fallback model that ignores the JSON
     * response type wraps it in a code fence, and a stray sentence before the object is not unheard of.
     * Anything unparseable is treated as an empty answer rather than thrown, since a half-formatted
     * message is worse than "nothing to do".
     */
    private JSONArray items(String response) {
        if (response == null || response.isBlank()) {
            return new JSONArray();
        }
        String body = response.trim();
        int start = body.indexOf('{');
        int end = body.lastIndexOf('}');
        if (start < 0 || end <= start) {
            log.warn("Gemini returned no JSON object for a workload estimate");
            return new JSONArray();
        }
        try {
            JSONArray items = new JSONObject(body.substring(start, end + 1)).optJSONArray("items");
            return items == null ? new JSONArray() : items;
        } catch (Exception e) {
            log.warn("Could not parse the workload estimate Gemini returned", e);
            return new JSONArray();
        }
    }

    /**
     * {@code 2026-09-30} as {@code ср 30.09}. An unparseable date is passed through rather than
     * dropped - the deadline is the useful part of the line even when its shape is wrong.
     */
    private String due(String isoDate, LumiosChat languageSource) {
        try {
            LocalDate date = LocalDate.parse(isoDate.trim());
            return shortDay(date, languageSource) + " " + DAY_MONTH.format(date);
        } catch (Exception e) {
            return clean(isoDate, 16);
        }
    }

    private String shortDay(LocalDate date, LumiosChat languageSource) {
        return TimetablePagedUtil.getShortDayName(date.getDayOfWeek(), translationService, languageSource);
    }

    /**
     * Collapses whitespace, folds the dashes a model reaches for by default into plain hyphens, and
     * trims to something that fits one line on a phone.
     */
    private String clean(String value, int limit) {
        if (value == null) {
            return "";
        }
        String collapsed = value.replace('—', '-').replace('–', '-').replace('−', '-')
                .replaceAll("\\s+", " ").trim();
        return collapsed.length() <= limit ? collapsed : collapsed.substring(0, limit - 1).trim() + "…";
    }

    private String escape(String text) {
        return MessageFormatter.escapeMarkdown(text);
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
     * Class types stay in English whatever the answer's language: they are the one part of the agenda
     * the model has to reason about precisely, and {@link ClassType} already names them.
     */
    private String type(ClassEntry classEntry) {
        ClassType classType = classEntry.getClassType();
        return classType == null ? ClassType.UNKNOWN.name() : classType.name();
    }

    private String systemInstruction(LumiosChat languageSource) {
        return isEnglish(languageSource)
                ? "You help a university student plan their fortnight. Reply with JSON only - no prose, no "
                  + "code fence. Every piece of text inside it is in English. Never use em dashes or en "
                  + "dashes; write a plain hyphen."
                : "Ти допомагаєш студенту спланувати два тижні. Відповідай ЛИШЕ у форматі JSON, без тексту "
                  + "навколо і без блоку коду. Увесь текст усередині - українською. Ніколи не використовуй "
                  + "довгі тире, лише звичайний дефіс.";
    }

    private String prompt(String agenda, LocalDate today, LumiosChat languageSource) {
        String isoToday = today.toString();
        String weekday = today.getDayOfWeek().getDisplayName(TextStyle.FULL,
                Locale.forLanguageTag(languageSource.getLanguage()));

        if (isEnglish(languageSource)) {
            return """
                    Today is %s (%s). Below is what is still ahead in this student's personal timetable \
                    for the rest of this week and all of the next. It already lists only the subjects \
                    they attend, and lectures are excluded because nothing is prepared for them.

                    %s
                    Work out what they most likely have to prepare, and return it as JSON:

                    {"items":[{"subject":"...","due":"YYYY-MM-DD","note":"...","priority":"HIGH|MEDIUM|LOW"}]}

                    - `subject` - the subject, shortened to at most 40 characters. Keep it recognisable.
                    - `due` - the date of the class the work has to be ready for, from the timetable above.
                    - `note` - at most 70 characters saying what probably has to be done. No subject name \
                    in it, it is already the first field.
                    - `priority` - HIGH for a LAB, whose work has to be finished before the class because \
                    it is defended there, and for subjects whose names sound hard and technical. MEDIUM \
                    for a PRACTICE or seminar, which needs preparing for and attending. LOW for anything \
                    light, such as humanities.

                    Rules:
                    - Days earlier this week have already happened and are not in the list. If little is \
                    left of this week, the answer is mostly about next week - that is correct, not a mistake.
                    - At most %d items, the most pressing first within each priority. Merge repeats of one \
                    subject into a single item at its earliest deadline.
                    - You do not know the real assignments. Phrase every note as what is likely.
                    - Return the JSON object and nothing else.
                    """.formatted(isoToday, weekday, agenda, MAX_ITEMS);
        }
        return """
                Сьогодні %s (%s). Нижче - те, що ще попереду в особистому розкладі студента до кінця цього \
                тижня і на весь наступний. У ньому вже лише ті дисципліни, які він відвідує, а лекції \
                виключені, бо до них нічого не готують.

                %s
                Визнач, що йому найімовірніше треба підготувати, і поверни це у форматі JSON:

                {"items":[{"subject":"...","due":"YYYY-MM-DD","note":"...","priority":"HIGH|MEDIUM|LOW"}]}

                - `subject` - дисципліна, скорочена до 40 символів максимум. Має лишитися впізнаваною.
                - `due` - дата тієї пари, до якої робота має бути готова, з розкладу вище.
                - `note` - максимум 70 символів про те, що саме ймовірно треба зробити. Без назви \
                дисципліни, вона вже є в першому полі.
                - `priority` - HIGH для LAB, роботу до якої треба виконати ДО пари, бо саме на ній її \
                захищають, і для дисциплін, назви яких звучать складно й технічно. MEDIUM для PRACTICE \
                (практичного чи семінарського заняття), до якого треба готуватися і на якому треба бути. \
                LOW для легких, наприклад гуманітарних.

                Правила:
                - Дні на початку цього тижня вже минули, їх у списку немає. Якщо від цього тижня лишилось \
                небагато, відповідь буде переважно про наступний - це правильно, а не помилка.
                - Не більше %d пунктів, найтерміновіші першими в межах кожного пріоритету. Повтори однієї \
                дисципліни об'єднуй в один пункт із найранішим дедлайном.
                - Ти не знаєш реальних завдань. Формулюй кожну примітку як імовірну.
                - Поверни лише JSON-об'єкт і нічого більше.
                """.formatted(isoToday, weekday, agenda, MAX_ITEMS);
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

    /**
     * Ordered high to low: the render walks these in turn, so this is the order the sections appear in.
     */
    private enum Priority {

        HIGH("🔴", "command.due.priority.high"),
        MEDIUM("🟡", "command.due.priority.medium"),
        LOW("🟢", "command.due.priority.low");

        private final String marker;
        private final String messageKey;

        Priority(String marker, String messageKey) {
            this.marker = marker;
            this.messageKey = messageKey;
        }

        /**
         * Anything the model did not label, or labelled with something of its own invention, is treated
         * as middling rather than dropped.
         */
        static Priority of(String value) {
            if (value == null) {
                return MEDIUM;
            }
            return switch (value.trim().toUpperCase(Locale.ROOT)) {
                case "HIGH" -> HIGH;
                case "LOW" -> LOW;
                default -> MEDIUM;
            };
        }
    }

    /**
     * @param choicesHash what the member attends, so re-picking electives moves them off the old answer
     * @param day         so an answer never survives midnight, when "this week" starts meaning something
     *                    else
     */
    private record ReportKey(Long chatId, Long telegramUserId, LocalDate day, int choicesHash) {
    }
}
