package org.db.hrsp.api.config.security;

import org.db.hrsp.kafka.model.KafkaPayload;
import org.db.hrsp.kafka.producers.PersistEventProducer;
import org.db.hrsp.service.repository.model.Role;
import org.db.hrsp.service.repository.model.User;
import org.db.hrsp.service.repository.UserRepository;
import com.nimbusds.jose.shaded.gson.internal.LinkedTreeMap;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Component
public class JwtInterceptor implements HandlerInterceptor {


    public static final String CLIENT_PUBLIC_ADMIN = "client_public_admin";
    public static final String CLIENT_PUBLIC_USER = "client_public_user";
    private final UserRepository userRepository;

    private final HttpServletRequest request;
    private final PersistEventProducer eventProducer;

    private User currentUser;

    @Autowired
    public JwtInterceptor(UserRepository userRepository, HttpServletRequest request, PersistEventProducer eventProducer) {
        this.userRepository = userRepository;
        this.request = request;
        this.eventProducer = eventProducer;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            log.warn("No JWT found in security context.");
            return true;
        }

        return !getUserFromToken(jwt);
    }

    public User getCurrentUser() {
        return userRepository.findByUsername(currentUser.getUsername()).orElseGet(() -> {
            log.warn("User not found in database, returning null.");
            return null;
        });
    }

    private boolean getUserFromToken(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        if (username == null) {
            log.warn("No username found in JWT.");
            return true;
        }

        this.currentUser = userRepository.findByUsername(username).orElseGet(() -> {
            User newAccount = User.builder()
                    .username(username)
                    .firstName(jwt.getClaimAsString("given_name"))
                    .lastName(jwt.getClaimAsString("family_name"))
                    .email(jwt.getClaimAsString("email"))
                    .available(true) // Default value for availability, perhaps some stronger logic here
                    .position("n/a") // Default value for position, perhaps some stronger logic here
                    .build();

            ArrayList<String> jwtRoles = (ArrayList<String>) ((LinkedTreeMap<?, ?>) jwt.getClaim("realm_access")).get("roles");
            boolean isAdmin = jwtRoles.contains(CLIENT_PUBLIC_ADMIN);
            boolean isUser = jwtRoles.contains(CLIENT_PUBLIC_USER);

            if (isAdmin) {
                newAccount.setRoles(List.of(Role.CLIENT_PUBLIC_ADMIN));
            } else if (isUser) {
                newAccount.setRoles(List.of(Role.CLIENT_PUBLIC_USER));
            }
            newAccount = userRepository.save(newAccount);
            eventProducer.publishEvent(
                    KafkaPayload.builder()
                            .action(KafkaPayload.Action.CREATE)
                            .userId(newAccount.getUsername())
                            .topic(KafkaPayload.Topic.EMPLOYEES)
                            .build());
            return newAccount;
        });

        return false;
    }

}
