package dev.ua.ikeepcalm.lumios.telegram.utils;

/**
 * Turns text into valid Telegram MarkdownV2, keeping the formatting that is already there.
 * <p>
 * Telegram refuses a message whose MarkdownV2 is malformed, and {@code TelegramClient} then resends it
 * with no parse mode at all - so one stray character costs the whole message its formatting. Escaping
 * everything avoids the refusal but throws the formatting away just as thoroughly: that is what the
 * previous implementation did, which is why nothing the bot sent ever rendered bold.
 * <p>
 * So this is a parser rather than a set of replacements. Well-formed emphasis, code, links and
 * spoilers are recognised and re-emitted as MarkdownV2 entities; every other special character is
 * escaped. A delimiter with no partner is escaped too, which is the whole trick - it is exactly the
 * unmatched ones that make Telegram refuse the message.
 * <p>
 * It is also idempotent: running it over its own output changes nothing, because {@code \x} is read
 * back as a literal and re-escaped. Messages that pass through both the chunker and the send path get
 * sanitized twice, so that matters.
 * <p>
 * Along the way the dialects an LLM writes by default are folded into the one Telegram understands:
 * {@code **bold**} becomes {@code *bold*}, a {@code ###} heading becomes a bold line, and a
 * {@code -} list marker becomes a bullet - Telegram has no heading or list syntax at all.
 */
public final class MarkdownV2Sanitizer {

    /**
     * Everything MarkdownV2 reserves. Any of these left unescaped outside an entity is a parse error.
     */
    private static final String SPECIALS = "_*[]()~`>#+-=|{}.!";

    /**
     * How far a delimiter may look for its partner. An opening {@code *} in a long message would
     * otherwise scan to the end of the text for every candidate, and a stray one in a wall of prose
     * would swallow the rest of it into bold.
     */
    private static final int MAX_SPAN = 2000;

    private MarkdownV2Sanitizer() {
    }

    public static String sanitize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String normalised = foldLlmDialect(text);
        StringBuilder out = new StringBuilder(normalised.length() + 64);
        parse(normalised, 0, normalised.length(), out);
        return out.toString();
    }

    /**
     * Rewrites the Markdown an LLM reaches for into what Telegram actually has. Lines inside a fenced
     * code block are left exactly as they are.
     */
    private static String foldLlmDialect(String text) {
        String[] lines = text.split("\n", -1);
        StringBuilder folded = new StringBuilder(text.length());
        boolean inFence = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.trim().startsWith("```")) {
                inFence = !inFence;
            } else if (!inFence) {
                line = foldHeading(line);
                line = foldBullet(line);
            }
            folded.append(line);
            if (i < lines.length - 1) {
                folded.append('\n');
            }
        }
        return folded.toString();
    }

    /**
     * {@code ## Heading} becomes a bold line. Telegram has no headings, and left alone the hashes are
     * escaped and shown, which is worse than nothing.
     */
    private static String foldHeading(String line) {
        int i = 0;
        while (i < line.length() && (line.charAt(i) == ' ' || line.charAt(i) == '\t')) {
            i++;
        }
        int hashes = 0;
        while (i + hashes < line.length() && line.charAt(i + hashes) == '#') {
            hashes++;
        }
        if (hashes == 0 || hashes > 6 || i + hashes >= line.length() || line.charAt(i + hashes) != ' ') {
            return line;
        }
        String title = line.substring(i + hashes).trim();
        return title.isEmpty() ? line : line.substring(0, i) + "*" + title + "*";
    }

    /**
     * A leading {@code -}, {@code *} or {@code +} is a list marker, not emphasis. Turning it into a
     * bullet both reads better and keeps the parser from mistaking it for an opening delimiter.
     */
    private static String foldBullet(String line) {
        int i = 0;
        while (i < line.length() && (line.charAt(i) == ' ' || line.charAt(i) == '\t')) {
            i++;
        }
        if (i >= line.length() || "-*+".indexOf(line.charAt(i)) < 0) {
            return line;
        }
        if (i + 1 >= line.length() || line.charAt(i + 1) != ' ') {
            return line;
        }
        return line.substring(0, i) + "•" + line.substring(i + 1);
    }

    private static void parse(String s, int from, int to, StringBuilder out) {
        int i = from;
        while (i < to) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> i = literal(s, i, to, out);
                case '`' -> i = code(s, i, to, out);
                case '[' -> i = link(s, i, to, out);
                case '*', '_', '~' -> i = emphasis(s, i, to, from, out);
                case '|' -> i = spoiler(s, i, to, out);
                default -> {
                    escapeInto(c, out);
                    i++;
                }
            }
        }
    }

    /**
     * An escape the author already wrote. Kept as the literal it stands for, which is what makes the
     * whole pass idempotent.
     */
    private static int literal(String s, int i, int to, StringBuilder out) {
        if (i + 1 < to && SPECIALS.indexOf(s.charAt(i + 1)) >= 0) {
            out.append('\\').append(s.charAt(i + 1));
            return i + 2;
        }
        out.append("\\\\");
        return i + 1;
    }

    private static int code(String s, int i, int to, StringBuilder out) {
        if (s.startsWith("```", i)) {
            int close = s.indexOf("```", i + 3);
            // An unterminated fence is closed here rather than escaped: the content is almost always
            // code, and showing it as code beats showing it as escaped punctuation.
            int end = (close < 0 || close >= to) ? to : close;
            out.append("```").append(escapeCodeContent(s.substring(i + 3, end))).append("```");
            return (close < 0 || close >= to) ? to : close + 3;
        }

        int close = -1;
        for (int j = i + 1; j < to; j++) {
            char c = s.charAt(j);
            if (c == '\n') {
                break;
            }
            if (c == '`') {
                close = j;
                break;
            }
        }
        if (close < 0 || close == i + 1) {
            out.append("\\`");
            return i + 1;
        }
        out.append('`').append(escapeCodeContent(s.substring(i + 1, close))).append('`');
        return close + 1;
    }

    private static int link(String s, int i, int to, StringBuilder out) {
        int label = find(s, i + 1, to, ']');
        if (label < 0 || label + 1 >= to || s.charAt(label + 1) != '(') {
            out.append("\\[");
            return i + 1;
        }
        int url = find(s, label + 2, to, ')');
        if (url < 0) {
            out.append("\\[");
            return i + 1;
        }
        out.append('[');
        parse(s, i + 1, label, out);
        out.append("](").append(escapeUrl(s.substring(label + 2, url))).append(')');
        return url + 1;
    }

    /**
     * Emphasis, if the delimiter has a partner; otherwise one escaped character and on with the scan.
     *
     * @param from where the current span began, so the boundary test does not read outside it
     */
    private static int emphasis(String s, int i, int to, int from, StringBuilder out) {
        char marker = s.charAt(i);
        int run = (i + 1 < to && s.charAt(i + 1) == marker) ? 2 : 1;
        int contentStart = i + run;

        int close = findRun(s, contentStart, to, marker, run);
        if (close < 0 || close == contentStart) {
            escapeInto(marker, out);
            return i + 1;
        }

        // `snake_case` and `3*4*5` are not emphasis. A single delimiter has to sit on a word boundary,
        // the way every other Markdown implementation decides the same question.
        if (run == 1 && (isWordChar(charAt(s, i - 1, from, to)) || isWordChar(charAt(s, close + 1, from, to)))) {
            escapeInto(marker, out);
            return i + 1;
        }

        String tag = tagFor(marker, run);
        out.append(tag);
        parse(s, contentStart, close, out);
        out.append(tag);
        return close + run;
    }

    /**
     * MarkdownV2 spells bold {@code *}, italic {@code _}, underline {@code __} and strikethrough
     * {@code ~}. The doubled forms an LLM writes for bold and strikethrough collapse onto those.
     */
    private static String tagFor(char marker, int run) {
        if (marker == '_') {
            return run == 2 ? "__" : "_";
        }
        return String.valueOf(marker);
    }

    private static int spoiler(String s, int i, int to, StringBuilder out) {
        if (i + 1 >= to || s.charAt(i + 1) != '|') {
            out.append("\\|");
            return i + 1;
        }
        int close = findRun(s, i + 2, to, '|', 2);
        if (close < 0 || close == i + 2) {
            out.append("\\|");
            return i + 1;
        }
        out.append("||");
        parse(s, i + 2, close, out);
        out.append("||");
        return close + 2;
    }

    /**
     * The next unescaped run of exactly {@code run} markers, within {@link #MAX_SPAN}. A longer run is
     * not a closer: {@code ***} ends a {@code **} span and leaves a literal behind, which Telegram
     * would reject, so it is skipped and the delimiter ends up escaped instead.
     */
    private static int findRun(String s, int from, int to, char marker, int run) {
        int limit = Math.min(to, from + MAX_SPAN);
        for (int j = from; j < limit; j++) {
            char c = s.charAt(j);
            if (c == '\\') {
                j++;
                continue;
            }
            if (c != marker) {
                continue;
            }
            int length = 0;
            while (j + length < to && s.charAt(j + length) == marker) {
                length++;
            }
            if (length == run) {
                return j;
            }
            j += length - 1;
        }
        return -1;
    }

    private static int find(String s, int from, int to, char target) {
        int limit = Math.min(to, from + MAX_SPAN);
        for (int j = from; j < limit; j++) {
            char c = s.charAt(j);
            if (c == '\\') {
                j++;
            } else if (c == target) {
                return j;
            }
        }
        return -1;
    }

    private static char charAt(String s, int index, int from, int to) {
        return (index < from || index >= to) ? ' ' : s.charAt(index);
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c);
    }

    private static void escapeInto(char c, StringBuilder out) {
        if (SPECIALS.indexOf(c) >= 0) {
            out.append('\\');
        }
        out.append(c);
    }

    /**
     * Inside a code entity only the backslash and the backtick need escaping - everything else is
     * shown as written, which is the entire point of code.
     */
    private static String escapeCodeContent(String content) {
        return content.replace("\\", "\\\\").replace("`", "\\`");
    }

    private static String escapeUrl(String url) {
        return url.replace("\\", "\\\\").replace(")", "\\)");
    }
}
