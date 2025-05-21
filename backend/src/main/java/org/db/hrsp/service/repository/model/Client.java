package org.db.hrsp.service.repository.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.*;
import org.db.hrsp.service.repository.model.util.AbstractEntity;

import java.util.List;

@Entity
@Getter
@Setter
public class Client extends AbstractEntity {

    @OneToMany
    @JsonIgnore
    private List<StaffingProcess> staffingProcesses;

    private String clientName;
    private String clientEmail;
    private String contactPersonName;
    private String contactPersonEmail;
    private String contactPersonPhone;

}
