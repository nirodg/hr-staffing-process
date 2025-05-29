package org.db.hrsp.graphql;

import org.apache.http.client.HttpResponseException;
import org.db.hrsp.api.common.ConflictException;
import org.db.hrsp.api.config.security.JwtInterceptor;
import org.db.hrsp.api.dto.EmployeeDTO;
import org.db.hrsp.api.dto.RoleDto;
import org.db.hrsp.api.dto.UserDTO;
import org.db.hrsp.kafka.KafkaPublisher;
import org.db.hrsp.kafka.model.KafkaPayload;
import org.db.hrsp.service.EmployeeService;
import org.db.hrsp.service.UserService;
import org.db.hrsp.service.repository.model.Role;
import org.db.hrsp.service.repository.model.User;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Controller
public class EmployeeGraphQLController {
    private final EmployeeService employeeService;
    private final UserService userService;
    private final JwtInterceptor jwt;
    private final KafkaPublisher kafkaPublisher;

    public EmployeeGraphQLController(EmployeeService EmployeeService, UserService userService, JwtInterceptor jwt, KafkaPublisher kafkaPublisher) {
        this.employeeService = EmployeeService;
        this.userService = userService;
        this.jwt = jwt;
        this.kafkaPublisher = kafkaPublisher;
    }

    @QueryMapping
    public EmployeeDTO getEmployee(@Argument Long id) {
        return employeeService.getEmployee(id);
    }

    @QueryMapping
    public List<EmployeeDTO> getEmployees() {
        return employeeService.getAllEmployees();
    }

    @MutationMapping
    public UserDTO updateUserInfo(@Argument Long id,
                                      @Argument UserDTO input) throws Throwable {
        User actor =  jwt.getCurrentUser();


        boolean isAdmin = actor.getRoles().stream()
                .anyMatch(role -> role.equals(Role.CLIENT_PUBLIC_ADMIN));

        if (!isAdmin) {
            throw new HttpResponseException(401, "Only ADMIN users can update employees.");
        }

        // Check if role change is attempted
        UserDTO userToChange = userService.getById(id);
        RoleDto currentRole = userToChange.getRoles().getFirst();
        RoleDto newRole = input.getRoles().getFirst();
        boolean isRoleChanging = !newRole.equals(currentRole);
        if (isRoleChanging && currentRole.equals(Role.CLIENT_PUBLIC_ADMIN)) {
            long adminCount = userService.countByRole("ADMIN");
            boolean isLastAdmin = adminCount <= 1;
            boolean isSelf = actor.getId().equals(input.getId());
            if (isLastAdmin) {
                throw new ConflictException("At least one ADMIN must remain in the system.");
            }

            if (isSelf && isLastAdmin) {
                throw new ConflictException("You cannot remove your own ADMIN role as the only admin.");
            }
        }

        UserDTO updated;
        try {
            updated = userService.update(id, input);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            kafkaPublisher.publish(KafkaPayload.Topic.EMPLOYEES, KafkaPayload.Action.UPDATE);
        }
        return updated;
    }
}
