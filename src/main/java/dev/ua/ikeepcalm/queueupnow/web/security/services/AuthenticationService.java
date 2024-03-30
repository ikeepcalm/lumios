package dev.ua.ikeepcalm.queueupnow.web.security.services;

import dev.ua.ikeepcalm.queueupnow.web.security.auth.ApiKeyAuthentication;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationService {

    @Value("${rest.api.key}")
    private String AUTH_TOKEN;

    @Value("${rest.api.header}")
    private String AUTH_TOKEN_HEADER_NAME;

    public Authentication getAuthentication(HttpServletRequest request) {
        String apiKey = request.getHeader(AUTH_TOKEN_HEADER_NAME);
        if (apiKey == null || !apiKey.equals(AUTH_TOKEN)) {
            throw new BadCredentialsException("Invalid API Key! Please provide a valid API Key!");
        } return new ApiKeyAuthentication("key", AuthorityUtils.NO_AUTHORITIES);
    }

}
