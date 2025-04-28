package org.db.hrsp.model;

import org.db.hrsp.util.AbstractEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
