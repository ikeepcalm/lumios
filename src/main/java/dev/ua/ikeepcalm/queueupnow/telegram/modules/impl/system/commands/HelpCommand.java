
package dev.ua.ikeepcalm.queueupnow.telegram.modules.impl.system.commands;

import dev.ua.ikeepcalm.queueupnow.telegram.modules.parents.CommandParent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.message.Message;


@Component
public class HelpCommand extends CommandParent {

    @Override
    public void processUpdate(Message message) {
        String helpText = """
                *ВСЕ ВІДНОСНО ЧЕРГ*
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
                        \n*ВСЕ ВІДНОСНО ПОВАГИ*
                        /stats - Переглянути загальну статистику поваги в цьому чаті
                        /me - Переглянути власну статистику поваги в цьому чаті
                        /shop - Збільшити щоденний ліміт кредитів
                        
                        Повага змінюється завдяки реакціям на повідомлення інших користувачів, кожна реакція має своє додатнє або від'ємне значення поваги. Підтримуються майже всі звичайні реакції в телеграмі.
                        """ +
                """
                        \n*ВСЕ ВІДНОСНО РОЗКЛАДУ*
                        /feed - Згенерувати посилання на веб-редактор
                        /today - Подивитися розклад на сьогодні
                        /week - Подивитися розклад на тиждень
                        """
                +
                """
                        \n*ВСЕ ВІДНОСНО ЗАВДАНЬ*
                        /task \\[dd.mm.year] \\[HH:mm] \\[Назва] <Посилання> - Створити
                        /edit \\[ID] \\[dd.mm.year] \\[HH:mm] \\[Назва] <Посилання> - Редагувати
                        /due- Список усіх завдань
                        """ +
                """
                        \n*ВІДНОСНО АРГУМЕНТІВ*
                        <Аргумент> і \\[Аргумент] відрізняються. В чому різниця? \\[Аргумент] є обов'язковим, <Аргумент>  - ні
                        """;
        sendMessage(helpText, ParseMode.MARKDOWN);
    }
}

