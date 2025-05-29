package org.db.hrsp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.db.hrsp.api.common.NotFoundException;
import org.db.hrsp.api.config.security.JwtInterceptor;
import org.db.hrsp.api.dto.UserDTO;
import org.db.hrsp.api.dto.mapper.UserMapper;
import org.db.hrsp.common.LogMethodExecution;
import org.db.hrsp.kafka.KafkaPublisher;
import org.db.hrsp.kafka.model.KafkaPayload;
import org.db.hrsp.service.repository.UserRepository;
import org.db.hrsp.service.repository.model.Role;
import org.db.hrsp.service.repository.model.User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@LogMethodExecution
public class UserService extends AbstractService<User, UserDTO, UserRepository, UserMapper> {

    private final UserRepository repository;
    private final JwtInterceptor jwtInterceptor;
    private final UserMapper mapper;
    private final KafkaPublisher kafkaPublisher;

    @Transactional
    public void updatePosition(String username, String newPosition) {
        repository.findByUsername(username)
                .map(u -> {
                    u.setPosition(newPosition);
                    return repository.save(u);
                })
                .orElseThrow(() ->
                        new NotFoundException("User %s not found".formatted(username)));
    }

    public Optional<User> findByUsername(String username) {
        Optional<User> user = repository.findByUsername(username);
        if(user.isEmpty()){
            throw new NotFoundException("User %s not found".formatted(username));
        }
        return user;
    }

    public User getByJwt(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        return repository.findByUsername(username).orElseGet(() -> {
            User newUser = User.builder()
                    .username(username)
                    .firstName(jwt.getClaimAsString("given_name"))
                    .lastName(jwt.getClaimAsString("family_name"))
                    .email(jwt.getClaimAsString("email"))
                    .available(true) // Default value for availability, perhaps some stronger logic here
                    .build();
            return repository.save(newUser);
        });
    }

    public long countByRole(String role) {
        return repository.countByRoles(Role.valueOf(role));
    }

    public UserDTO updateCurrentUser(UserDTO input) {
        User user = jwtInterceptor.getCurrentUser();
        mapper.update(user, input);
        repository.save(user);
        kafkaPublisher.publish(KafkaPayload.Topic.EMPLOYEES, KafkaPayload.Action.UPDATE);
        return mapper.toDto(user);
    }
}
