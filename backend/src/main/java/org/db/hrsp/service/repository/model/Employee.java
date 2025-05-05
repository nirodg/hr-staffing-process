package org.db.hrsp.service.repository.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import org.db.hrsp.service.repository.model.util.AbstractEntity;

import java.util.List;

@Entity
@Getter
@Setter
public class Employee extends AbstractEntity {

    private String name;
    private boolean isAvailable = true;

    @OneToMany
    @JsonIgnore
    private List<StaffingProcess> staffingProcesses;

    @OneToOne
    private User user;

    public Employee(String name, boolean isAvailable) {
        this.name = name;
        this.isAvailable = isAvailable;
    }

    public Employee() {
    }

}
