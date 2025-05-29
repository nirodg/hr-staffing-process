package org.db.hrsp.api.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO extends AbstractEntityDto {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String position;
    private boolean available;
    private List<RoleDto> roles;
}
