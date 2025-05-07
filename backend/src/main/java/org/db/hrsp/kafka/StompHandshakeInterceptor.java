package org.db.hrsp.kafka;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Arrays;
import java.util.Map;

@Slf4j
@AllArgsConstructor
public class StompHandshakeInterceptor implements HandshakeInterceptor {

    private String APP_TOKEN;

    private JwtDecoder jwtDecoder;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {


        try {
            // Extract tokens from both headers and query parameters
            String appToken = extractParam(request, "X-APP-TOKEN");
            String authToken = extractParam(request, "jwt");

            if (appToken == null || !appToken.equals(APP_TOKEN)) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                response.getHeaders().set("Connection", "close");
                return false;
            }

            if (authToken == null) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                response.getHeaders().set("Connection", "close");
                return false;
            }

            Jwt jwt = jwtDecoder.decode(authToken);
            attributes.put("jwt", jwt);
            return true;

        } catch (JwtException e) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            response.getHeaders().set("Connection", "close");
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }


    private String extractParam(ServerHttpRequest request, String tokenName) {
        // Check headers first
        String headerValue = request.getHeaders().getFirst(tokenName);
        if (headerValue != null) return headerValue;

        // Fallback to query parameters
        String query = request.getURI().getQuery();
        if (query != null) {
            return Arrays.stream(query.split("&"))
                    .map(param -> param.split("="))
                    .filter(arr -> arr[0].equals(tokenName))
                    .findFirst()
                    .map(arr -> arr.length > 1 ? arr[1] : "")
                    .orElse(null);
        }
        return null;
    }
}