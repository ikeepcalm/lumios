
package dev.ua.ikeepcalm.queueupnow.telegram.modules.impl.system.commands;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import dev.ua.ikeepcalm.queueupnow.telegram.modules.parents.CommandParent;
import dev.ua.ikeepcalm.queueupnow.telegram.wrappers.TextMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.Collections;


@Component
public class AuthCommand extends CommandParent {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.url}")
    private String AUTH_URL;

    @Override
    public void processUpdate(Message message) {
        try {
            if (!message.getChat().getType().equals("private")) {
                return;
            }

            Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
            String token = JWT.create()
                    .withIssuer("lumios")
                    .withIssuedAt(new java.util.Date())
                    .withExpiresAt(new java.util.Date(System.currentTimeMillis() + 3600000))
                    .withClaim("id", message.getFrom().getId())
                    .withClaim("username", message.getFrom().getUserName())
                    .withClaim("languageCode", message.getFrom().getLanguageCode())
                    .sign(algorithm);

            TextMessage textMessage = new TextMessage();
            textMessage.setChatId(message.getChatId());
            textMessage.setText("""
                    *Авторизація*
                    
                    Перейдіть за посиланням, яке висвітилося кнопкою під цим повідомленням!
                    """);
            textMessage.setParseMode(ParseMode.MARKDOWN);
            InlineKeyboardRow inlineKeyboardRow = new InlineKeyboardRow();
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton("\uD83D\uDC6E Авторизуватися");
            inlineKeyboardButton.setUrl(AUTH_URL + token);
            inlineKeyboardRow.add(inlineKeyboardButton);
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(Collections.singletonList(inlineKeyboardRow));
            textMessage.setReplyKeyboard(inlineKeyboardMarkup);
            telegramClient.sendTextMessage(textMessage);
        } catch (JWTCreationException exception) {
            sendMessage("Error while creating token", ParseMode.MARKDOWN);
        }
    }
}

