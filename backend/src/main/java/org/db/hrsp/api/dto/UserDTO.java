package org.db.hrsp.api.dto;

import lombok.*;
import org.db.hrsp.service.repository.model.util.AbstractEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO extends AbstractEntity {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String position;
    private boolean available;
}
