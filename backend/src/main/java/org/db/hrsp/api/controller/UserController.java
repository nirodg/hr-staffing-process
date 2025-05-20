package org.db.hrsp.api.controller;

import lombok.AllArgsConstructor;
import org.db.hrsp.common.LogMethodExecution;
import org.db.hrsp.service.UserService;
import org.db.hrsp.service.repository.UserRepository;
import org.db.hrsp.service.repository.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
@LogMethodExecution
public class UserController {

    private final UserRepository userRepository;
    private final UserService service;

    @GetMapping()
    public List<User> getAllUsers() {
        return service.findAll();
    }

    @GetMapping("/my_account")
    public User getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return service.getByJwt(jwt);
    }

    @DeleteMapping("/my_account")
    public void deleteMyProfile(String username) {
        service.findByUsername(username).map(user -> {
            userRepository.delete(user);
            return null;
        });
    }

}
