package dev.ua.ikeepcalm.lumios.telegram.utils;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The half of {@code /due} that has to be right every single time: turning whatever the model returned
 * into one fixed layout that Telegram will actually accept as MarkdownV2.
 * <p>
 * Telegram rejects a message whose MarkdownV2 is malformed, and the client then resends it with no
 * parse mode at all - which is how the raw {@code **asterisks**} used to reach the group. So the
 * escaping here is not cosmetic.
 */
class WorkloadReporterTest {

    private static final LocalDate FRIDAY = LocalDate.of(2026, 9, 25);

    private final WorkloadReporter reporter =
            new WorkloadReporter(null, null, new TranslationService(TranslationService.messageSource()), null, null);

    private final LumiosChat chat = englishChat();

    private static LumiosChat englishChat() {
        LumiosChat chat = new LumiosChat();
        chat.setLanguage("en");
        return chat;
    }

    private String render(String json) {
        return reporter.render(json, chat, FRIDAY);
    }

    @Test
    @DisplayName("renders subject, deadline and note on one line, in priority order")
    void rendersTheFixedLayout() {
        String text = render("""
                {"items":[
                  {"subject":"Human rights","due":"2026-10-01","note":"read the handout","priority":"LOW"},
                  {"subject":"Software security","due":"2026-09-30","note":"finish the lab","priority":"HIGH"}
                ]}""");

        assertThat(text).contains("*Software security* \\| Wed 30\\.09 \\| finish the lab");
        assertThat(text).contains("*Human rights* \\| Thu 01\\.10 \\| read the handout");
        assertThat(text.indexOf("Software security")).isLessThan(text.indexOf("Human rights"));
        assertThat(text).contains("🔴").contains("🟢");
    }

    @Test
    @DisplayName("every dot the layout produces is escaped, so Telegram accepts the message")
    void escapesEveryLiteralDot() {
        String text = render("""
                {"items":[{"subject":"Web tech v2.0","due":"2026-09-30","note":"lab 1. and 2.","priority":"HIGH"}]}""");

        // An unescaped '.' or '|' is exactly what makes Telegram refuse the whole message.
        assertThat(text.replace("\\.", "")).doesNotContain(".");
        assertThat(text.replace("\\|", "")).doesNotContain("|");
    }

    @Test
    @DisplayName("the dashes a model reaches for by default are folded into plain hyphens")
    void foldsFancyDashes() {
        String text = render("""
                {"items":[{"subject":"Modelling — discrete","due":"2026-09-30","note":"report – due","priority":"HIGH"}]}""");

        assertThat(text).doesNotContain("—").doesNotContain("–");
        assertThat(text).contains("Modelling \\- discrete").contains("report \\- due");
    }

    @Test
    @DisplayName("keeps the answer short, however many items come back")
    void capsTheNumberOfItems() {
        StringBuilder json = new StringBuilder("{\"items\":[");
        for (int i = 0; i < 20; i++) {
            json.append(i == 0 ? "" : ",")
                    .append("{\"subject\":\"Subject ").append(i)
                    .append("\",\"due\":\"2026-09-30\",\"note\":\"do it\",\"priority\":\"HIGH\"}");
        }
        String text = render(json.append("]}").toString());

        assertThat(text.lines().filter(line -> line.startsWith("•")).count()).isEqualTo(8);
    }

    @Test
    @DisplayName("an unlabelled priority is treated as middling rather than dropped")
    void unknownPriorityBecomesMedium() {
        String text = render("""
                {"items":[{"subject":"Economics","due":"2026-09-30","note":"prepare","priority":"URGENT!!"}]}""");

        assertThat(text).contains("🟡").contains("Economics");
    }

    @Test
    @DisplayName("JSON wrapped in a code fence by a fallback model is still read")
    void unwrapsAFencedAnswer() {
        String text = render("""
                Here you go:
                ```json
                {"items":[{"subject":"Economics","due":"2026-09-30","note":"prepare","priority":"MEDIUM"}]}
                ```""");

        assertThat(text).contains("Economics");
    }

    @Test
    @DisplayName("an answer that cannot be parsed reads as 'nothing to do', never as broken markup")
    void unparseableAnswerFallsBack() {
        assertThat(render("the model decided to chat instead")).isEqualTo("Nothing urgent in sight for the next two weeks\\.");
        assertThat(render("")).isEqualTo("Nothing urgent in sight for the next two weeks\\.");
        assertThat(render("{\"items\":[]}")).isEqualTo("Nothing urgent in sight for the next two weeks\\.");
    }

    @Test
    @DisplayName("survives the sanitizer the send path runs over every message")
    void survivesTheSendPath() {
        // TelegramClient sanitizes every MarkdownV2 message on the way out. That pass used to escape
        // this layout's own asterisks, so the group saw *Subject* spelled out. Nothing may change here.
        String rendered = render("""
                {"items":[{"subject":"Software security 2.0","due":"2026-09-30","note":"finish lab 1","priority":"HIGH"}]}""");

        assertThat(MarkdownV2Sanitizer.sanitize(rendered)).isEqualTo(rendered);
        assertThat(rendered).contains("*Software security 2\\.0*");
    }

    @Test
    @DisplayName("a deadline the model mangled is shown as written rather than dropped")
    void keepsAnUnparseableDeadline() {
        String text = render("""
                {"items":[{"subject":"Economics","due":"next Tuesday","note":"prepare","priority":"HIGH"}]}""");

        assertThat(text).contains("next Tuesday");
    }
}
