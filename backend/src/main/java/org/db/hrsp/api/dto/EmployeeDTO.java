package org.db.hrsp.api.dto;

import lombok.Getter;
import lombok.Setter;
import org.db.hrsp.service.repository.model.StaffingProcess;
import org.db.hrsp.service.repository.model.util.AbstractEntity;

import java.util.List;

@Getter
@Setter
public class EmployeeDTO extends AbstractEntity {

    private String name;
    private boolean isAvailable = true;
    private List<StaffingProcess> staffingProcesses;
}
