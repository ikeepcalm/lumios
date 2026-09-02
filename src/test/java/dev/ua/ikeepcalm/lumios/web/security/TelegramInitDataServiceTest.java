package dev.ua.ikeepcalm.lumios.web.security;

import dev.ua.ikeepcalm.lumios.web.security.services.TelegramInitDataService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The signature is the whole reason a group id may travel through the browser, so it is worth
 * proving that a forged one is actually rejected - a validator that accepts everything looks exactly
 * like a working one from the outside.
 */
class TelegramInitDataServiceTest {

    private static final String TOKEN = "123456:AA-Fake-Token-For-Tests_0000000000000";

    private final TelegramInitDataService service = new TelegramInitDataService(TOKEN);

    @Test
    @DisplayName("a genuine launch yields its user and startapp value")
    void acceptsGenuineInitData() {
        String initData = sign(Map.of(
                "user", "{\"id\":42,\"first_name\":\"Тест\"}",
                "chat_type", "supergroup",
                "start_param", "-1001767321866",
                "auth_date", String.valueOf(Instant.now().getEpochSecond())));

        TelegramInitDataService.InitData verified = service.verify(initData);

        assertThat(verified.userId()).isEqualTo(42L);
        assertThat(verified.startParam()).isEqualTo("-1001767321866");
    }

    @Test
    @DisplayName("swapping the group id for another one invalidates the signature")
    void rejectsTamperedStartParam() {
        String initData = sign(Map.of(
                "user", "{\"id\":42}",
                "start_param", "-1001767321866",
                "auth_date", String.valueOf(Instant.now().getEpochSecond())))
                .replace("-1001767321866", "-1009999999999");

        assertThatThrownBy(() -> service.verify(initData))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("initData signed with somebody else's bot token is rejected")
    void rejectsForeignToken() {
        String initData = sign(Map.of(
                "user", "{\"id\":42}",
                "auth_date", String.valueOf(Instant.now().getEpochSecond())));

        assertThatThrownBy(() -> new TelegramInitDataService("999:Some-Other-Token").verify(initData))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("a launch older than a day is rejected even though it is properly signed")
    void rejectsStaleInitData() {
        String initData = sign(Map.of(
                "user", "{\"id\":42}",
                "auth_date", String.valueOf(Instant.now().minusSeconds(60 * 60 * 25).getEpochSecond())));

        assertThatThrownBy(() -> service.verify(initData))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("a link opened without startapp carries no chat")
    void allowsMissingStartParam() {
        String initData = sign(Map.of(
                "user", "{\"id\":42}",
                "auth_date", String.valueOf(Instant.now().getEpochSecond())));

        assertThat(service.verify(initData).startParam()).isNull();
    }

    /**
     * Builds the query string Telegram would hand the frontend: fields sorted by key, hashed with a
     * key derived from the bot token, then percent-encoded the way {@code encodeURIComponent} does.
     */
    private static String sign(Map<String, String> fields) {
        Map<String, String> sorted = new TreeMap<>(fields);
        String checkString = sorted.entrySet().stream()
                .map(field -> field.getKey() + "=" + field.getValue())
                .collect(Collectors.joining("\n"));

        byte[] secret = hmac("WebAppData".getBytes(UTF_8), TOKEN.getBytes(UTF_8));
        String hash = HexFormat.of().formatHex(hmac(secret, checkString.getBytes(UTF_8)));

        return sorted.entrySet().stream()
                .map(field -> field.getKey() + "=" + encode(field.getValue()))
                .collect(Collectors.joining("&")) + "&hash=" + hash;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, UTF_8).replace("+", "%20");
    }

    private static byte[] hmac(byte[] key, byte[] message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(message);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
