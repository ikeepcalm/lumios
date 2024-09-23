package dev.ua.ikeepcalm.lumios.web.security.filters;

import dev.ua.ikeepcalm.lumios.web.security.services.AuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class AuthenticationFilter extends GenericFilterBean {

    private static final String[] AUTH_WHITELIST = {
            "/v2/api-docs",
            "v2/api-docs",
            "/swagger-resources",
            "swagger-resources",
            "/swagger-resources/",
            "swagger-resources/",
            "/configuration/ui",
            "configuration/ui",
            "/configuration/security",
            "configuration/security",
            "/swagger-ui.html",
            "swagger-ui.html",
            "webjars/**",
            "/v3/api-docs/",
            "v3/api-docs/",
            "/swagger-ui/",
            "swagger-ui/",
    };

    private final AuthenticationService authenticationService;

    public AuthenticationFilter(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String path = ((HttpServletRequest) request).getRequestURI();

        for (String s : AUTH_WHITELIST) {
            if (path.startsWith(s)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        try {
            Authentication authentication = authenticationService.getAuthentication((HttpServletRequest) request);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (BadCredentialsException exp) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            if (!httpResponse.isCommitted()) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
                PrintWriter writer = httpResponse.getWriter();
                writer.print(exp.getMessage());
                writer.flush();
                writer.close();
            } else {
                logger.warn("Response is already committed. Cannot modify the response.");
            }
            return;
        }
        filterChain.doFilter(request, response);
    }
}
