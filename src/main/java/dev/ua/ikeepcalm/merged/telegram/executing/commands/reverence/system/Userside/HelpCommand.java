/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 *  org.telegram.telegrambots.meta.api.objects.Message
 */
package dev.ua.ikeepcalm.merged.telegram.executing.commands.reverence.system.Userside;

import dev.ua.ikeepcalm.merged.telegram.executing.Executable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class HelpCommand
extends Executable {
    public void execute(Message origin) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("""
                        *ВСЕ ВІДНОСНО ЧЕРГ*
                        /queue <Назва> - Створити чергу із заданою назвою\n
                        Подальша взаємодія із чергами реалізується натисканням відповідних кнопок під повідомленням від боту:
                        - \ud83d\udd3c Приєднатися - Доєднатися у кінець черги
                        - ✅ Я вже все - Вийти з голови черги, і сповістити наступного
                        - \ud83d\udd04 Вийти - Вийти не з голови черги, без сповіщення
                        """)
                .append("""
                        \n*ВСЕ ВІДНОСНО ПОВАГИ*
                        /register - Зареєструвати свій акаунт у цьому чаті
                        /stats - Переглянути загальну статистику поваги в цьому чаті
                        /me - Переглянути власну статистику поваги в цьому чаті
                        /increase \\[@User] \\[к-сть] - Додати користувачу повагу
                        /decrease \\[@User] \\[к-сть] - Відняти повагу в користувача
                        /shop - Збільшити щоденний ліміт кредитів
                        /everyone - Покликати усіх учасників чату
                        """)
                .append("""
                        \n*ВСЕ ВІДНОСНО ЗАВДАНЬ*
                        /task \\[dd.mm.year] \\[HH:mm] \\[Назва] <Посилання> - Створити
                        /edit \\[ID] dd.mm.year HH:mm \\[Назва] <Посилання> - Редагувати
                         /whatisduetoday - Список усіх завдань
                        """)
                .append("""
                        \n*ВІДНОСНО АРГУМЕНТІВ*
                        <Аргумент> і \\[Аргумент] відрізняються. В чому різниця? \\[Аргумент] є обов'язковим, <Аргумент>  - ні
                        """);

        sendMessage(origin, stringBuilder.toString());
    }
}

