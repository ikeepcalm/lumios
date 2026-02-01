package dev.ua.ikeepcalm.lumios.telegram.utils;

import org.telegram.telegrambots.meta.api.methods.ParseMode;

import java.util.ArrayList;
import java.util.List;

public class MessageFormatter {

    private static final int MAX_MESSAGE_LENGTH = 4096;
    
    public static final String BOT_NAME = "Lumios";
    public static final String SUCCESS_EMOJI = "✅";
    public static final String ERROR_EMOJI = "❌";
    public static final String WARNING_EMOJI = "⚠️";
    public static final String INFO_EMOJI = "ℹ️";
    public static final String LOADING_EMOJI = "⏳";
    
    public static String escapeMarkdown(String text) {
        if (text == null) return null;
        return text.replaceAll("([_*\\[\\]()~`>#+\\-=|{}.!])", "\\\\$1");
    }
    
    public static String formatSuccessMessage(String message) {
        return SUCCESS_EMOJI + " " + message;
    }
    
    public static String formatErrorMessage(String message) {
        return ERROR_EMOJI + " " + message;
    }
    
    public static String formatWarningMessage(String message) {
        return WARNING_EMOJI + " " + message;
    }
    
    public static String formatInfoMessage(String message) {
        return INFO_EMOJI + " " + message;
    }
    
    public static String formatLoadingMessage(String message) {
        return LOADING_EMOJI + " " + message;
    }
    
    public static String formatBoldText(String text) {
        return "*" + escapeMarkdown(text) + "*";
    }
    
    public static String formatItalicText(String text) {
        return "_" + escapeMarkdown(text) + "_";
    }
    
    public static String formatCodeText(String text) {
        return "`" + text + "`";
    }
    
    public static String formatCodeBlock(String text, String language) {
        return "```" + (language != null ? language : "") + "\n" + text + "\n```";
    }
    
    public static String formatTitle(String title) {
        return ">>> " + formatBoldText(title) + " <<<\n\n";
    }
    
    public static String formatQueueEntry(int id, String name, String username) {
        return String.format("ID: %d - %s (@%s)\n", id, name, username);
    }
    
    public static String formatUserMention(String username, Long userId) {
        if (username != null && !username.isEmpty()) {
            return "@" + username;
        }
        return "[User](tg://user?id=" + userId + ")";
    }
    
    public static String formatApiErrorMessage(int errorCode, String operation) {
        return switch (errorCode) {
            case 400 -> formatErrorMessage("Невірний запит під час виконання операції: " + operation);
            case 401 -> formatErrorMessage("Недійсний токен бота");
            case 403 -> formatErrorMessage("Бот не має дозволу на виконання цієї дії");
            case 404 -> formatErrorMessage("Ресурс не знайдено");
            case 429 -> formatWarningMessage("Занадто багато запитів. Спробуйте пізніше");
            case 500 -> formatErrorMessage("Внутрішня помилка сервера Telegram");
            default -> formatErrorMessage("Помилка API: " + errorCode + " під час " + operation);
        };
    }
    
    public static String formatHelpSection(String sectionTitle, String content) {
        return "\n" + formatBoldText(sectionTitle) + "\n" + content;
    }
    
    public static String getDefaultParseMode() {
        return ParseMode.MARKDOWNV2;
    }

    /**
     * Sanitizes AI-generated text for MarkdownV2 format.
     * Use this for all AI responses to prevent Telegram parsing errors.
     */
    public static String sanitizeMarkdownV2(String text) {
        return MarkdownV2Sanitizer.sanitize(text);
    }

    /**
     * Splits long messages into chunks that fit within Telegram's limit
     */
    public static List<String> chunkMessage(String text, String parseMode) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.length() <= MAX_MESSAGE_LENGTH) {
            chunks.add(text);
            return chunks;
        }

        // Try to split at paragraph boundaries first
        String[] paragraphs = text.split("\n\n");
        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            // If single paragraph is too long, split at sentence boundaries
            if (paragraph.length() > MAX_MESSAGE_LENGTH) {
                if (currentChunk.length() > 0) {
                    chunks.add(sanitizeChunk(currentChunk.toString(), parseMode));
                    currentChunk = new StringBuilder();
                }
                chunks.addAll(splitLongParagraph(paragraph, parseMode));
                continue;
            }

            // Check if adding this paragraph exceeds limit
            if (currentChunk.length() + paragraph.length() + 2 > MAX_MESSAGE_LENGTH) {
                chunks.add(sanitizeChunk(currentChunk.toString(), parseMode));
                currentChunk = new StringBuilder(paragraph);
            } else {
                if (currentChunk.length() > 0) {
                    currentChunk.append("\n\n");
                }
                currentChunk.append(paragraph);
            }
        }

        if (currentChunk.length() > 0) {
            chunks.add(sanitizeChunk(currentChunk.toString(), parseMode));
        }

        // Add continuation markers
        for (int i = 0; i < chunks.size(); i++) {
            if (i < chunks.size() - 1) {
                chunks.set(i, chunks.get(i) + "\n\n_(продовження...)_");
            }
            if (i > 0) {
                chunks.set(i, "_(продовження)_\n\n" + chunks.get(i));
            }
        }

        return chunks;
    }

    /**
     * Splits a long paragraph at sentence boundaries
     */
    private static List<String> splitLongParagraph(String paragraph, String parseMode) {
        List<String> chunks = new ArrayList<>();
        String[] sentences = paragraph.split("(?<=[.!?])\\s+");
        StringBuilder currentChunk = new StringBuilder();

        for (String sentence : sentences) {
            if (sentence.length() > MAX_MESSAGE_LENGTH) {
                // Sentence itself is too long, do hard split
                if (currentChunk.length() > 0) {
                    chunks.add(sanitizeChunk(currentChunk.toString(), parseMode));
                    currentChunk = new StringBuilder();
                }
                chunks.addAll(hardSplitText(sentence, MAX_MESSAGE_LENGTH - 100));
                continue;
            }

            if (currentChunk.length() + sentence.length() + 1 > MAX_MESSAGE_LENGTH) {
                chunks.add(sanitizeChunk(currentChunk.toString(), parseMode));
                currentChunk = new StringBuilder(sentence);
            } else {
                if (currentChunk.length() > 0) {
                    currentChunk.append(" ");
                }
                currentChunk.append(sentence);
            }
        }

        if (currentChunk.length() > 0) {
            chunks.add(sanitizeChunk(currentChunk.toString(), parseMode));
        }

        return chunks;
    }

    /**
     * Hard splits text at character boundaries (last resort)
     */
    private static List<String> hardSplitText(String text, int maxLength) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxLength, text.length());
            chunks.add(text.substring(start, end));
            start = end;
        }
        return chunks;
    }

    /**
     * Sanitizes a chunk based on parse mode
     */
    private static String sanitizeChunk(String chunk, String parseMode) {
        if (ParseMode.MARKDOWN.equals(parseMode) || ParseMode.MARKDOWNV2.equals(parseMode)) {
            return sanitizeMarkdownV2(chunk);
        }
        return chunk;
    }
}