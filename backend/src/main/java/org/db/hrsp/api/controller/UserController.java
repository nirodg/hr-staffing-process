package org.db.hrsp.api.controller;

import lombok.AllArgsConstructor;
import org.db.hrsp.common.LogMethodExecution;
import org.db.hrsp.service.repository.UserRepository;
import org.db.hrsp.service.repository.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
@LogMethodExecution
public class UserController {
    private final UserRepository userRepository;

    @GetMapping()
    public ResponseEntity<Iterable<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/my_account")
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

    @DeleteMapping("/my_account")
    public ResponseEntity<Object> deleteMyProfile(String username) {
        return userRepository.findByUsername(username).map(user -> {
            userRepository.delete(user);
            return ResponseEntity.noContent().build();
        }).orElse(ResponseEntity.notFound().build());
    }

}
