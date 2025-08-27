package dev.ua.ikeepcalm.lumios.telegram.utils;

import org.telegram.telegrambots.meta.api.methods.ParseMode;

public class MessageFormatter {
    
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
}