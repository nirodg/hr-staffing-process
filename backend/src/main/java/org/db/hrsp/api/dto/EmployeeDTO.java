package org.db.hrsp.api.dto;

import lombok.Getter;
import lombok.Setter;
import org.db.hrsp.service.repository.model.StaffingProcess;

import java.util.List;

@Getter
@Setter
public class EmployeeDTO extends AbstractEntityDto {

    private String name;
    private boolean isAvailable = true;
    private List<StaffingProcess> staffingProcesses;
}
