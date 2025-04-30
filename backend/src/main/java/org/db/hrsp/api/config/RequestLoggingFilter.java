package org.db.hrsp.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String fullUrl = request.getRequestURL().toString();
        String queryString = request.getQueryString();

        if (queryString != null) {
            fullUrl += "?" + queryString;
        }

        log.debug("🔍 Incoming request: " + request.getMethod() + " " + fullUrl);

        filterChain.doFilter(request, response);
    }
}
