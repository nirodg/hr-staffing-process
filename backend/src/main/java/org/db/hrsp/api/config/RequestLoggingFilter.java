package org.db.hrsp.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Value("${app.security.token}")
    private String APP_TOKEN;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
    	String appToken = request.getHeader("X-APP-TOKEN");

        String fullUrl = request.getRequestURL().toString();
        boolean isWssUrl = fullUrl.matches(".*\\/ws\\/.*");

        if (!APP_TOKEN.equals(appToken) && !isWssUrl) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid or missing X-APP-TOKEN");
            return;
        }

        String queryString = request.getQueryString();

        if (queryString != null) {
            fullUrl += "?" + queryString;
        }

        log.debug("🔍 Incoming request: " + request.getMethod() + " " + fullUrl);

        filterChain.doFilter(request, response);
    }
}
