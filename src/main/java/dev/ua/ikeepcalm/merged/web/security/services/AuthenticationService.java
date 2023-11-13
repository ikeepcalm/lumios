package dev.ua.ikeepcalm.merged.web.security.services;

import dev.ua.ikeepcalm.merged.web.security.auth.ApiKeyAuthentication;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

@Component
@PropertySource(value={"classpath:thirdparty.properties"})
public class AuthenticationService {

    @Value("${rest.api.key}")
    private String AUTH_TOKEN;

    @Value("${rest.api.key.header.name}")
    private String AUTH_TOKEN_HEADER_NAME;

    public Authentication getAuthentication(HttpServletRequest request) {
        String apiKey = request.getHeader(AUTH_TOKEN_HEADER_NAME);
        if (apiKey == null || !apiKey.equals(AUTH_TOKEN)) {
            throw new BadCredentialsException("Invalid API Key");
        } return new ApiKeyAuthentication("key", AuthorityUtils.NO_AUTHORITIES);
    }

}
