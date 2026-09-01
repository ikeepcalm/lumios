package dev.ua.ikeepcalm.lumios.telegram.utils;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.personal.TimetableMember;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.ReminderChannel;
import dev.ua.ikeepcalm.lumios.telegram.utils.markup.SettingsMarkupUtil;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The private-chat menu where a member says how they want to be told about their classes.
 * <p>
 * Split from {@link ElectivePicker} because the two answer different questions - what I study, versus
 * how I am told about it - and because the picker is already a paged list with no room left for four
 * more rows. Each menu carries a button opening the other.
 * <p>
 * Callback payloads, all prefixed {@code rem#} so one handler receives them. Every payload carries the
 * group's chat id, because the menu lives in a private chat and the settings are per group.
 * <ul>
 *     <li>{@code rem#ch#<chatId>} - cycle the delivery channel</li>
 *     <li>{@code rem#l#<chatId>} - cycle the advance warning</li>
 *     <li>{@code rem#dg#<chatId>} - turn the morning digest on or off</li>
 *     <li>{@code rem#dt#<chatId>} - cycle the hour the digest arrives</li>
 *     <li>{@code rem#m#<chatId>} - open the elective picker instead</li>
 *     <li>{@code rem#d#<chatId>} - done, close the menu</li>
 *     <li>{@code rem#o#<chatId>} - open this menu for one group (from the picker)</li>
 *     <li>{@code rem#g#<chatId>} - sent from a group: deliver this menu privately</li>
 *     <li>{@code rem#c#<chatId>} - pick which group to configure</li>
 * </ul>
 */
public final class ReminderSettingsPicker {

    public static final String PREFIX = "rem#";
    public static final String CHANNEL = PREFIX + "ch#";
    public static final String LEAD = PREFIX + "l#";
    public static final String DIGEST = PREFIX + "dg#";
    public static final String DIGEST_TIME = PREFIX + "dt#";
    public static final String OPEN_ELECTIVES = PREFIX + "m#";
    public static final String DONE = PREFIX + "d#";
    public static final String OPEN = PREFIX + "o#";
    public static final String FROM_GROUP = PREFIX + "g#";
    public static final String CHOOSE_CHAT = PREFIX + "c#";

    /**
     * Hours the digest button offers. They all sit on a half hour so one scheduler firing at :00 and
     * :30 covers every one of them.
     */
    public static final LocalTime[] DIGEST_TIME_OPTIONS = {
            LocalTime.of(6, 30), LocalTime.of(7, 0), LocalTime.of(7, 30),
            LocalTime.of(8, 0), LocalTime.of(8, 30)
    };

    /**
     * Order the channel button walks through.
     */
    private static final ReminderChannel[] CHANNEL_ORDER = {
            ReminderChannel.DM, ReminderChannel.GROUP_TAG, ReminderChannel.BOTH, ReminderChannel.OFF
    };

    private ReminderSettingsPicker() {
    }

    public static ReminderChannel nextChannel(ReminderChannel current) {
        for (int i = 0; i < CHANNEL_ORDER.length; i++) {
            if (CHANNEL_ORDER[i] == current) {
                return CHANNEL_ORDER[(i + 1) % CHANNEL_ORDER.length];
            }
        }
        return CHANNEL_ORDER[0];
    }

    public static LocalTime nextDigestTime(LocalTime current) {
        for (int i = 0; i < DIGEST_TIME_OPTIONS.length; i++) {
            if (DIGEST_TIME_OPTIONS[i].equals(current)) {
                return DIGEST_TIME_OPTIONS[(i + 1) % DIGEST_TIME_OPTIONS.length];
            }
        }
        return DIGEST_TIME_OPTIONS[0];
    }

    /**
     * The member's effective advance warning: their own if they set one, otherwise the chat's.
     */
    public static int effectiveLead(TimetableMember member, LumiosChat groupChat) {
        return member.getLeadMinutes() == null
                ? SettingsMarkupUtil.currentLeadMinutes(groupChat)
                : member.getLeadMinutes();
    }

    public static String text(TranslationService translations, LumiosChat languageSource, String groupName,
                              TimetableMember member, LumiosChat groupChat) {
        ReminderChannel channel = member.getReminderChannel();

        StringBuilder text = new StringBuilder();
        text.append(translations.getMessage("reminders.title", languageSource, groupName));
        text.append("\n\n").append(translations.getMessage(channelKey(channel), languageSource));

        if (channel.sendsDm()) {
            int lead = effectiveLead(member, groupChat);
            text.append("\n").append(lead == 0
                    ? translations.getMessage("reminders.lead.off", languageSource)
                    : translations.getMessage("reminders.lead", languageSource, String.valueOf(lead)));
        }
        if (channel.tagsInGroup()) {
            // The group announcement is one message shared by everyone, so it can only ever fire at the
            // chat's own lead time - worth saying, or the member's own setting looks broken.
            text.append("\n").append(translations.getMessage("reminders.footer.group_tag_lead", languageSource,
                    String.valueOf(SettingsMarkupUtil.currentLeadMinutes(groupChat))));
        }

        text.append("\n").append(member.isDigestEnabled()
                ? translations.getMessage("reminders.digest.on", languageSource,
                        member.digestTimeOrDefault().toString())
                : translations.getMessage("reminders.digest.off", languageSource));

        if (member.isDmUnavailable()) {
            text.append("\n\n").append(translations.getMessage("reminders.dm-unavailable", languageSource));
        }
        return text.toString();
    }

    public static InlineKeyboardMarkup keyboard(TranslationService translations, LumiosChat languageSource,
                                                Long groupChatId, TimetableMember member, LumiosChat groupChat) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        ReminderChannel channel = member.getReminderChannel();

        keyboard.add(new InlineKeyboardRow(button(
                translations.getMessage("reminders.button.channel", languageSource,
                        translations.getMessage(channelShortKey(nextChannel(channel)), languageSource)),
                CHANNEL + groupChatId)));

        if (channel.sendsDm()) {
            int next = SettingsMarkupUtil.nextLeadMinutes(effectiveLead(member, groupChat));
            keyboard.add(new InlineKeyboardRow(button(next == 0
                            ? translations.getMessage("reminders.button.lead_off", languageSource)
                            : translations.getMessage("reminders.button.lead", languageSource, String.valueOf(next)),
                    LEAD + groupChatId)));
        }

        keyboard.add(new InlineKeyboardRow(button(
                translations.getMessage(member.isDigestEnabled()
                        ? "reminders.button.digest_disable" : "reminders.button.digest_enable", languageSource),
                DIGEST + groupChatId)));
        if (member.isDigestEnabled()) {
            keyboard.add(new InlineKeyboardRow(button(
                    translations.getMessage("reminders.button.digest_time", languageSource,
                            nextDigestTime(member.digestTimeOrDefault()).toString()),
                    DIGEST_TIME + groupChatId)));
        }

        keyboard.add(new InlineKeyboardRow(button(
                translations.getMessage("reminders.button.electives", languageSource),
                OPEN_ELECTIVES + groupChatId)));
        keyboard.add(new InlineKeyboardRow(button(
                translations.getMessage("mine.button.done", languageSource), DONE + groupChatId)));

        return new InlineKeyboardMarkup(keyboard);
    }

    private static String channelKey(ReminderChannel channel) {
        return switch (channel) {
            case DM -> "reminders.channel.dm";
            case GROUP_TAG -> "reminders.channel.group_tag";
            case BOTH -> "reminders.channel.both";
            case OFF -> "reminders.channel.off";
        };
    }

    private static String channelShortKey(ReminderChannel channel) {
        return switch (channel) {
            case DM -> "reminders.channel.short.dm";
            case GROUP_TAG -> "reminders.channel.short.group_tag";
            case BOTH -> "reminders.channel.short.both";
            case OFF -> "reminders.channel.short.off";
        };
    }

    private static InlineKeyboardButton button(String label, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton(label);
        button.setCallbackData(callbackData);
        return button;
    }
}
