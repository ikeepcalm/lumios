
package dev.ua.ikeepcalm.merged.telegram.modules.system.commands;

import dev.ua.ikeepcalm.merged.database.dal.interfaces.*;
import dev.ua.ikeepcalm.merged.telegram.AbsSender;
import dev.ua.ikeepcalm.merged.telegram.modules.CommandParent;
import dev.ua.ikeepcalm.merged.telegram.modules.queues.utils.QueueLifecycleUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;


@Component
public class HelpCommand extends CommandParent{

    @Override
    public void processUpdate(Message message) {
        instantiateUpdate(message);
        String helpText = """
                *ВСЕ ВІДНОСНО ЧЕРГ*
                /queue <Назва> - Створити чергу із заданою назвою
                                        
                Подальша взаємодія із чергами реалізується натисканням відповідних кнопок під повідомленням від боту:
                - \ud83d\udd3c Приєднатися - Доєднатися у кінець черги
                - ✅ Я вже все - Вийти з голови черги, і сповістити наступного
                - \ud83d\udd04 Вийти - Вийти не з голови черги, без сповіщення
                """ +
                """
                        \n*ВСЕ ВІДНОСНО ПОВАГИ*
                        /register - Зареєструвати свій акаунт у цьому чаті
                        /stats - Переглянути загальну статистику поваги в цьому чаті
                        /me - Переглянути власну статистику поваги в цьому чаті
                        /increase \\[@User] \\[к-сть] - Додати користувачу повагу
                        /decrease \\[@User] \\[к-сть] - Відняти повагу в користувача
                        /shop - Збільшити щоденний ліміт кредитів
                        /everyone - Покликати усіх учасників чату
                        """ +
                """
                        \n*ВСЕ ВІДНОСНО ЗАВДАНЬ*
                        /task \\[dd.mm.year] \\[HH:mm] \\[Назва] <Посилання> - Створити
                        /edit \\[ID] [dd.mm.year] [HH:mm] \\[Назва] <Посилання> - Редагувати
                         /whatisduetoday - Список усіх завдань
                        """ +
                """
                        \n*ВІДНОСНО АРГУМЕНТІВ*
                        <Аргумент> і \\[Аргумент] відрізняються. В чому різниця? \\[Аргумент] є обов'язковим, <Аргумент>  - ні
                        """;
        sendMessage(helpText);
    }
}

