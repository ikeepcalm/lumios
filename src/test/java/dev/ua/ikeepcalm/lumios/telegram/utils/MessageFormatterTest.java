package dev.ua.ikeepcalm.lumios.telegram.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageFormatterTest {

    @Test
    @DisplayName("a mention links by id, so it pings members with no username")
    void mentionsById() {
        assertThat(MessageFormatter.formatMentionLink("Bohdan", 42L))
                .isEqualTo("[Bohdan](tg://user?id=42)");
    }

    @Test
    @DisplayName("only the characters legacy Markdown reads inside a label are escaped")
    void escapesOnlyWhatLegacyMarkdownReads() {
        // The MarkdownV2 escape set would put visible backslashes before dots and dashes in a name.
        assertThat(MessageFormatter.formatMentionLink("a_b*c`d[e]f", 7L))
                .isEqualTo("[a\\_b\\*c\\`d\\[e\\]f](tg://user?id=7)");
        assertThat(MessageFormatter.formatMentionLink("Іван Петренко-Коваль", 7L))
                .isEqualTo("[Іван Петренко-Коваль](tg://user?id=7)");
    }

    @Test
    @DisplayName("a member with no name at all still gets a working mention")
    void fallsBackToTheId() {
        assertThat(MessageFormatter.formatMentionLink(null, 99L)).isEqualTo("[99](tg://user?id=99)");
        assertThat(MessageFormatter.formatMentionLink("   ", 99L)).isEqualTo("[99](tg://user?id=99)");
    }
}
