package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.queues;

import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleQueue;
import dev.ua.ikeepcalm.lumios.database.entities.queue.SimpleUser;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.database.exceptions.NoSuchEntityException;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.MessageFormatter;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@BotCommand(command = "restore")
public class RestoreCommand extends ServicesShortcut implements Interaction {

    private static final Pattern USER_PATTERN = Pattern.compile("ID: \\d+ - (.+) \\(@(.+)\\)");

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();

        if (message.getReplyToMessage() == null) {
            sendMessage(MessageFormatter.formatWarningMessage("Відповідайте на повідомлення черги, яку потрібно відновити!"), message);
            return;
        }

        Message repliedMessage = message.getReplyToMessage();

        if (!(repliedMessage.getReplyMarkup() instanceof InlineKeyboardMarkup markup)) {
            sendMessage(MessageFormatter.formatWarningMessage("Це повідомлення не є чергою!"), message);
            return;
        }

        UUID queueId;
        try {
            String callbackData = markup.getKeyboard().getFirst().getFirst().getCallbackData();
            if (!callbackData.endsWith("-simple-join")) {
                sendMessage(MessageFormatter.formatWarningMessage("Це повідомлення не є звичайною чергою!"), message);
                return;
            }
            queueId = UUID.fromString(callbackData.replace("-simple-join", ""));
        } catch (Exception e) {
            sendMessage(MessageFormatter.formatErrorMessage("Не вдалося розпізнати ID черги!"), message);
            return;
        }

        try {
            queueService.findSimpleById(queueId);
            sendMessage(MessageFormatter.formatInfoMessage("Ця черга вже існує у базі даних і не потребує відновлення!"), message);
            return;
        } catch (NoSuchEntityException ignored) {
            // Queue doesn't exist — proceed with restore
        }

        String text = repliedMessage.getText();
        if (text == null || !text.startsWith(">>> ")) {
            sendMessage(MessageFormatter.formatWarningMessage("Не вдалося розпізнати формат черги!"), message);
            return;
        }

        int aliasEnd = text.indexOf(" <<<");
        if (aliasEnd < 4) {
            sendMessage(MessageFormatter.formatWarningMessage("Не вдалося розпізнати назву черги!"), message);
            return;
        }
        String alias = text.substring(4, aliasEnd);

        SimpleQueue simpleQueue = new SimpleQueue(alias);
        simpleQueue.setId(queueId);
        simpleQueue.setChatId(message.getChatId());
        simpleQueue.setMessageId(repliedMessage.getMessageId());

        Matcher matcher = USER_PATTERN.matcher(text);
        long placeholderId = -1L;
        while (matcher.find()) {
            SimpleUser simpleUser = new SimpleUser();
            simpleUser.setName(matcher.group(1));
            simpleUser.setUsername(matcher.group(2));
            simpleUser.setAccountId(placeholderId--);
            simpleQueue.getContents().add(simpleUser);
        }

        queueService.save(simpleQueue);
        sendMessage(MessageFormatter.formatSuccessMessage("Чергу \"" + alias + "\" успішно відновлено!"), message);
    }
}
