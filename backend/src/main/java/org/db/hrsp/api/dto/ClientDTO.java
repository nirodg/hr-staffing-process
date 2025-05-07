package org.db.hrsp.api.dto;

import lombok.Getter;
import lombok.Setter;
import org.db.hrsp.service.repository.model.StaffingProcess;
import org.db.hrsp.service.repository.model.util.AbstractEntity;

import java.util.List;

@Getter
@Setter
public class ClientDTO extends AbstractEntity {

    private String clientName;
    private List<StaffingProcess> staffingProcesses;
    private String clientEmail;
    private String contactPersonName;
    private String contactPersonEmail;
    private String contactPersonPhone;
}
