package dev.ua.ikeepcalm.lumios.telegram.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sanitizes text for Telegram MarkdownV2 format.
 * MarkdownV2 requires escaping these characters outside of code/pre entities:
 * _ * [ ] ( ) ~ ` > # + - = | { } . !
 */
public class MarkdownV2Sanitizer {

    // Patterns to identify code blocks and inline code
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```[\\s\\S]*?```", Pattern.DOTALL);
    private static final Pattern INLINE_CODE_PATTERN = Pattern.compile("`[^`\n]+?`");

    // Characters that must be escaped in MarkdownV2 (outside code blocks)
    private static final String CHARS_TO_ESCAPE = "_*[]()~`>#+-=|{}.!";

    /**
     * Sanitizes AI-generated text for MarkdownV2.
     * Preserves code blocks and inline code, escapes special characters everywhere else.
     */
    public static String sanitize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // First, fix any unclosed code blocks
        text = fixUncloseCodeBlocks(text);

        // Strategy: Replace code blocks with placeholders, escape special chars, then restore
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;

        // Find all code blocks (both ``` and `)
        Matcher codeBlockMatcher = CODE_BLOCK_PATTERN.matcher(text);
        Matcher inlineCodeMatcher = INLINE_CODE_PATTERN.matcher(text);

        // Combine both matchers into a list of protected regions
        java.util.List<Region> protectedRegions = new java.util.ArrayList<>();

        while (codeBlockMatcher.find()) {
            protectedRegions.add(new Region(codeBlockMatcher.start(), codeBlockMatcher.end(), codeBlockMatcher.group()));
        }

        while (inlineCodeMatcher.find()) {
            protectedRegions.add(new Region(inlineCodeMatcher.start(), inlineCodeMatcher.end(), inlineCodeMatcher.group()));
        }

        // Sort regions by start position
        protectedRegions.sort((a, b) -> Integer.compare(a.start, b.start));

        // Remove overlapping regions (code blocks take precedence)
        protectedRegions = removeOverlaps(protectedRegions);

        // Process text: escape chars outside protected regions, keep protected regions as-is
        for (Region region : protectedRegions) {
            // Process text before this region
            if (region.start > lastEnd) {
                String beforeRegion = text.substring(lastEnd, region.start);
                result.append(escapeMarkdownV2(beforeRegion));
            }
            // Add the protected region as-is
            result.append(region.content);
            lastEnd = region.end;
        }

        // Process remaining text after last region
        if (lastEnd < text.length()) {
            String afterRegions = text.substring(lastEnd);
            result.append(escapeMarkdownV2(afterRegions));
        }

        return result.toString();
    }

    /**
     * Escapes special MarkdownV2 characters in plain text (not in code blocks)
     */
    private static String escapeMarkdownV2(String text) {
        StringBuilder escaped = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (CHARS_TO_ESCAPE.indexOf(c) >= 0) {
                escaped.append('\\');
            }
            escaped.append(c);
        }
        return escaped.toString();
    }

    /**
     * Fixes unclosed code blocks by adding closing markers
     */
    private static String fixUncloseCodeBlocks(String text) {
        // Count triple backticks
        int tripleBacktickCount = countOccurrences(text, "```");
        if (tripleBacktickCount % 2 != 0) {
            text += "\n```";
        }

        // Count single backticks (excluding those in triple backticks)
        String withoutCodeBlocks = text.replaceAll("```[\\s\\S]*?```", "");
        int singleBacktickCount = countOccurrences(withoutCodeBlocks, "`");
        if (singleBacktickCount % 2 != 0) {
            text += "`";
        }

        return text;
    }

    /**
     * Counts occurrences of a substring
     */
    private static int countOccurrences(String text, String substring) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }

    /**
     * Removes overlapping regions (keeps the first one)
     */
    private static java.util.List<Region> removeOverlaps(java.util.List<Region> regions) {
        if (regions.isEmpty()) {
            return regions;
        }

        java.util.List<Region> result = new java.util.ArrayList<>();
        Region current = regions.getFirst();

        for (int i = 1; i < regions.size(); i++) {
            Region next = regions.get(i);
            if (next.start >= current.end) {
                // No overlap, add current and move to next
                result.add(current);
                current = next;
            } else {
                // Overlap, extend current if needed
                if (next.end > current.end) {
                    current = new Region(current.start, next.end,
                        current.content + next.content.substring(current.end - next.start));
                }
            }
        }
        result.add(current);

        return result;
    }

    /**
     * Represents a protected region (code block or inline code)
     */
    private static class Region {
        final int start;
        final int end;
        final String content;

        Region(int start, int end, String content) {
            this.start = start;
            this.end = end;
            this.content = content;
        }
    }
}
