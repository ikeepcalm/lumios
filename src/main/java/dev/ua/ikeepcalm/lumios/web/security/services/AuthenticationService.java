package dev.ua.ikeepcalm.lumios.web.security.services;

import dev.ua.ikeepcalm.lumios.web.security.auth.ApiKeyAuthentication;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationService {

    private final Environment environment;

    public AuthenticationService(Environment environment) {
        this.environment = environment;
    }

    public Authentication getAuthentication(HttpServletRequest request) {
        String apiKey = request.getHeader(environment.getProperty("REST_API_HEADER"));
        if (apiKey == null || !apiKey.equals(environment.getProperty("REST_API_KEY"))) {
            throw new BadCredentialsException("Invalid API Key! Please provide a valid API Key!");
        }
        return new ApiKeyAuthentication("key", AuthorityUtils.NO_AUTHORITIES);
    }

}
