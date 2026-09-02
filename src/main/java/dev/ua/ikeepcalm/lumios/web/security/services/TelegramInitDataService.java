package dev.ua.ikeepcalm.lumios.web.security.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Verifies the {@code initData} string Telegram hands to a Mini App frontend.
 * <p>
 * The frontend must forward it byte for byte in the {@link #HEADER} header: it is a signed query
 * string, so re-encoding it - re-ordering fields, dropping one, normalising whitespace - breaks the
 * signature. Everything the caller claims about itself, {@code start_param} included, is covered by
 * that signature, which is why the group id can travel through the client at all.
 */
@Component
public class TelegramInitDataService {

    public static final String HEADER = "X-Telegram-Init-Data";

    /**
     * A Mini App left open in the background keeps its original initData, so anything shorter would
     * log people out mid-session. Telegram's own examples use the same day-long window.
     */
    private static final Duration MAX_AGE = Duration.ofDays(1);

    private static final String HMAC_SHA256 = "HmacSHA256";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String botToken;

    public TelegramInitDataService(@Value("${telegram.bot.token}") String botToken) {
        this.botToken = botToken;
    }

    public InitData verify(String initData) {
        if (initData == null || initData.isBlank()) {
            throw new BadCredentialsException("Empty Telegram initData");
        }

        Map<String, String> fields = new TreeMap<>();
        String hash = null;
        for (String pair : initData.split("&")) {
            int separator = pair.indexOf('=');
            if (separator < 0) {
                throw new BadCredentialsException("Malformed Telegram initData");
            }
            String key = decode(pair.substring(0, separator));
            String value = decode(pair.substring(separator + 1));
            if ("hash".equals(key)) {
                hash = value;
            } else {
                fields.put(key, value);
            }
        }

        if (hash == null) {
            throw new BadCredentialsException("Telegram initData carries no hash");
        }

        // Every field but `hash` itself takes part, sorted by key - a TreeMap is already in that order.
        String checkString = fields.entrySet().stream()
                .map(field -> field.getKey() + "=" + field.getValue())
                .collect(Collectors.joining("\n"));

        byte[] secret = hmac("WebAppData".getBytes(UTF_8), botToken.getBytes(UTF_8));
        String expected = HexFormat.of().formatHex(hmac(secret, checkString.getBytes(UTF_8)));

        if (!MessageDigest.isEqual(expected.getBytes(UTF_8), hash.getBytes(UTF_8))) {
            throw new BadCredentialsException("Telegram initData signature does not match");
        }

        return new InitData(parseUserId(fields.get("user")), issuedAt(fields.get("auth_date")), fields.get("start_param"));
    }

    /**
     * @param userId     the Telegram user who opened the Mini App
     * @param startParam the {@code startapp} value the launch link carried, or null if it had none
     */
    public record InitData(long userId, Instant authDate, String startParam) {
    }

    private Instant issuedAt(String authDate) {
        if (authDate == null) {
            throw new BadCredentialsException("Telegram initData carries no auth_date");
        }
        Instant issued;
        try {
            issued = Instant.ofEpochSecond(Long.parseLong(authDate));
        } catch (NumberFormatException e) {
            throw new BadCredentialsException("Telegram initData carries a malformed auth_date");
        }
        if (issued.plus(MAX_AGE).isBefore(Instant.now())) {
            throw new BadCredentialsException("Telegram initData has expired");
        }
        return issued;
    }

    private long parseUserId(String user) {
        if (user == null) {
            throw new BadCredentialsException("Telegram initData identifies no user");
        }
        try {
            JsonNode id = objectMapper.readTree(user).get("id");
            if (id == null || !id.canConvertToLong()) {
                throw new BadCredentialsException("Telegram initData identifies no user");
            }
            return id.asLong();
        } catch (JsonProcessingException e) {
            throw new BadCredentialsException("Telegram initData carries a malformed user");
        }
    }

    /**
     * Telegram builds initData with {@code encodeURIComponent}, which escapes a literal plus as
     * {@code %2B}, so URLDecoder's habit of reading '+' as a space cannot corrupt a value here.
     */
    private static String decode(String value) {
        return URLDecoder.decode(value, UTF_8);
    }

    private static byte[] hmac(byte[] key, byte[] message) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(key, HMAC_SHA256));
            return mac.doFinal(message);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 is unavailable", e);
        }
    }
}
