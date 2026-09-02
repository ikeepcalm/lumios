package dev.ua.ikeepcalm.lumios.telegram.utils;

import dev.ua.ikeepcalm.lumios.telegram.utils.TimetableViewSupport.Scope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TimetablePagedUtilTest {

    @Test
    @DisplayName("the legacy paging payload still parses, so buttons already on screen keep working")
    void parsesLegacyPayload() {
        String payload = "timetable-today-3-forward";
        assertThat(TimetablePagedUtil.extractCommandType(payload)).isEqualTo("today");
        assertThat(TimetablePagedUtil.extractPage(payload)).isEqualTo(3);
        assertThat(TimetablePagedUtil.extractDirection(payload)).isEqualTo("forward");
    }

    @Test
    @DisplayName("a malformed payload falls back rather than throwing at the user")
    void fallsBackOnMalformedPayload() {
        assertThat(TimetablePagedUtil.extractCommandType("timetable")).isEqualTo("today");
        assertThat(TimetablePagedUtil.extractPage("timetable-today")).isEqualTo(1);
        assertThat(TimetablePagedUtil.extractDirection("timetable-today-2")).isEqualTo("forward");
    }

    @Test
    @DisplayName("the view payload is split on # so a negative group chat id survives")
    void viewPayloadCarriesNegativeChatId() {
        // This is the whole reason for a second callback family: the timetable- payload splits on "-",
        // which a chat id like -1001234567890 does not survive.
        String payload = TimetableViewSupport.PAGE + -1001234567890L + "#today#2#" + Scope.MINE.token();
        String[] parts = payload.split("#");

        assertThat(parts[0]).isEqualTo("view");
        assertThat(parts[1]).isEqualTo("p");
        assertThat(Long.parseLong(parts[2])).isEqualTo(-1001234567890L);
        assertThat(parts[3]).isEqualTo("today");
        assertThat(Integer.parseInt(parts[4])).isEqualTo(2);
        assertThat(Scope.of(parts[5])).isEqualTo(Scope.MINE);
    }

    @Test
    @DisplayName("a view payload stays inside Telegram's 64-byte callback limit")
    void viewPayloadFitsTheCallbackLimit() {
        String payload = TimetableViewSupport.SWITCH + -1001234567890L + "#tomorrow#12#" + Scope.ALL.token();
        assertThat(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8).length).isLessThanOrEqualTo(64);
    }

    @Test
    @DisplayName("scope round-trips through its token, and anything unknown means personal")
    void scopeRoundTrips() {
        assertThat(Scope.of(Scope.MINE.token())).isEqualTo(Scope.MINE);
        assertThat(Scope.of(Scope.ALL.token())).isEqualTo(Scope.ALL);
        assertThat(Scope.of("")).isEqualTo(Scope.MINE);
        assertThat(Scope.of(null)).isEqualTo(Scope.MINE);
        assertThat(Scope.MINE.other()).isEqualTo(Scope.ALL);
        assertThat(Scope.ALL.other()).isEqualTo(Scope.MINE);
    }
}
