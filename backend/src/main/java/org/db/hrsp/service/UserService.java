package org.db.hrsp.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.db.hrsp.common.LogMethodExecution;
import org.db.hrsp.service.repository.UserRepository;
import org.db.hrsp.service.repository.model.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
@LogMethodExecution
public class UserService {

    private final UserRepository repository;

    @Transactional
    public void updatePosition(String username, String newPosition) {
        repository.findByUsername(username).ifPresent(user -> {
            user.setPosition(newPosition);
            repository.save(user);
        });
    }

    @Transactional
    public User save(User user) {
        return repository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username);
    }
}
