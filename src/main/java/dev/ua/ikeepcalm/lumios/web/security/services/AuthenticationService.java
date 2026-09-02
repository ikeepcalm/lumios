package dev.ua.ikeepcalm.lumios.web.security.services;

import dev.ua.ikeepcalm.lumios.web.security.auth.ApiKeyAuthentication;
import dev.ua.ikeepcalm.lumios.web.security.auth.TelegramMiniAppAuthentication;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationService {

    /**
     * The only request a Mini App session is allowed to make.
     * <p>
     * Its initData proves the caller is a real Telegram user who opened the app, and that the group
     * id in {@code start_param} survived the trip through the browser unaltered - but not that they
     * belong to that group, since anyone can craft a startapp link for a chat id they already know.
     * Reading a timetable everyone in the chat can see anyway is a fair trade for that; deleting one
     * is not, so writes stay behind the API key.
     */
    private static final String MINI_APP_PATH = "/timetables/retrieve";

    private static final String CHAT_ID_HEADER = "chatId";

    @Value("${rest.api.key}")
    private String AUTH_TOKEN;

    @Value("${rest.api.header}")
    private String AUTH_TOKEN_HEADER_NAME;

    private final TelegramInitDataService telegramInitDataService;

    public AuthenticationService(TelegramInitDataService telegramInitDataService) {
        this.telegramInitDataService = telegramInitDataService;
    }

    public Authentication getAuthentication(HttpServletRequest request) {
        String initData = request.getHeader(TelegramInitDataService.HEADER);
        if (initData != null && !initData.isBlank()) {
            return authenticateMiniApp(request, initData);
        }

        String apiKey = request.getHeader(AUTH_TOKEN_HEADER_NAME);
        if (apiKey == null || !apiKey.equals(AUTH_TOKEN)) {
            throw new BadCredentialsException("Invalid API Key! Please provide a valid API Key!");
        }
        return new ApiKeyAuthentication("key", AuthorityUtils.NO_AUTHORITIES);
    }

    private Authentication authenticateMiniApp(HttpServletRequest request, String initData) {
        if (!HttpMethod.GET.matches(request.getMethod()) || !MINI_APP_PATH.equals(request.getRequestURI())) {
            throw new BadCredentialsException("Telegram Mini App sessions may only read the timetable!");
        }

        TelegramInitDataService.InitData verified = telegramInitDataService.verify(initData);

        Long chatId = parseChatId(request.getHeader(CHAT_ID_HEADER));
        Long startParam = parseChatId(verified.startParam());
        if (chatId == null || !chatId.equals(startParam)) {
            throw new BadCredentialsException("Requested chatId does not match the chat the Mini App was opened for!");
        }

        return new TelegramMiniAppAuthentication(verified.userId(), chatId);
    }

    private static Long parseChatId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
