package org.db.hrsp.controller;

import org.db.hrsp.model.User;
import org.db.hrsp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public User getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");

        return userRepository.findByUsername(username).orElseGet(() -> {
            User newUser = User.builder()
                    .username(username)
                    .firstName(jwt.getClaimAsString("given_name"))
                    .lastName(jwt.getClaimAsString("family_name"))
                    .email(jwt.getClaimAsString("email"))
                    .available(true) // Default value for availability, perhaps some stronger logic here
                    .build();
            return userRepository.save(newUser);
        });
    }

    @GetMapping
    public ResponseEntity<Iterable<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateMyProfile(
            String name,
            @RequestBody User updates
    ) {
        return userRepository.findByUsername(name).map(user -> {
            user.setFirstName(updates.getFirstName());
            user.setLastName(updates.getLastName());
            user.setPosition(updates.getPosition());
            user.setEmail(updates.getEmail());
            user.setAvailable(updates.isAvailable());
            return ResponseEntity.ok(userRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/me")
    public ResponseEntity<Object> deleteMyProfile(String username) {
        return userRepository.findByUsername(username).map(user -> {
            userRepository.delete(user);
            return ResponseEntity.noContent().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
