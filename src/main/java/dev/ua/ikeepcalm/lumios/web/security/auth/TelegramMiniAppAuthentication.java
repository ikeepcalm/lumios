package dev.ua.ikeepcalm.lumios.web.security.auth;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

/**
 * A caller that proved itself by forwarding a valid Telegram {@code initData} rather than the API
 * key. Carries the chat it is allowed to read - the one its launch link was built for.
 */
@Getter
public class TelegramMiniAppAuthentication extends AbstractAuthenticationToken {

    private final long userId;
    private final long chatId;

    public TelegramMiniAppAuthentication(long userId, long chatId) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.userId = userId;
        this.chatId = chatId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }
}
