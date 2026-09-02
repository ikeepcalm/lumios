package dev.ua.ikeepcalm.lumios.telegram.utils;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.personal.TimetableMember;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.types.ReminderChannel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReminderSettingsPickerTest {

    @Test
    @DisplayName("the channel button walks every option and comes back round")
    void channelCyclesThroughEveryOption() {
        ReminderChannel channel = ReminderChannel.DM;
        for (int step = 0; step < 4; step++) {
            channel = ReminderSettingsPicker.nextChannel(channel);
        }
        assertThat(channel).isEqualTo(ReminderChannel.DM);
        assertThat(ReminderSettingsPicker.nextChannel(ReminderChannel.OFF)).isEqualTo(ReminderChannel.DM);
    }

    @Test
    @DisplayName("the digest hour cycles, and an unrecognised one restarts the cycle")
    void digestTimeCycles() {
        assertThat(ReminderSettingsPicker.nextDigestTime(LocalTime.of(6, 30))).isEqualTo(LocalTime.of(7, 0));
        assertThat(ReminderSettingsPicker.nextDigestTime(LocalTime.of(8, 30))).isEqualTo(LocalTime.of(6, 30));
        assertThat(ReminderSettingsPicker.nextDigestTime(LocalTime.of(4, 15))).isEqualTo(LocalTime.of(6, 30));
    }

    @Test
    @DisplayName("every digest hour lands on a half hour the scheduler actually fires at")
    void digestTimesMatchTheCron() {
        // The cron is "0 0,30 6-9", so an option on any other minute would simply never be sent.
        for (LocalTime option : ReminderSettingsPicker.DIGEST_TIME_OPTIONS) {
            assertThat(option.getMinute()).isIn(0, 30);
            assertThat(option.getHour()).isBetween(6, 9);
            assertThat(option.getSecond()).isZero();
        }
    }

    @Test
    @DisplayName("a member with no lead time of their own inherits the chat's")
    void leadFallsBackToTheChat() {
        LumiosChat chat = new LumiosChat();
        chat.setReminderLeadMinutes(15);
        TimetableMember member = new TimetableMember();

        assertThat(ReminderSettingsPicker.effectiveLead(member, chat)).isEqualTo(15);
        member.setLeadMinutes(5);
        assertThat(ReminderSettingsPicker.effectiveLead(member, chat)).isEqualTo(5);
    }

    @Test
    @DisplayName("a chat with no lead time set gets the default, not silence")
    void nullChatLeadMeansTheDefault() {
        LumiosChat chat = new LumiosChat();
        chat.setReminderLeadMinutes(null);
        assertThat(ReminderSettingsPicker.effectiveLead(new TimetableMember(), chat))
                .isEqualTo(LumiosChat.DEFAULT_REMINDER_LEAD_MINUTES);
    }

    @Test
    @DisplayName("a member who never set a digest hour still has one")
    void digestTimeHasADefault() {
        TimetableMember member = new TimetableMember();
        assertThat(member.getDigestTime()).isNull();
        assertThat(member.digestTimeOrDefault()).isEqualTo(TimetableMember.DEFAULT_DIGEST_TIME);
    }

    @Test
    @DisplayName("the channel says which deliveries it implies")
    void channelKnowsItsDeliveries() {
        assertThat(ReminderChannel.DM.sendsDm()).isTrue();
        assertThat(ReminderChannel.DM.tagsInGroup()).isFalse();
        assertThat(ReminderChannel.GROUP_TAG.sendsDm()).isFalse();
        assertThat(ReminderChannel.GROUP_TAG.tagsInGroup()).isTrue();
        assertThat(ReminderChannel.BOTH.sendsDm()).isTrue();
        assertThat(ReminderChannel.BOTH.tagsInGroup()).isTrue();
        assertThat(ReminderChannel.OFF.sendsDm()).isFalse();
        assertThat(ReminderChannel.OFF.tagsInGroup()).isFalse();
    }
}
