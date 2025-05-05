package org.db.hrsp.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.rotation.AdapterTokenVerifier;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.common.VerificationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class StompHandshakeInterceptor implements HandshakeInterceptor {
    private final KeycloakSpringBootProperties configuration;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        List<String> protocols = request.getHeaders().get("Sec-WebSocket-Protocol");
        try {
            String token = protocols.get(0).split(", ")[2];
            log.debug("Token: " + token);
            AdapterTokenVerifier.verifyToken(token, KeycloakDeploymentBuilder.build(configuration));
            response.setStatusCode(HttpStatus.SWITCHING_PROTOCOLS);
            log.debug("token valid");
        } catch (IndexOutOfBoundsException e) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        } catch (VerificationException e) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            log.error(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
