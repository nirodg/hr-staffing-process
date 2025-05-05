package org.db.hrsp.kafka;

import lombok.RequiredArgsConstructor;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@EnableWebSocketSecurity
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/backend-updates"); // frontend subscribes to this
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        KeycloakSpringBootProperties configuration = new KeycloakSpringBootProperties();
        registry.addEndpoint("/ws")
                .addInterceptors(new StompHandshakeInterceptor(configuration))
                .setAllowedOrigins("http://localhost:4200")
                .withSockJS(); // fallback
    }

    @Bean
    AuthorizationManager<Message<?>> messageAuthorizationManager(
            MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        messages.simpDestMatchers("/ws/**")
                .hasAnyRole("CLIENT_PUBLIC_USER", "CLIENT_PUBLIC_ADMIN")
                .anyMessage()
                .authenticated();
        return messages.build();
    }
}
