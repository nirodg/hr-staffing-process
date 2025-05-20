package org.db.hrsp.graphql;


import lombok.RequiredArgsConstructor;
import org.db.hrsp.api.dto.EmployeeDTO;
import org.db.hrsp.api.dto.UserDTO;
import org.db.hrsp.api.dto.mapper.UserMapper;
import org.db.hrsp.service.UserService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserGraphQLController {

    private final UserService userService;
    private final UserMapper userMapper;

    @QueryMapping
    public List<UserDTO> getUsers() {
        return userService.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @QueryMapping
    public UserDTO getUser(@Argument Long id) {
        return userService.findAll().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .map(userMapper::toDto)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}