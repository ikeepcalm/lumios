package dev.ua.ikeepcalm.lumios.telegram.utils.markup;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.source.AiModel;
import dev.ua.ikeepcalm.lumios.telegram.utils.TranslationService;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class SettingsMarkupUtil {

    public static InlineKeyboardMarkup getSettingsKeyboard(LumiosChat lumiosChat, TranslationService translationService) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardButton timetableEnabled;
        if (lumiosChat.isTimetableEnabled()) {
            timetableEnabled = new InlineKeyboardButton(translationService.getMessage("settings.timetable.enabled", lumiosChat));
            timetableEnabled.setCallbackData("settings-timetable-disable");
        } else {
            timetableEnabled = new InlineKeyboardButton(translationService.getMessage("settings.timetable.disabled", lumiosChat));
            timetableEnabled.setCallbackData("settings-timetable-enable");
        }

        InlineKeyboardRow secondRow = new InlineKeyboardRow();
        InlineKeyboardButton diceEnabled;
        if (lumiosChat.isDiceEnabled()) {
            diceEnabled = new InlineKeyboardButton(translationService.getMessage("settings.dice.enabled", lumiosChat));
            diceEnabled.setCallbackData("settings-dice-disable");
        } else {
            diceEnabled = new InlineKeyboardButton(translationService.getMessage("settings.dice.disabled", lumiosChat));
            diceEnabled.setCallbackData("settings-dice-enable");
        }

        InlineKeyboardRow thirdRow = new InlineKeyboardRow();
        InlineKeyboardButton aiEnabled;
        if (lumiosChat.isAiEnabled()) {
            aiEnabled = new InlineKeyboardButton(translationService.getMessage("settings.ai.enabled", lumiosChat));
            aiEnabled.setCallbackData("settings-ai-disable");
        } else {
            aiEnabled = new InlineKeyboardButton(translationService.getMessage("settings.ai.disabled", lumiosChat));
            aiEnabled.setCallbackData("settings-ai-enable");
        }

        InlineKeyboardRow fourthRow = new InlineKeyboardRow();
        if (lumiosChat.isAiEnabled()) {

            if (lumiosChat.getAiModel() == null) {
                lumiosChat.setAiModel(AiModel.OPENAI);
            }

            switch (lumiosChat.getAiModel()) {
                case GEMINI -> {
                    InlineKeyboardButton geminiEnabled = new InlineKeyboardButton("Gemini ✅");
                    geminiEnabled.setCallbackData("settings-ai-gemini");
                    fourthRow.add(geminiEnabled);
                }
                case OPENAI -> {
                    InlineKeyboardButton openAIEnabled = new InlineKeyboardButton("OpenAI ✅");
                    openAIEnabled.setCallbackData("settings-ai-openai");
                    fourthRow.add(openAIEnabled);
                }
            }
        }

        firstRow.add(timetableEnabled);
        secondRow.add(diceEnabled);
        thirdRow.add(aiEnabled);

        keyboard.add(firstRow);

        if (lumiosChat.isTimetableEnabled()) {
            InlineKeyboardRow plainRow = new InlineKeyboardRow();
            InlineKeyboardButton plainTimetable;
            if (lumiosChat.isPlainTimetableEnabled()) {
                plainTimetable = new InlineKeyboardButton(translationService.getMessage("settings.plain.enabled", lumiosChat));
                plainTimetable.setCallbackData("settings-plain-timetable-disable");
            } else {
                plainTimetable = new InlineKeyboardButton(translationService.getMessage("settings.plain.disabled", lumiosChat));
                plainTimetable.setCallbackData("settings-plain-timetable-enable");
            }
            plainRow.add(plainTimetable);
            keyboard.add(plainRow);
        }

        keyboard.add(secondRow);
        keyboard.add(thirdRow);
        keyboard.add(fourthRow);
        
        if (lumiosChat.isAiEnabled()) {
            InlineKeyboardRow nicknameRow = new InlineKeyboardRow();
            InlineKeyboardButton nicknameButton = new InlineKeyboardButton(translationService.getMessage("settings.nickname", lumiosChat));
            nicknameButton.setCallbackData("settings-nickname");
            nicknameRow.add(nicknameButton);
            keyboard.add(nicknameRow);
        }

        // Language Row
        InlineKeyboardRow languageRow = new InlineKeyboardRow();
        InlineKeyboardButton languageButton;
        if ("en".equals(lumiosChat.getLanguage())) {
            // Currently English -> show button to switch to Ukrainian
            languageButton = new InlineKeyboardButton(translationService.getMessage("settings.language.switch_to_uk", lumiosChat));
            languageButton.setCallbackData("settings-lang-uk");
        } else {
            // Currently Ukrainian -> show button to switch to English
            languageButton = new InlineKeyboardButton(translationService.getMessage("settings.language.switch_to_en", lumiosChat));
            languageButton.setCallbackData("settings-lang-en");
        }
        languageRow.add(languageButton);
        keyboard.add(languageRow);

        return new InlineKeyboardMarkup(keyboard);
    }

}
