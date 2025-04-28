package org.db.hrsp.model;

import org.db.hrsp.util.AbstractEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User  extends AbstractEntity {

    @Column(unique = true, nullable = false)
    private String username;

    private String firstName;
    private String lastName;
    private String position;
    private String email;
    private boolean available;

    @OneToMany
    @JsonIgnore
    private List<StaffingProcess> staffingProcesses;

    @ElementCollection
    private List<Role> roles;

}
