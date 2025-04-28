package org.db.hrsp.model;

import org.db.hrsp.util.AbstractEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

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
