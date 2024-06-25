package dev.ua.ikeepcalm.lumios.telegram.interactions.commands;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;


@Component
@BotCommand(command = "help", aliases = ("start help"))
public class HelpCommand extends ServicesShortcut implements Interaction {

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        if (message.getChat().getType().equals("private")) {
            String helpText = """
                                      *ВСЕ СТОСОВНО ЧЕРГ*
                                      /queue <Назва> - Створити чергу із заданою назвою
                                      /mixed <Назва> - Створити мішану чергу із заданою назвою
                                                      
                                      Подальша взаємодія із чергами реалізується натисканням відповідних кнопок під повідомленням від боту:
                                      - Join \uD83D\uDD30 - Доєднатися у кінець черги
                                      - I'm done ✅ - Вийти з черги, і сповістити наступного
                                      - Leave \ud83d\udd04 - Вийти не з голови черги, без сповіщення
                                      - Delete ❌ - Видалити чергу, лише для адміністраторів
                                      - Notify ⚠ - Сповістити голову черги про його позицію
                                      - Shuffle \uD83D\uDD00 - Перемішати чергу у випадковому порядку
                                      """ +
                              """
                                      \n*ВСЕ СТОСОВНО ПОВАГИ*
                                      /stats - Переглянути загальну статистику поваги в цьому чаті
                                      /me - Переглянути власну статистику поваги в цьому чаті
                                                              
                                      Повага змінюється завдяки реакціям на повідомлення інших користувачів, кожна реакція має своє додатнє або від'ємне значення поваги. Підтримуються майже всі звичайні реакції в телеграмі і будь-які кастомні.
                                      """ +
                              """
                                      \n*ВСЕ СТОСОВНО РОЗКЛАДУ*
                                      /editor - Згенерувати посилання на веб-редактор
                                      /today - Подивитися розклад на сьогодні
                                      /tomorrow - Подивитися розклад на завтра
                                      /week - Подивитися розклад на тиждень
                                      /now - Посилання на поточну пару
                                      /next - Посилання на наступну пару
                                      """
                              +
                              """
                                      \n*ВСЕ СТОСОВНО ЗАВДАНЬ*
                                      /task \\[dd.mm.year] \\[HH:mm] \\[Назва] <Посилання> - Створити
                                      /edit \\[ID] \\[dd.mm.year] \\[HH:mm] \\[Назва] <Посилання> - Редагувати
                                      /due- Список усіх завдань
                                      """ +
                              """
                                      \n*СТОСОВНО АРГУМЕНТІВ*
                                      <Аргумент> і \\[Аргумент] відрізняються. В чому різниця? \\[Аргумент] є обов'язковим, <Аргумент>  - ні
                                      """;
            sendMessage(helpText, ParseMode.MARKDOWN, message);
        } else {
            String helpText = """
                    *Привіт, шкіряний мішок!*
                                        
                    Я можу допомогти тобі з кількома речами:
                    - Керування чергами
                    - Керування розкладом
                    - Керування завданнями
                    - Керування повагою
                    - Певні веселощі
                                        
                    Щоб дізнатися більше, використай цю ж команду у ПП зі мною
                    Якщо тобі подобається інший підхід, зазирни у довідку на сайті
                    """;
            TextMessage textMessage = new TextMessage();
            textMessage.setText(helpText);
            textMessage.setParseMode(ParseMode.MARKDOWN);
            textMessage.setChatId(message.getChatId());
            List<InlineKeyboardRow> keyboard = new ArrayList<>();
            InlineKeyboardRow firstRow = new InlineKeyboardRow();
            InlineKeyboardRow secondRow = new InlineKeyboardRow();
            InlineKeyboardButton website = new InlineKeyboardButton("\uD83C\uDF10 Онлайн довідка");
            InlineKeyboardButton pms = new InlineKeyboardButton("\uD83D\uDCBB Коротка довідка");
            website.setUrl("https://www.lumios.dev/tutorial");
            pms.setUrl("https://t.me/lumios_bot?start=help");
            firstRow.add(website);
            secondRow.add(pms);
            keyboard.add(firstRow);
            keyboard.add(secondRow);
            textMessage.setReplyKeyboard(new InlineKeyboardMarkup(keyboard));
            sendMessage(textMessage, message);
        }
    }
}

