package dev.ua.ikeepcalm.lumios.telegram.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The sanitizer has two jobs that pull against each other: never hand Telegram malformed MarkdownV2
 * (it refuses the whole message, and the client then resends it unformatted), and never throw away
 * formatting that was already valid (which is how every bold line in the bot came out as literal
 * asterisks).
 */
class MarkdownV2SanitizerTest {

    private static String sanitize(String text) {
        return MarkdownV2Sanitizer.sanitize(text);
    }

    /**
     * Every special character MarkdownV2 reserves, unescaped, outside an entity - the exact thing that
     * makes Telegram reject a message.
     */
    private static void assertNoStraySpecials(String output) {
        String withoutEscapes = output.replaceAll("\\\\.", "");
        String withoutEntities = withoutEscapes
                .replaceAll("```[\\s\\S]*?```", "")
                .replaceAll("`[^`]*`", "")
                .replaceAll("\\[([^\\]]*)]\\(([^)]*)\\)", "$1")
                .replace("||", "")
                .replace("__", "")
                .replace("*", "")
                .replace("_", "")
                .replace("~", "");
        assertThat(withoutEntities).matches(
                text -> "[]()>#+-=|{}.!`".chars().noneMatch(c -> text.indexOf(c) >= 0),
                "no unescaped MarkdownV2 special characters");
    }

    @Nested
    @DisplayName("keeps formatting")
    class KeepsFormatting {

        @Test
        @DisplayName("bold survives instead of becoming literal asterisks")
        void keepsBold() {
            assertThat(sanitize("hello *world*")).isEqualTo("hello *world*");
        }

        @Test
        @DisplayName("the **bold** an LLM writes is folded onto Telegram's single asterisk")
        void foldsDoubleAsteriskBold() {
            assertThat(sanitize("**Urgent** today")).isEqualTo("*Urgent* today");
        }

        @Test
        @DisplayName("italic, underline and strikethrough come through")
        void keepsOtherEmphasis() {
            assertThat(sanitize("_i_ __u__ ~~s~~")).isEqualTo("_i_ __u__ ~s~");
        }

        @Test
        @DisplayName("text inside emphasis is still escaped")
        void escapesInsideEmphasis() {
            assertThat(sanitize("*version 2.0*")).isEqualTo("*version 2\\.0*");
        }

        @Test
        @DisplayName("links keep their label and url, each escaped by its own rules")
        void keepsLinks() {
            assertThat(sanitize("[the class](https://meet.google.com/abc-def)"))
                    .isEqualTo("[the class](https://meet.google.com/abc-def)");
        }

        @Test
        @DisplayName("code is left as written apart from backticks and backslashes")
        void keepsCode() {
            assertThat(sanitize("run `git commit -m \"x.y\"` now"))
                    .isEqualTo("run `git commit -m \"x.y\"` now");
            assertThat(sanitize("```java\nint a = 1.0;\n```"))
                    .isEqualTo("```java\nint a = 1.0;\n```");
        }

        @Test
        @DisplayName("a heading becomes a bold line, since Telegram has no headings")
        void foldsHeadings() {
            assertThat(sanitize("### What to do\ntext")).isEqualTo("*What to do*\ntext");
        }

        @Test
        @DisplayName("list markers become bullets")
        void foldsBullets() {
            assertThat(sanitize("- first\n* second\n  + third"))
                    .isEqualTo("• first\n• second\n  • third");
        }
    }

    @Nested
    @DisplayName("never leaves Telegram a malformed message")
    class NeverMalformed {

        @Test
        @DisplayName("a delimiter with no partner is escaped")
        void escapesUnmatchedDelimiters() {
            assertThat(sanitize("2 * 3 and _oops")).isEqualTo("2 \\* 3 and \\_oops");
            assertNoStraySpecials(sanitize("2 * 3 and _oops"));
        }

        @Test
        @DisplayName("an underscore inside a word is not italic")
        void ignoresIntrawordUnderscores() {
            assertThat(sanitize("call reminder_lead_minutes now"))
                    .isEqualTo("call reminder\\_lead\\_minutes now");
        }

        @Test
        @DisplayName("ordinary punctuation is escaped")
        void escapesPunctuation() {
            assertThat(sanitize("Done! Cost: 3.50 (approx) #1"))
                    .isEqualTo("Done\\! Cost: 3\\.50 \\(approx\\) \\#1");
        }

        @Test
        @DisplayName("an unterminated code fence is closed rather than escaped")
        void closesDanglingFence() {
            assertThat(sanitize("```\nint a;")).isEqualTo("```\nint a;```");
        }

        @Test
        @DisplayName("a bracket that is not a link is escaped")
        void escapesNonLinkBrackets() {
            // '=' is on Telegram's reserved list too, easy to forget.
            assertThat(sanitize("array[0] = 1")).isEqualTo("array\\[0\\] \\= 1");
            assertNoStraySpecials(sanitize("array[0] = 1"));
        }

        @Test
        @DisplayName("a realistic LLM answer comes out clean")
        void handlesARealisticAnswer() {
            String output = sanitize("""
                    ## Plan for the week
                    - **Security** — finish the lab (due 30.09!)
                    - Use `npm run build` first
                    - See [the docs](https://example.com/a_b)

                    Cost: ~$3.50 | 50% done_
                    """);
            assertNoStraySpecials(output);
            assertThat(output).contains("*Plan for the week*").contains("• *Security*")
                    .contains("`npm run build`").contains("[the docs](https://example.com/a_b)");
        }
    }

    @Nested
    @DisplayName("idempotent")
    class Idempotent {

        @Test
        @DisplayName("sanitizing twice changes nothing, since both send paths may do it")
        void stableUnderRepetition() {
            for (String input : new String[]{
                    "hello *world* 2.0",
                    "**bold** and `code.x` and [a](https://b.c/d)",
                    "2 * 3 and _oops",
                    "### Head\n- item 1.5",
                    "```java\nint a = 1;\n```"}) {
                String once = sanitize(input);
                assertThat(sanitize(once)).as("stable for: %s", input).isEqualTo(once);
            }
        }

        @Test
        @DisplayName("the layout /due builds is passed through untouched")
        void leavesTheDueReportAlone() {
            // /due escapes its own fields and writes its own markup; the send path sanitizes everything
            // once more on the way out, and that second pass used to be what ate the asterisks.
            String report = """
                    🎓 *Що варто зробити*
                    _Пт 25\\.09 → Нд 04\\.10_

                    🔴 *Терміново*
                    • *Безпека програмного забезпечення* \\| Ср 30\\.09 \\| виконати завдання""";

            assertThat(sanitize(report)).isEqualTo(report);
        }
    }

    @Test
    @DisplayName("null and empty are handled")
    void handlesNothing() {
        assertThat(sanitize(null)).isNull();
        assertThat(sanitize("")).isEmpty();
    }
}
