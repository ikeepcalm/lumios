package dev.ua.ikeepcalm.lumios.telegram.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownValidator {

    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(.*?)```", Pattern.DOTALL);
    private static final Pattern INLINE_CODE_PATTERN = Pattern.compile("`([^`]+)`");
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[(.*?)]\\((.*?)\\)");
    private static final Pattern BOLD_PATTERN = Pattern.compile("\\*(.*?)\\*");
    private static final Pattern ITALIC_PATTERN = Pattern.compile("_(.*?)_");

    // Symbols to escape if they appear outside of valid markdown entities
    // In Telegram Markdown V1, these are the main troublemakers if unclosed/unmatched.
    private static final String[] SYMBOLS_TO_ESCAPE = {"*", "_", "`", "["};

    public static String checkAndResolveMarkdown(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        Map<String, String> placeholders = new HashMap<>();

        // Order matters! Code blocks first to protect content inside them.
        message = replacePattern(message, CODE_BLOCK_PATTERN, placeholders);
        message = replacePattern(message, INLINE_CODE_PATTERN, placeholders);
        message = replacePattern(message, LINK_PATTERN, placeholders);
        message = replacePattern(message, BOLD_PATTERN, placeholders);
        message = replacePattern(message, ITALIC_PATTERN, placeholders);

        // Escape remaining symbols that could break parsing
        for (String symbol : SYMBOLS_TO_ESCAPE) {
            // Use replace (literal) instead of replaceAll (regex) for safety
            // We want to replace literal "*" with literal "\\*"
            // String.replace(CharSequence, CharSequence) does literal replacement
            message = message.replace(symbol, "\\" + symbol);
        }

        // Restore placeholders
        // We must be careful not to resolve placeholders that were inside other placeholders
        // But since we extracted them sequentially, it should be fine.
        // Actually, we just replace all keys back to values.
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }

        return message;
    }

    private static String replacePattern(String text, Pattern pattern, Map<String, String> placeholders) {
        Matcher matcher = pattern.matcher(text);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String placeholder = "MD_PH_" + UUID.randomUUID().toString().replace("-", "") + "_END";
            placeholders.put(placeholder, matcher.group());
            matcher.appendReplacement(sb, placeholder);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}