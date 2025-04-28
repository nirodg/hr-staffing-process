package org.db.hrsp.dto;

import org.db.hrsp.model.StaffingProcess;
import org.db.hrsp.util.AbstractEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EmployeeDTO extends AbstractEntity {

    private String name;
    private boolean isAvailable = true;
    private List<StaffingProcess> staffingProcesses;
}
