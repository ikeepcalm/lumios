package dev.ua.ikeepcalm.lumios.telegram.modules.impl.games.commands;

import dev.ua.ikeepcalm.lumios.telegram.modules.impl.games.utils.TenorUtil;
import dev.ua.ikeepcalm.lumios.telegram.modules.parents.CommandParent;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.EditMessage;
import dev.ua.ikeepcalm.lumios.telegram.wrappers.MediaMessage;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.Timer;

@Component
public class SoloGambleCommand extends CommandParent {

    private final TenorUtil tenorUtil;

    public SoloGambleCommand(TenorUtil tenorUtil) {
        this.tenorUtil = tenorUtil;
    }

    @Override
    public void processUpdate(Message message) {
        String commandText = message.getText();
        String[] parts = commandText.split("\\s+", 2);
        int betAmount;
        try {
            betAmount = Integer.parseInt(parts[1]);
            if (betAmount > reverenceUser.getReverence() * 0.4) {
                sendMessage("Стривай-но! Ти не можеш грати на суму більше 40% від свого рівня поваги. Спробуй ще раз");
                return;
            }
        } catch (NumberFormatException e) {
            if (parts[1].equals("all")) {
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
            newReverence = reverenceUser.getReverence() + betAmount;
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
            newReverence = reverenceUser.getReverence() - betAmount;
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
        try {
            sent = telegramClient.sendAnimation(new MediaMessage(message.getMessageId(), message.getChatId(), null, animation));
        } catch (RuntimeException e) {
            sendMessage("Занадто багато запитів на сервер. Спробуй ще раз через декілька хвилин!");
            return;
        }

        reverenceUser.setReverence(newReverence);
        String finalResultMessage = resultMessage;
        new Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        EditMessage editMessage = new EditMessage();
                        editMessage.setMessageId(sent.getMessageId());
                        editMessage.setChatId(sent.getChatId());
                        editMessage.setText(finalResultMessage);
                        telegramClient.sendEditMessage(editMessage, true);
                    }
                }, 8000);
    }

    private String generateWinMessage(int betAmount, int newReverence) {
        String[] messages = {
                "Леді Фортуна посміхається тобі! Ти виграв " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Так тримати!",
                "Ти справжній везунчик! Ти виграв " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Продовжуй в тому ж дусі!",
                "Ти продав свою душу дияволу, але це варте того! Ти виграв " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Не забувай, що справжній гравець ніколи не здавається!",
                "Невже ти відкрив секрет великої перемоги? Ти виграв " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Так тримати!",
                "...і він виграв! Ти виграв " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Так тримати!",
                "Дідько його бери, як він це робить! Ти виграв " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Так тримати!",
                "Не вірю очам, це що, знову перемога? Ти виграв " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Так тримати!",
                "Ти виграв " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Так тримати!",
                "Коли він народився, увесь світ прошептав його ім'я - переможець! Ти виграв " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Так тримати!",
                "Хакарі б пишався тобою! Ти виграв " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Так тримати!",
                "Зажди! Навчи мене! Ти виграв " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Так тримати!",
        };
        return randomMessage(messages);
    }

    private String generateLoseMessage(int betAmount, int newReverence) {
        String[] messages = {
                "Ти програв " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Не здавайся, у тебе ще є шанс відігратися!",
                "Що таке? Ти програв " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Не здавайся, у тебе ще є шанс відігратися!",
                "ХА-ХА, ОЦЕ ЛУЗЕР! Ти програв " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Не здавайся, у тебе ще є шанс відігратися!",
                "Забув помолитися перед грою? Ти програв " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Не здавайся, у тебе ще є шанс відігратися!",
                "Яка ж шкода! Ти програв " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Не здавайся, у тебе ще є шанс відігратися!",
                "Невезуча мавпа! Ти програв " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Не здавайся, у тебе ще є шанс відігратися!",
                "Сьогодні не твій день. Ти програв " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Не здавайся, у тебе ще є шанс відігратися!",
                "ХАХВАХВХАХАВХ ЦЕ Ж ТИ? Ти програв " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Не здавайся, у тебе ще є шанс відігратися!",
                "ЯКЕ ЖАЛЮГІДНЕ! Ти програв " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Не здавайся, у тебе ще є шанс відігратися!",
                "Ти програв, ти в нуліну, твоя мама тебе не любить, твій тато тебе не любить, твої друзі тебе не люблять, твій кіт тебе не любить, твій сусід тебе не любить, твій бос тебе не любить! Ти програв " + betAmount + " поваги і тепер у тебе " + newReverence + " поваги. Не здавайся, у тебе ще є шанс відігратися!"
        };
        return randomMessage(messages);
    }

    private String generateWinKeyword() {
        String[] messages = {
                "Jackpot!",
                "Casino",
                "Rich",
                "Royal Flush",
                "Hakari jjk",
                "Jujutsu Kaisen",
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
                "Hakari jjk",
                "Jujutsu Kaisen",
        };
        return randomMessage(messages);
    }

    private String randomMessage(String[] messages) {
        return messages[new Random().nextInt(messages.length)];
    }
}

