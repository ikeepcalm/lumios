package dev.ua.ikeepcalm.lumios.telegram.interactions.commands;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.utils.TenorUtil;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.MediaMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.RemoveMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.TextMessage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@BotCommand(command = "gamble", aliases = ("gamble_all"))
public class GambleCommand extends ServicesShortcut implements Interaction {

    private static final Logger log = LoggerFactory.getLogger(GambleCommand.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final TenorUtil tenorUtil;

    public GambleCommand(TenorUtil tenorUtil) {
        this.tenorUtil = tenorUtil;
    }

    @Override
    public void fireInteraction(Update update, LumiosUser user, LumiosChat chat) {
        Message message = update.getMessage();
        String commandText = message.getText();
        commandText = commandText.replace("@lumios_bot", "");
        commandText = commandText.replace("_", " ");
        String[] parts = commandText.split("\\s+", 2);
        int betAmount;
        try {
            betAmount = Integer.parseInt(parts[1]);
            if (betAmount > user.getReverence() * 0.3) {
                sendMessage("Стривай-но! Ти не можеш грати на суму більше ніж 30% від свого рівня поваги. Спробуй ще раз", message);
                return;
            }
        } catch (NumberFormatException e) {
            if (parts[1].equals("all")) {
                betAmount = user.getReverence();
            } else {
                sendMessage("Хіба це схоже на число? Спробуй ще раз. Наприклад: /gamble 100", message);
                return;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            sendMessage("Ти забув вказати суму ставки. Спробуй ще раз. Наприклад: /gamble 100", message);
            return;
        }

        if (betAmount <= 0) {
            sendMessage("Ти не можеш грати з від'ємною або нульовою сумою. Спробуй ще раз", message);
            return;
        }

        boolean win = RNG().nextBoolean();

        int newReverence;
        String resultMessage = "@" + user.getUsername() + "\n\n";
        int randomInt = RNG().nextInt(10);
        InputFile animation;
        if (win) {
            if (betAmount == user.getReverence()) {
                newReverence = (int) (user.getReverence() * 1.5);
            } else {
                newReverence = (int) (user.getReverence() + (betAmount * 0.5));
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
            if (betAmount == user.getReverence()) {
                newReverence = (int) (user.getReverence() * 0.5);
            } else {
                newReverence = (int) (user.getReverence() - (betAmount * 0.7));
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
            sent = telegramClient.sendTextMessage(new TextMessage("Never stop gambling, because next time you might hit a jackpot!", message.getChatId(), message.getMessageId(), null, null, null));
            isCaption = false;
        }

        user.setReverence(newReverence);
        String finalResultMessage = resultMessage;
        Message finalSent = sent;
        boolean finalIsCaption = isCaption;
        userService.save(user);
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

    private boolean getRandomBoolean() {
        return Math.random() < 0.5;
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
                "Gojo Satoru",
                "Anime",
                "Mahoraga",
                "Gojo"
        };
        return randomMessage(messages);
    }

    private String generateLoseKeyword() {
        String[] messages = {
                "Loser",
                "Casino",
                "Bankrupt",
                "Dark Souls",
                "Wasted",
                "Died",
                "Death",
                "Elden Ring",
                "JJK",
                "Anime",
        };
        return randomMessage(messages);
    }

    @Contract(pure = true, value = "-> new")
    private @NotNull SecureRandom RNG() {
        return new SecureRandom(SecureRandom.getSeed(20));
    }

    private String randomMessage(String[] messages) {
        return messages[new Random().nextInt(messages.length)];
    }
}

