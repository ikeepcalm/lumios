package dev.ua.ikeepcalm.lumios.telegram.utils.markup;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.source.AiModel;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class SettingsMarkupUtil {

    public static InlineKeyboardMarkup getSettingsKeyboard(LumiosChat lumiosChat) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow firstRow = new InlineKeyboardRow();
        InlineKeyboardButton timetableEnabled;
        if (lumiosChat.isTimetableEnabled()) {
            timetableEnabled = new InlineKeyboardButton("Сповіщення ✅");
            timetableEnabled.setCallbackData("settings-timetable-disable");
        } else {
            timetableEnabled = new InlineKeyboardButton("Сповіщення ❌");
            timetableEnabled.setCallbackData("settings-timetable-enable");
        }

        InlineKeyboardRow secondRow = new InlineKeyboardRow();
        InlineKeyboardButton diceEnabled;
        if (lumiosChat.isDiceEnabled()) {
            diceEnabled = new InlineKeyboardButton("Кубики ✅");
            diceEnabled.setCallbackData("settings-dice-disable");
        } else {
            diceEnabled = new InlineKeyboardButton("Кубики ❌");
            diceEnabled.setCallbackData("settings-dice-enable");
        }

        InlineKeyboardRow thirdRow = new InlineKeyboardRow();
        InlineKeyboardButton aiEnabled;
        if (lumiosChat.isAiEnabled()) {
            aiEnabled = new InlineKeyboardButton("AI ✅");
            aiEnabled.setCallbackData("settings-ai-disable");
        } else {
            aiEnabled = new InlineKeyboardButton("AI ❌");
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
        keyboard.add(secondRow);
        keyboard.add(thirdRow);
        keyboard.add(fourthRow);
        
        if (lumiosChat.isAiEnabled()) {
            InlineKeyboardRow nicknameRow = new InlineKeyboardRow();
            InlineKeyboardButton nicknameButton = new InlineKeyboardButton("Псевдонім бота");
            nicknameButton.setCallbackData("settings-nickname");
            nicknameRow.add(nicknameButton);
            keyboard.add(nicknameRow);
        }

        return new InlineKeyboardMarkup(keyboard);
    }

}
