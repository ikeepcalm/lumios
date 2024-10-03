package dev.ua.ikeepcalm.lumios.telegram.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownValidator {

    private static final Pattern BOLD_PATTERN = Pattern.compile("\\*(.*?)\\*");
    private static final Pattern ITALIC_PATTERN = Pattern.compile("_(.*?)_");
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[(.*?)\\]\\((.*?)\\)");

    private static final String[] MARKDOWN_SYMBOLS = {"_", "*", "[", "]", "(", ")"};

    public static String checkAndResolveMarkdown(String message) {
        message = preserveValidMarkdown(message);
        message = escapeInvalidSymbols(message);

        return message;
    }

    private static String preserveValidMarkdown(String message) {
        message = preservePattern(BOLD_PATTERN, message);   // Bold
        message = preservePattern(ITALIC_PATTERN, message); // Italic
        message = preservePattern(LINK_PATTERN, message);   // Links
        return message;
    }

    private static String preservePattern(Pattern pattern, String message) {
        Matcher matcher = pattern.matcher(message);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String escapeInvalidSymbols(String message) {
        for (String symbol : MARKDOWN_SYMBOLS) {
            message = escapeInvalidSymbol(message, symbol);
        }
        return message;
    }

    private static String escapeInvalidSymbol(String message, String symbol) {
        return message.replaceAll("(?<!\\\\)" + Pattern.quote(symbol), "\\\\" + symbol);
    }

}
