package org.db.hrsp.service.repository.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.*;
import org.db.hrsp.service.repository.model.util.AbstractEntity;

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
