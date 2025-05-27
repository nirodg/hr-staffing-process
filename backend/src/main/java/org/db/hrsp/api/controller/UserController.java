package org.db.hrsp.api.controller;

import lombok.AllArgsConstructor;
import org.db.hrsp.api.dto.UserDTO;
import org.db.hrsp.common.LogMethodExecution;
import org.db.hrsp.service.UserService;
import org.db.hrsp.service.repository.UserRepository;
import org.db.hrsp.service.repository.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
//@AllArgsConstructor
@LogMethodExecution
public class UserController {

    private final UserRepository userRepository;
    private final UserService service;

    public UserController(UserRepository userRepository, UserService service) {
        this.userRepository = userRepository;
        this.service = service;
    }

    @GetMapping()
    public List<UserDTO> getAllUsers() {
        return service.getAll();
    }

    @GetMapping("/my_account")
    public User getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return service.getByJwt(jwt);
    }

    @GetMapping("/{username}/account")
    public Optional<User> getById(@PathVariable("username") String username) {
        return service.findByUsername(username);
    }

    @DeleteMapping("/my_account")
    public void deleteMyProfile(String username) {
        service.findByUsername(username).map(user -> {
            userRepository.delete(user);
            return null;
        });
    }
    @PutMapping("/me")
    public UserDTO updateMyProfile(@RequestBody UserDTO input) {
        return service.updateCurrentUser(input);
    }

}
