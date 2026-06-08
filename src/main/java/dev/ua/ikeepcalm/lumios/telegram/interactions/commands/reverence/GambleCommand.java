package dev.ua.ikeepcalm.lumios.telegram.interactions.commands.reverence;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosChat;
import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import dev.ua.ikeepcalm.lumios.telegram.core.annotations.BotCommand;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.ServicesShortcut;
import dev.ua.ikeepcalm.lumios.telegram.core.shortcuts.interfaces.Interaction;
import dev.ua.ikeepcalm.lumios.telegram.exceptions.MessageProcessingException;
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
                sendMessage(translationService.getMessage("gamble.error.limit", chat), message);
                return;
            }
        } catch (NumberFormatException e) {
            if (parts[1].equals("all")) {
                betAmount = user.getReverence();
            } else {
                sendMessage(translationService.getMessage("gamble.error.not_number", chat), message);
                return;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            sendMessage(translationService.getMessage("gamble.error.no_bet", chat), message);
            return;
        }

        if (betAmount <= 0) {
            sendMessage(translationService.getMessage("gamble.error.negative_bet", chat), message);
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
            resultMessage = resultMessage + generateWinMessage(betAmount, newReverence, chat);
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
            resultMessage = resultMessage + generateLoseMessage(betAmount, newReverence, chat);
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
        } catch (MessageProcessingException e) {
            throw new RuntimeException(e);
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
                    throw new RuntimeException(e);
                }
            }, 5, TimeUnit.MINUTES);
        }, 8, TimeUnit.SECONDS);
    }

    private boolean getRandomBoolean() {
        return Math.random() < 0.5;
    }

    private String generateWinMessage(int betAmount, int newReverence, LumiosChat chat) {
        String[] keys = {
                "gamble.win.1",
                "gamble.win.2",
                "gamble.win.3",
                "gamble.win.4",
                "gamble.win.5",
                "gamble.win.6",
                "gamble.win.7",
                "gamble.win.8",
                "gamble.win.9",
                "gamble.win.10",
                "gamble.win.11",
                "gamble.win.12"
        };
        String key = keys[new Random().nextInt(keys.length)];
        return translationService.getMessage(key, chat, betAmount, newReverence);
    }

    private String generateLoseMessage(int betAmount, int newReverence, LumiosChat chat) {
        String[] keys = {
                "gamble.lose.1",
                "gamble.lose.2",
                "gamble.lose.3",
                "gamble.lose.4",
                "gamble.lose.5",
                "gamble.lose.6",
                "gamble.lose.7",
                "gamble.lose.8",
                "gamble.lose.9",
                "gamble.lose.10",
                "gamble.lose.11",
                "gamble.lose.12"
        };
        String key = keys[new Random().nextInt(keys.length)];
        return translationService.getMessage(key, chat, betAmount, newReverence);
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

