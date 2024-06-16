package dev.ua.ikeepcalm.lumios.telegram.modules.impl.games.commands;

import dev.ua.ikeepcalm.lumios.telegram.modules.impl.games.utils.TenorUtil;
import dev.ua.ikeepcalm.lumios.telegram.modules.parents.CommandParent;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.MediaMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.RemoveMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class GambleCommand extends CommandParent {

    private static final Logger log = LoggerFactory.getLogger(GambleCommand.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final TenorUtil tenorUtil;

    public GambleCommand(TenorUtil tenorUtil) {
        this.tenorUtil = tenorUtil;
    }

    @Override
    public void processUpdate(Message message) {
        String commandText = message.getText();
        String[] parts = commandText.split("\\s+", 2);
        int betAmount;
        try {
            betAmount = Integer.parseInt(parts[1]);
            if (betAmount > reverenceUser.getReverence() * 0.3) {
                sendMessage("Стривай-но! Ти не можеш грати на суму більше ніж 30% від свого рівня поваги. Спробуй ще раз");
                return;
            }
        } catch (NumberFormatException e) {
            if (parts[1].equals("all") || parts[0].equals("/gamble_all")) {
                betAmount = reverenceUser.getReverence();
            } else {
                sendMessage("Хіба це схоже на число? Спробуй ще раз. Наприклад: /gamble 100");
                return;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            sendMessage("Ти забув вказати суму ставки. Спробуй ще раз. Наприклад: /gamble 100");
            return;
        }

        if (betAmount <= 0) {
            sendMessage("Ти не можеш грати з від'ємною або нульовою сумою. Спробуй ще раз");
            return;
        }

        Random random = new Random();
        boolean win = random.nextBoolean();

        int newReverence;
        String resultMessage = "@" + reverenceUser.getUsername() + "\n\n";
        int randomInt = random.nextInt(10);
        InputFile animation;
        if (win) {
            if (betAmount == reverenceUser.getReverence()) {
                newReverence = (int) (reverenceUser.getReverence() * 1.4);
            } else {
                newReverence = (int) (reverenceUser.getReverence() + (betAmount * 0.5));
            }
            resultMessage = resultMessage + generateWinMessage(betAmount, newReverence);
            JSONObject winGifs = tenorUtil.getSearchResults(generateWinKeyword(), 10);
            if (winGifs.isEmpty()) {
                animation = new InputFile(new File("img/win.gif"));
            } else {
                try {
                    URI uri = new URI(winGifs.getJSONArray("results").getJSONObject(randomInt).getJSONObject("media_formats").getJSONObject("gif").getString("url"));
                    String name = winGifs.getJSONArray("results").getJSONObject(randomInt).getString("content_description");
                    animation = new InputFile(uri.toURL().openStream(), name + ".gif");
                } catch (URISyntaxException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            if (betAmount == reverenceUser.getReverence()) {
                newReverence = (int) (reverenceUser.getReverence() * 0.5);
            } else {
                newReverence = (int) (reverenceUser.getReverence() - (betAmount * 0.6));
            }
            resultMessage = resultMessage + generateLoseMessage(betAmount, newReverence);
            JSONObject loseGifs = tenorUtil.getSearchResults(generateLoseKeyword(), 10);
            if (loseGifs.isEmpty()) {
                animation = new InputFile(new File("img/lose.gif"));
            } else {
                try {
                    URI uri = new URI(loseGifs.getJSONArray("results").getJSONObject(randomInt).getJSONObject("media_formats").getJSONObject("gif").getString("url"));
                    String name = loseGifs.getJSONArray("results").getJSONObject(randomInt).getString("content_description");
                    animation = new InputFile(uri.toURL().openStream(), name + ".gif");
                } catch (URISyntaxException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (newReverence < 0) {
            newReverence = 0;
        }
        Message sent;
        boolean isCaption;
        try {
            sent = telegramClient.sendAnimation(new MediaMessage(message.getMessageId(), message.getChatId(), null, animation));
            isCaption = true;
        } catch (RuntimeException e) {
            sent = telegramClient.sendTextMessage(new TextMessage("Mercury snake biting its tail... Кручу колесо, кидаю кубики...", message.getChatId(), message.getMessageId(), null, null, null));
            isCaption = false;
        }

        reverenceUser.setReverence(newReverence);
        String finalResultMessage = resultMessage;
        Message finalSent = sent;
        boolean finalIsCaption = isCaption;
        scheduler.schedule(() -> {
            EditMessage editMessage = new EditMessage();
            editMessage.setMessageId(finalSent.getMessageId());
            editMessage.setChatId(finalSent.getChatId());
            editMessage.setText(finalResultMessage);
            Message sentMessage = telegramClient.sendEditMessage(editMessage, finalIsCaption);
            scheduler.schedule(() -> {
                try {
                    telegramClient.sendRemoveMessage(new RemoveMessage(sentMessage.getMessageId(), sentMessage.getChatId()));
                    telegramClient.sendRemoveMessage(new RemoveMessage(message.getMessageId(), message.getChatId()));
                } catch (TelegramApiException e) {
                    log.error("Failed to delete message: {}", sentMessage.getText());
                }
            }, 5, TimeUnit.MINUTES);
        }, 8, TimeUnit.SECONDS);
    }

    private String generateWinMessage(int betAmount, int newReverence) {
        String[] messages = {
                "Леді Фортуна посміхається тобі! Ти виграв ставку розміром " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Так тримати!",
                "Ти справжній везунчик! Ти виграв ставку розміром " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Продовжуй в тому ж дусі!",
                "Ти продав свою душу дияволу, але це варте того! Ти виграв ставку розміром " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Не забувай, що справжній гравець ніколи не здавається!",
                "Невже ти відкрив секрет великої перемоги? Ти виграв ставку розміром " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Так тримати!",
                "...і він виграв! Ти виграв ставку розміром " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Так тримати!",
                "Дідько його бери, як він це робить! Ти виграв ставку розміром " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Так тримати!",
                "Не вірю очам, це що, знову перемога? Ти виграв ставку розміром " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Так тримати!",
                "Ти виграв ставку розміром " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Так тримати!",
                "Коли він народився, увесь світ прошептав його ім'я - переможець! Ти виграв ставку розміром " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Так тримати!",
                "Хакарі б пишався тобою! Ти виграв ставку розміром " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Так тримати!",
                "Зажди! Навчи мене! Ти виграв ставку розміром " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Так тримати!",
                "Ти виграв, ти великий, ти найкращий! Ти виграв ставку розміром " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Так тримати!",
        };
        return randomMessage(messages);
    }

    private String generateLoseMessage(int betAmount, int newReverence) {
        String[] messages = {
                "Ти програв ставку розміром " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Не здавайся, у тебе ще є шанс відігратися!",
                "Що таке? Ти програв ставку розміром " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Не здавайся, у тебе ще є шанс відігратися!",
                "ХА-ХА, ОЦЕ ЛУЗЕР! Ти програв ставку розміром " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Не здавайся, у тебе ще є шанс відігратися!",
                "Забув помолитися перед грою? Ти програв ставку розміром " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Не здавайся, у тебе ще є шанс відігратися!",
                "Яка ж шкода! Ти програв ставку розміром " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Не здавайся, у тебе ще є шанс відігратися!",
                "Невезуча мавпа! Ти програв ставку розміром " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Не здавайся, у тебе ще є шанс відігратися!",
                "Сьогодні не твій день. Ти програв ставку розміром " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Не здавайся, у тебе ще є шанс відігратися!",
                "ХАХВАХВХАХАВХ ЦЕ Ж ТИ? Ти програв ставку розміром " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Не здавайся, у тебе ще є шанс відігратися!",
                "ЯКЕ ЖАЛЮГІДНЕ! Ти програв ставку розміром " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Не здавайся, у тебе ще є шанс відігратися!",
                "Ти програв, ти в нуліну, твоя мама тебе не любить, твій тато тебе не любить, твої друзі тебе не люблять, твій кіт тебе не любить, твій сусід тебе не любить, твій бос тебе не любить! Ти програв ставку розміром " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Не здавайся, у тебе ще є шанс відігратися!",
                "Ти програв ставку розміром " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Не здавайся, у тебе ще є шанс відігратися!",
                "Лісова мавпа... Ти програв ставку розміром " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Не здавайся, у тебе ще є шанс відігратися!"
        };
        return randomMessage(messages);
    }

    private String generateWinKeyword() {
        String[] messages = {
                "Jackpot",
                "Casino",
                "Luck",
                "Lucky",
                "Gamble",
                "Rich",
                "Royal Flush",
                "JJK",
                "Anime",
                "Amon",
                "Mahoraga",
                "With this treasure I summon",
                "Honored one"
        };
        return randomMessage(messages);
    }

    private String generateLoseKeyword() {
        String[] messages = {
                "Loser",
                "Casino",
                "Bankrupt",
                "Dark Souls",
                "Elden Ring",
                "JJK",
                "Anime",
                "Tokyo Ghoul",
                "Kaiju No. 8",
                "Fuck up"
        };
        return randomMessage(messages);
    }

    private String randomMessage(String[] messages) {
        return messages[new Random().nextInt(messages.length)];
    }
}

