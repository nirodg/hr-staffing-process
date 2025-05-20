package org.db.hrsp.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.db.hrsp.api.common.ConflictException;
import org.db.hrsp.api.common.NotFoundException;
import org.db.hrsp.common.LogMethodExecution;
import org.db.hrsp.service.repository.UserRepository;
import org.db.hrsp.service.repository.model.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@LogMethodExecution
public class UserService {

    private final UserRepository repository;

    @Transactional
    public User updatePosition(String username, String newPosition) {
        return repository.findByUsername(username)
                .map(u -> {
                    u.setPosition(newPosition);
                    return repository.save(u);
                })
                .orElseThrow(() ->
                        new NotFoundException("User %s not found".formatted(username)));
    }

    @Transactional
    public User save(User user) {
        try {
            return repository.save(user);
        } catch (DataIntegrityViolationException dup) {
            throw new ConflictException("User with the same username or email already exists");
        }
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

    public List<User> findAll() {
        return repository.findAll();
    }
}
